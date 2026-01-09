package com.liamo.workouts.auth.controller;

import com.liamo.workouts.auth.WorkoutsTestUtil;
import com.liamo.workouts.auth.model.dto.CreateUserRequestDTO;
import com.liamo.workouts.auth.model.entity.UserVerification;
import com.liamo.workouts.auth.service.EmailService;
import com.liamo.workouts.auth.service.UserInfoService;
import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserInfoService userInfoService;
    @MockitoBean
    private EmailService emailService;

    // Although tracer is not used in the AuthController, it is used
    // in the GlobalExceptionHandler, so we need to mock it because
    // @ControllerAdvice is loaded in too
    @MockitoBean
    private Tracer tracer;

    @Test
    void get_signin_thenReturnSigninView() throws Exception {
        mockMvc
            .perform(get("/signin").with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("signin"));
    }

    @Test
    void get_signup_thenReturnSignupView() throws Exception {
        mockMvc
            .perform(get("/signup").with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("signup"))
            .andExpect(model().attributeExists("createUserRequestDTO"));
    }

    @Test
    void post_signup_whenValidRequest_thenReturnSignupResultView() throws Exception {
        UserVerification us = new UserVerification(
            WorkoutsTestUtil.getLocalUserInfoBuilder().build(),
            24
        );

        when(userInfoService.registerNewLocalUser(any(CreateUserRequestDTO.class))).thenReturn(us);
        doNothing().when(emailService).sendRegistrationEmail(WorkoutsTestUtil.EMAIL, us.getToken());

        mockMvc
            .perform(
                post("/signup")
                    .with(csrf())
                    .param("email", WorkoutsTestUtil.EMAIL)
                    .param("givenName", WorkoutsTestUtil.GIVEN_NAME)
                    .param("familyName", WorkoutsTestUtil.FAMILY_NAME)
                    .param("password", WorkoutsTestUtil.PASSWORD)
                    .param("confirmPassword", WorkoutsTestUtil.PASSWORD)
            )
            .andExpect(status().isOk())
            .andExpect(view().name("signup-result"));
    }

    @Test
    void post_signup_whenBindingErrors_thenReturnSignupViewWithErrors() throws Exception {
        mockMvc
            .perform(
                post("/signup")
                    .with(csrf())
                    .param("email", "invalid-email")
                    .param("givenName", "")
                    .param("familyName", "")
                    .param("password", "")
                    .param("confirmPassword", "")
            )
            .andExpect(model().attributeHasFieldErrors(
                "createUserRequestDTO",
                "email",
                "givenName",
                "familyName",
                "password",
                "confirmPassword"
            ))
            .andExpect(status().isOk())
            .andExpect(view().name("signup"));
    }

    @Test
    void post_signup_whenPasswordMismatch_thenReturnSignupViewWithErrors() throws Exception {
        mockMvc
            .perform(
                post("/signup")
                    .with(csrf())
                    .param("email", WorkoutsTestUtil.EMAIL)
                    .param("givenName", WorkoutsTestUtil.GIVEN_NAME)
                    .param("familyName", WorkoutsTestUtil.FAMILY_NAME)
                    .param("password", WorkoutsTestUtil.PASSWORD)
                    .param("confirmPassword", "differentPassword")
            )
            .andExpect(model().attributeHasFieldErrors("createUserRequestDTO", "confirmPassword"))
            .andExpect(status().isOk())
            .andExpect(view().name("signup"));
    }

    @Test
    void post_resendVerification_whenValidEmail_thenRedirectWithSuccessMessage() throws Exception {
        UserVerification us = new UserVerification(
            WorkoutsTestUtil.getLocalUserInfoBuilder().build(),
            24
        );

        when(userInfoService.createNewUserVerification(WorkoutsTestUtil.EMAIL))
            .thenReturn(Optional.of(us));
        doNothing().when(emailService).sendRegistrationEmail(WorkoutsTestUtil.EMAIL, us.getToken());

        mockMvc
            .perform(
                post("/resend-verification")
                    .with(csrf())
                    .param("email", WorkoutsTestUtil.EMAIL)
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/resend-verification"))
            .andExpect(flash().attributeExists("message"));
    }

    @Test
    void post_resendVerification_whenInvalidEmail_thenRedirectWithErrorMessage() throws Exception {
        when(userInfoService.createNewUserVerification(WorkoutsTestUtil.EMAIL))
            .thenReturn(Optional.empty());

        mockMvc
            .perform(
                post("/resend-verification")
                    .with(csrf())
                    .param("email", WorkoutsTestUtil.EMAIL)
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/resend-verification"))
            .andExpect(flash().attributeExists("message"));
    }

    @Test
    void get_verify_whenValidToken_thenRedirectWithSuccessMessage() throws Exception {
        UUID token = UUID.randomUUID();
        when(userInfoService.verifyUserEmail(token.toString())).thenReturn(true);

        mockMvc
            .perform(
                get("/verify")
                    .with(csrf())
                    .param("token", token.toString())
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/signin"))
            .andExpect(flash().attributeExists("message"));
    }

    @Test
    void get_verify_whenInvalidToken_thenRedirectWithErrorMessage() throws Exception {
        UUID token = UUID.randomUUID();
        when(userInfoService.verifyUserEmail(token.toString())).thenReturn(false);

        mockMvc
            .perform(
                get("/verify")
                    .with(csrf())
                    .param("token", "invalid-token")
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/resend-verification"))
            .andExpect(flash().attributeExists("message"));
    }

    @Test
    void get_verify_whenMissingToken_thenRedirectWithErrorMessage() throws Exception {
        mockMvc
            .perform(
                get("/verify")
                    .with(csrf())
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/resend-verification"))
            .andExpect(flash().attributeExists("message"));
    }
}