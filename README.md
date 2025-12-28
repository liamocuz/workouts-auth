# Workouts Auth - OAuth2 Authorization Server

A production-ready OAuth2 Authorization Server built with Spring Boot 4.0.1, implementing the OAuth 2.0 and OpenID Connect protocols for secure authentication and authorization.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Security Features](#security-features)
- [API Endpoints](#api-endpoints)
- [Testing](#testing)
- [Deployment](#deployment)
- [Contributing](#contributing)

## Overview

This authorization server provides centralized authentication and authorization services for the Workouts application ecosystem. It supports both local user registration/authentication and federated login via OAuth2 providers (Google, Facebook).

## Features

### Core Features
- ✅ OAuth 2.0 Authorization Server implementation
- ✅ OpenID Connect (OIDC) support
- ✅ Local user registration and authentication
- ✅ Federated authentication (Google, Facebook)
- ✅ Email verification workflow
- ✅ JWT token customization with custom claims
- ✅ Session management with JDBC-backed sessions
- ✅ Role-based access control (RBAC)

### Security Features
- ✅ BCrypt password hashing
- ✅ PKCE (Proof Key for Code Exchange) support
- ✅ CSRF protection
- ✅ CORS configuration
- ✅ Secure session cookies (HttpOnly, SameSite)
- ✅ Email verification for local accounts
- ✅ Token expiration and refresh token rotation

### Observability
- ✅ Distributed tracing with Zipkin
- ✅ Prometheus metrics
- ✅ Spring Boot Actuator endpoints
- ✅ Comprehensive logging

## Technology Stack

### Core Framework
- **Spring Boot 4.0.1** - Application framework
- **Java 17** - Programming language
- **Maven** - Build tool

### Security
- **Spring Security 7.x** - Security framework
- **Spring Authorization Server** - OAuth2/OIDC implementation
- **BCrypt** - Password hashing

### Persistence
- **PostgreSQL** - Primary database
- **Spring Data JPA** - Data access layer
- **Hibernate** - ORM with bytecode enhancement

### Frontend
- **Thymeleaf** - Server-side template engine
- **Spring Security Thymeleaf Extras** - Security integration

### Observability
- **Micrometer** - Metrics collection
- **Zipkin** - Distributed tracing
- **Prometheus** - Metrics aggregation

### Development
- **Spring Boot DevTools** - Hot reload and development utilities
- **Testcontainers** - Integration testing with Docker
- **JUnit 5** - Testing framework
- **Mockito** - Mocking framework

### Native Compilation
- **GraalVM Native Image** - AOT compilation support

## Architecture

### Component Overview

```
┌─────────────────────────────────────────────────────────────┐
│                    Client Applications                       │
│            (Web BFF, Mobile Apps, etc.)                     │
└────────────────────────┬────────────────────────────────────┘
                         │
                         │ OAuth2/OIDC
                         ▼
┌─────────────────────────────────────────────────────────────┐
│               Workouts Auth Server                          │
│                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │ Auth         │  │ OAuth2       │  │ User         │     │
│  │ Controller   │  │ Authorization│  │ Management   │     │
│  │              │  │ Server       │  │              │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
│                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │ Security     │  │ JWT          │  │ Email        │     │
│  │ Config       │  │ Config       │  │ Service      │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                    PostgreSQL Database                       │
│  - User Information                                          │
│  - OAuth2 Authorizations                                     │
│  - Registered Clients                                        │
│  - Sessions                                                  │
└─────────────────────────────────────────────────────────────┘
```

### Key Components

#### 1. **SecurityConfig**
- Configures two security filter chains:
  - OAuth2 Authorization Server endpoints
  - Application endpoints (signin, signup, etc.)
- Manages registered OAuth2 clients
- Configures CORS and CSRF policies

#### 2. **UserInfoService**
- Manages user lifecycle (registration, verification, OAuth2 user sync)
- Handles both LOCAL and federated users
- JIT (Just-In-Time) user provisioning for OAuth2 users

#### 3. **JwtConfig**
- Customizes JWT tokens with application-specific claims
- Separates access token claims (roles) from ID token claims (profile info)

#### 4. **AuthenticationEventHandler**
- Tracks user login events
- Updates `last_login` timestamp

#### 5. **OAuth2 User Services**
- `WorkoutsOAuth2UserService` - Handles OAuth2 providers (Facebook)
- `WorkoutsOidcUserService` - Handles OIDC providers (Google)

## Getting Started

### Prerequisites

- Java 17 or higher
- PostgreSQL 12 or higher
- Maven 3.6+
- (Optional) Docker for running PostgreSQL

### Database Setup

1. Create a PostgreSQL database:
```sql
CREATE DATABASE workouts;
```

2. Run the schema migrations located in `src/main/resources/sql/`:
   - `auth_info.sql` - User and verification tables
   - `auth_state.sql` - OAuth2 state tables

### Environment Variables

Create a `.env` file or set the following environment variables:

```bash
# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=workouts
DB_USERNAME=your_db_user
DB_PASSWORD=your_db_password

# OAuth2 Provider Credentials
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
FACEBOOK_CLIENT_ID=your_facebook_client_id
FACEBOOK_CLIENT_SECRET=your_facebook_client_secret

# Registered Client Credentials
REACT_BFF_CLIENT_SECRET=your_bff_client_secret
```

### Running the Application

#### Development Mode

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

#### Production Mode

```bash
./mvnw clean package
java -jar target/workouts-auth-0.0.1-SNAPSHOT.jar
```

#### With GraalVM Native Image

```bash
./mvnw -Pnative native:compile
./target/workouts-auth
```

The application will start on `http://localhost:8080`

## Configuration

### Application Profiles

- **dev** (`application-dev.yaml`) - Development configuration with debug logging
- **prod** - Production configuration (add `application-prod.yaml`)

### Key Configuration Properties

#### Server Configuration
```yaml
server:
  port: 8080
  servlet:
    session:
      cookie:
        name: "AUTHSESSION"
        same-site: "Lax"
        secure: true  # Always true in production
        http-only: true
```

#### OAuth2 Client Registration

Registered clients are configured programmatically in `SecurityConfig.registeredClientRepository()`:

```java
RegisteredClient client = RegisteredClient
    .withId(UUID.randomUUID().toString())
    .clientId("workouts-web-bff-dev")
    .clientSecret("{bcrypt}...") // BCrypt encoded
    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
    .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
    .redirectUri("http://localhost:9090/login/oauth2/code/bff")
    .scope(OidcScopes.OPENID, OidcScopes.PROFILE, OidcScopes.EMAIL)
    .tokenSettings(
        TokenSettings.builder()
            .accessTokenTimeToLive(Duration.ofMinutes(15))
            .refreshTokenTimeToLive(Duration.ofDays(3))
            .reuseRefreshTokens(false)
            .build()
    )
    .build();
```

#### Token Settings

- **Access Token TTL**: 15 minutes
- **Refresh Token TTL**: 3 days
- **Refresh Token Reuse**: Disabled (rotation enabled)
- **PKCE**: Required for enhanced security

## Security Features

### Password Security
- BCrypt with default strength (10 rounds)
- Passwords never stored in plain text
- Password confirmation validation

### Email Verification
- Time-limited verification tokens (24 hours)
- Secure token generation using UUID
- Token cleanup after verification

### Session Security
- JDBC-backed sessions for horizontal scalability
- Secure session cookies with HttpOnly and SameSite
- Configurable session timeout

### CSRF Protection
- Enabled for all state-changing operations
- Synchronizer token pattern
- Exceptions for OAuth2 authorization endpoints

### CORS Configuration
```java
CorsConfiguration configuration = new CorsConfiguration();
configuration.addAllowedOrigin("http://localhost:9090");
configuration.addAllowedMethod("*");
configuration.addAllowedHeader("*");
configuration.setAllowCredentials(true);
```

**Note**: Update allowed origins for production!

## API Endpoints

### Public Endpoints

#### Authentication
- `GET /signin` - Login page
- `POST /login` - Process login (form post)
- `GET /signup` - Registration page
- `POST /signup` - Process registration

#### Email Verification
- `GET /verify?token={token}` - Verify email address
- `GET /resend-verification` - Resend verification page
- `POST /resend-verification` - Resend verification email

#### OAuth2 Login
- `GET /oauth2/authorization/{provider}` - Initiate OAuth2 login (google, facebook)

### OAuth2 Authorization Server Endpoints

- `GET /oauth2/authorize` - Authorization endpoint
- `POST /oauth2/token` - Token endpoint
- `POST /oauth2/revoke` - Token revocation
- `POST /oauth2/introspect` - Token introspection
- `GET /.well-known/oauth-authorization-server` - Server metadata
- `GET /.well-known/openid-configuration` - OIDC configuration
- `GET /oauth2/jwks` - JSON Web Key Set
- `GET /userinfo` - OIDC UserInfo endpoint

### Actuator Endpoints

- `GET /actuator/health` - Health check
- `GET /actuator/info` - Application info
- `GET /actuator/prometheus` - Prometheus metrics
- `GET /actuator/metrics` - Micrometer metrics

## Testing

### Running Tests

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=UserInfoServiceTest

# Run tests with coverage
./mvnw test jacoco:report
```

### Test Structure

The project includes comprehensive test coverage:

- **Unit Tests**: 94 tests covering services, controllers, and validation
- **Integration Tests**: Database and repository tests using Testcontainers
- **Security Tests**: Spring Security test support

### Test Coverage Summary

| Component | Test Count | Coverage |
|-----------|-----------|----------|
| Controllers | 15 | High |
| Services | 30 | High |
| Repositories | 20 | High |
| Security | 10 | Medium |
| Validators | 8 | High |
| Models | 11 | High |

### Testcontainers

The project uses Testcontainers for integration testing with PostgreSQL:

```java
@Testcontainers
@SpringBootTest
public class PostgreSQLTestcontainer {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");
}
```

## Deployment

### Docker Deployment

Create a `Dockerfile`:

```dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/workouts-auth-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Build and run:

```bash
docker build -t workouts-auth:latest .
docker run -p 8080:8080 \
  -e DB_HOST=postgres \
  -e DB_USERNAME=user \
  -e DB_PASSWORD=pass \
  workouts-auth:latest
```

### Native Image Deployment

For faster startup and lower memory footprint:

```bash
./mvnw -Pnative native:compile
docker build -f Dockerfile.native -t workouts-auth:native .
```

### Production Checklist

- [ ] Update CORS configuration with production origins
- [ ] Enable HTTPS and set `secure: true` for cookies
- [ ] Configure proper database connection pooling
- [ ] Set up database backups
- [ ] Configure logging levels appropriately
- [ ] Set up monitoring and alerting
- [ ] Review and update token expiration times
- [ ] Enable rate limiting (add Spring Cloud Gateway or similar)
- [ ] Set up secret management (Vault, AWS Secrets Manager, etc.)
- [ ] Configure proper health checks
- [ ] Set up CI/CD pipeline
- [ ] Review security headers (add Spring Security headers configuration)

## Database Schema

### Core Tables

#### user_info
Stores user account information for both local and OAuth2 users.

```sql
CREATE TABLE user_info (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL UNIQUE,
    provider VARCHAR(255) NOT NULL,
    sub VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    given_name VARCHAR(255),
    family_name VARCHAR(255),
    display_name VARCHAR(255),
    password_hash VARCHAR(255), -- Only for LOCAL users
    email_verified BOOLEAN DEFAULT FALSE,
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    last_login TIMESTAMPTZ NULL,
    deleted_at TIMESTAMPTZ NULL,
    UNIQUE (provider, sub),
    UNIQUE (provider, email)
);
```

#### user_roles
Stores user role assignments.

```sql
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES user_info (id),
    role VARCHAR(255) NOT NULL,
    PRIMARY KEY (user_id, role)
);
```

#### user_verification
Stores email verification tokens.

```sql
CREATE TABLE user_verification (
    id BIGSERIAL PRIMARY KEY,
    token UUID NOT NULL UNIQUE,
    expiration TIMESTAMPTZ NOT NULL,
    user_id BIGSERIAL REFERENCES user_info (id)
);
```

### OAuth2 Tables

The authorization server uses Spring's default OAuth2 schema:
- `oauth2_authorization` - Stores authorization grants
- `oauth2_authorization_consent` - Stores user consent
- `oauth2_registered_client` - Stores registered clients

See `src/main/resources/sql/auth_state.sql` for the complete schema.

## Architecture Decisions

### Why Two Security Filter Chains?

The application uses two separate security filter chains:

1. **OAuth2 Authorization Server Chain** - Handles OAuth2 endpoints with stateless authentication
2. **Application Chain** - Handles user-facing pages (login, signup) with session-based authentication

This separation provides better security isolation and allows different authentication mechanisms for different endpoints.

### Why JDBC Sessions?

JDBC-backed sessions enable:
- Horizontal scalability (session sharing across instances)
- Session persistence across restarts
- Centralized session management

### Why Separate sub and publicId?

- **sub** (subject): The provider's unique identifier (Google ID, Facebook ID, or random UUID for LOCAL)
- **publicId**: Our internal UUID that we control and expose safely

This separation allows:
- Provider independence
- Consistent public identifiers across auth providers
- Safe exposure of user IDs without leaking provider information

## Common Issues and Solutions

### Issue: Tests failing with database connection errors
**Solution**: Ensure Docker is running for Testcontainers, or check PostgreSQL connection settings.

### Issue: OAuth2 login fails with redirect_uri_mismatch
**Solution**: Verify the redirect URI in Google/Facebook console matches exactly with your configuration.

### Issue: Email verification not working
**Solution**: Check email service configuration. In development, use MailHog or similar SMTP testing tool.

### Issue: Native image compilation fails
**Solution**: Ensure GraalVM is installed and JAVA_HOME points to GraalVM installation.

## Contributing

### Code Style
- Follow standard Java coding conventions
- Use meaningful variable and method names
- Add JavaDoc for public APIs
- Keep methods focused and small

### Pull Request Process
1. Create a feature branch
2. Write tests for new functionality
3. Ensure all tests pass
4. Update documentation as needed
5. Submit pull request with clear description

### Testing Requirements
- All new code must have unit tests
- Integration tests for new features
- Maintain or improve code coverage

## License

[Add your license here]

## Support

For issues, questions, or contributions, please [open an issue](https://github.com/liamocuz/workouts-auth/issues).

## Acknowledgments

- Spring Security Team for the excellent OAuth2 Authorization Server
- Spring Boot Team for the amazing framework
- Contributors and maintainers

---

**Built with ❤️ using Spring Boot 4.0.1**
