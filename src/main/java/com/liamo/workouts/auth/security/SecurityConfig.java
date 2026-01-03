package com.liamo.workouts.auth.security;

import com.liamo.workouts.auth.config.properties.RegisteredClientProperties;
import com.liamo.workouts.auth.service.WorkoutsOAuth2UserService;
import com.liamo.workouts.auth.service.WorkoutsOidcUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Duration;
import java.util.UUID;

@Configuration
public class SecurityConfig {

    private final OAuth2AuthorizationServerConfigurer configurer;

    public SecurityConfig() {
        this.configurer = new OAuth2AuthorizationServerConfigurer();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain oauthChain(HttpSecurity http) {
        http
            .securityMatcher(configurer.getEndpointsMatcher())
            .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
            .with(configurer, (as) -> as.oidc(Customizer.withDefaults()))
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )
            .exceptionHandling(exception -> exception
                .defaultAuthenticationEntryPointFor(
                    new LoginUrlAuthenticationEntryPoint("/signin"),
                    new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                )
            )
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain appChain(
        HttpSecurity http,
        OAuth2AuthorizationRequestResolver customAuthorizationRequestResolver,
        WorkoutsOAuth2UserService oAuth2UserService,
        WorkoutsOidcUserService oidcUserService
    ) {
        http
            .securityMatcher("/**")
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(
                    "/signin",
                    "/signup",
                    "/verify",
                    "/resend-verification",
                    "/error",
                    "/favicon.ico",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/actuator/**"
                )
                .permitAll()    // All the request matchers are allowed
                .anyRequest().denyAll()
            )
            .formLogin(form -> form
                .loginPage("/signin")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/signin")
                .authorizationEndpoint(authorization -> authorization
                    .authorizationRequestResolver(customAuthorizationRequestResolver)
                )
                .userInfoEndpoint(userInfoEndpointConfig ->
                    userInfoEndpointConfig
                        .userService(oAuth2UserService)
                        .oidcUserService(oidcUserService)
                )
            )
            .csrf(Customizer.withDefaults())
            .cors(Customizer.withDefaults());

        return http.build();
    }

    /**
     * Custom OAuth2AuthorizationRequestResolver to add "prompt=login" to all authorization requests.
     * This is so that Google always prompts for account selection.
     */
    @Bean
    public OAuth2AuthorizationRequestResolver customAuthorizationRequestResolver(ClientRegistrationRepository repo) {
        DefaultOAuth2AuthorizationRequestResolver resolver =
            new DefaultOAuth2AuthorizationRequestResolver(repo, "/oauth2/authorization");
        resolver.setAuthorizationRequestCustomizer(request ->
            request.additionalParameters(params -> params.put("prompt", "login"))
        );
        return resolver;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("http://localhost:9090");
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository(
        JdbcTemplate jdbcTemplate,
        RegisteredClientProperties clientProperties,
        PasswordEncoder passwordEncoder
    ) {
        RegisteredClientRepository repo = new JdbcRegisteredClientRepository(jdbcTemplate);

        RegisteredClient webBffClient = repo.findByClientId(clientProperties.reactBffClientId());
        if (webBffClient == null) {
            RegisteredClient newReactBffClient = RegisteredClient
                .withId(UUID.randomUUID().toString())
                .clientId(clientProperties.reactBffClientId())
                .clientSecret(
                    passwordEncoder.encode(
                        clientProperties.reactBffClientSecret()
                    )
                )
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("http://localhost:9090/login/oauth2/code/bff")
                .postLogoutRedirectUri("http://localhost:9090/")
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .scope(OidcScopes.EMAIL)
                .clientSettings(
                    ClientSettings
                        .builder()
                        .requireProofKey(true)
                        .requireAuthorizationConsent(false)
                        .build()
                )
                .tokenSettings(
                    TokenSettings
                        .builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(15))
                        .refreshTokenTimeToLive(Duration.ofDays(3))
                        .reuseRefreshTokens(false)
                        .build()
                )
                .build();

            repo.save(newReactBffClient);
        }

        return repo;
    }

    @Bean
    public OAuth2AuthorizationService authorizationService(
        JdbcTemplate jdbcTemplate,
        RegisteredClientRepository registeredClientRepository
    ) {
        return new JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
