# Workouts Auth

A Spring Boot OAuth2 Authorization Server for the Workouts application ecosystem.

## Overview

This is a standalone OAuth2 Authorization Server built with Spring Boot 3.5.0 and Spring Security OAuth2 Authorization Server. It provides authentication and authorization services for the Workouts application.

## Features

- **OAuth2 Authorization Server**: Full OAuth2 authorization server implementation
- **User Authentication**: Username/password authentication with BCrypt password encoding
- **JWT Token Support**: Issues JWT access tokens with RSA key signing
- **Database Integration**: PostgreSQL database for user storage
- **Actuator Endpoints**: Health and info endpoints for monitoring
- **Testcontainers Support**: Automated testing with PostgreSQL containers

## Technology Stack

- Java 17
- Spring Boot 3.5.0
- Spring Security OAuth2 Authorization Server
- Spring Data JPA
- PostgreSQL
- Maven
- JUnit 5 & Mockito
- Testcontainers

## Prerequisites

- Java 17 or higher
- Docker (for running PostgreSQL)
- Maven 3.6+ (or use included Maven wrapper)

## Getting Started

### Database Setup

The application requires a PostgreSQL database. You can run one using Docker:

```bash
docker run -d \
  --name workouts-auth-db \
  -e POSTGRES_DB=workouts_auth \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:latest
```

### Running the Application

1. Clone the repository
2. Run the application:

```bash
./mvnw spring-boot:run
```

The authorization server will start on port 9000.

### Running Tests

```bash
./mvnw test
```

Tests use Testcontainers to automatically spin up a PostgreSQL instance.

## Configuration

Key configuration properties in `application.properties`:

- `server.port=9000` - Server port
- `spring.datasource.url` - Database connection URL
- OAuth2 configuration is done programmatically in `AuthorizationServerConfig`

## OAuth2 Client Configuration

A default OAuth2 client is configured:

- **Client ID**: `workouts-client`
- **Client Secret**: `secret`
- **Grant Types**: Authorization Code, Refresh Token, Client Credentials
- **Scopes**: openid, profile, read, write
- **Redirect URIs**: 
  - `http://127.0.0.1:8080/login/oauth2/code/workouts-client`
  - `http://127.0.0.1:8080/authorized`

## Default User

A default user is created on startup for testing:

- **Username**: `user`
- **Password**: `password`

## API Endpoints

### OAuth2 Endpoints

- `GET /.well-known/oauth-authorization-server` - OAuth2 server metadata
- `GET /oauth2/authorize` - Authorization endpoint
- `POST /oauth2/token` - Token endpoint
- `POST /oauth2/introspect` - Token introspection
- `POST /oauth2/revoke` - Token revocation
- `GET /oauth2/jwks` - JSON Web Key Set

### Actuator Endpoints

- `GET /actuator/health` - Health check (public)
- `GET /actuator/info` - Application info (requires authentication)

## Testing OAuth2 Flow

### 1. Get Authorization Code

Open in browser:
```
http://localhost:9000/oauth2/authorize?response_type=code&client_id=workouts-client&scope=read&redirect_uri=http://127.0.0.1:8080/authorized
```

Login with username: `user`, password: `password`

### 2. Exchange Code for Token

```bash
curl -X POST http://localhost:9000/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u workouts-client:secret \
  -d "grant_type=authorization_code" \
  -d "code=<AUTHORIZATION_CODE>" \
  -d "redirect_uri=http://127.0.0.1:8080/authorized"
```

### 3. Client Credentials Flow

```bash
curl -X POST http://localhost:9000/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u workouts-client:secret \
  -d "grant_type=client_credentials" \
  -d "scope=read"
```

### 4. Refresh Token

```bash
curl -X POST http://localhost:9000/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u workouts-client:secret \
  -d "grant_type=refresh_token" \
  -d "refresh_token=<REFRESH_TOKEN>"
```

## Project Structure

```
src/
├── main/
│   ├── java/com/liamo/workoutsauth/
│   │   ├── config/
│   │   │   ├── AuthorizationServerConfig.java  # OAuth2 server configuration
│   │   │   ├── SecurityConfig.java             # Security configuration
│   │   │   └── DataInitializer.java            # Database initialization
│   │   ├── entity/
│   │   │   └── User.java                       # User entity
│   │   ├── repository/
│   │   │   └── UserRepository.java             # User repository
│   │   ├── service/
│   │   │   └── CustomUserDetailsService.java   # User details service
│   │   └── WorkoutsAuthApplication.java        # Main application
│   └── resources/
│       └── application.properties              # Application configuration
└── test/
    └── java/com/liamo/workoutsauth/
        ├── config/
        │   ├── AuthorizationServerConfigTest.java
        │   └── SecurityConfigTest.java
        ├── entity/
        │   └── UserTest.java
        ├── repository/
        │   └── UserRepositoryTest.java
        ├── service/
        │   └── CustomUserDetailsServiceTest.java
        └── TestcontainersConfiguration.java    # Test database configuration
```

## Security Considerations

- Passwords are encrypted using BCrypt
- JWT tokens are signed with RSA keys
- HTTPS should be used in production
- Client secrets should be properly secured in production
- Consider implementing rate limiting for token endpoints
- Regular security audits recommended

## Development

### Adding New Users

Users can be added programmatically through the `UserRepository`:

```java
User user = new User("username", passwordEncoder.encode("password"));
userRepository.save(user);
```

### Customizing OAuth2 Clients

Edit `AuthorizationServerConfig.java` to add or modify OAuth2 clients.

## License

This project is part of the Workouts application ecosystem.

## Contributors

- Liam O'Cuz

## Support

For issues and questions, please create an issue in the GitHub repository.
