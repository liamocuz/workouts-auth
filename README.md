# Workouts Auth

A comprehensive OAuth2 Authorization Server built with Spring Boot that provides authentication and authorization services for the Workouts application ecosystem. This server implements the OAuth2/OpenID Connect authorization code flow with PKCE (Proof Key for Code Exchange) for secure authentication.

## Technology Stack

- **Framework**: Spring Boot 4.0.1
- **Security**: Spring Security OAuth2 Authorization Server
- **Database**: PostgreSQL with JPA/Hibernate
- **Session Management**: Spring Session JDBC
- **Additional Features**: 
  - Email verification for local users
  - OAuth2/OIDC federated authentication (Google)
  - JWT token generation with custom claims
  - Observability with Micrometer and Zipkin

## Key Features

- **OAuth2 Authorization Server**: Full-featured OAuth2/OIDC provider supporting authorization code flow with PKCE
- **Multiple Authentication Methods**:
  - Local username/password authentication with email verification
  - Federated authentication via OAuth2 providers (Google)
- **BFF (Backend for Frontend) Support**: Configured to work with a BFF client at `http://localhost:9090`
- **Secure Token Management**: JWT access tokens and refresh tokens with configurable TTLs
- **User Management**: Comprehensive user information storage with roles and permissions
- **Session Management**: Separate session management for auth server and BFF clients

## OAuth2 Authorization Code Flow with PKCE

This section provides a detailed step-by-step description of how the OAuth2 authorization code flow works in this system, including where all data is stored in the database.

### Overview

The application acts as an **OAuth2 Authorization Server** that issues tokens to client applications (like a BFF - Backend for Frontend). The BFF acts as an OAuth2 client that requests authorization on behalf of users.

### Participants

1. **User**: The end user accessing the application through their browser
2. **BFF (Backend for Frontend)**: The OAuth2 client application running at `http://localhost:9090`
3. **Authorization Server**: This application (Workouts Auth) running at the default port
4. **Resource Server**: Backend APIs that accept and validate the tokens issued by this authorization server

### Flow Steps

#### 1. User Initiates Login at BFF

**What Happens:**
- User navigates to the BFF application (e.g., `http://localhost:9090`)
- BFF detects the user is not authenticated
- BFF redirects user to the Authorization Server's authorization endpoint

**URL Example:**
```
GET /oauth2/authorize
  ?response_type=code
  &client_id=react-bff
  &redirect_uri=http://localhost:9090/login/oauth2/code/bff
  &scope=openid profile email
  &state=<random_state>
  &code_challenge=<pkce_code_challenge>
  &code_challenge_method=S256
```

**Database Activity:**
- **Table**: `bff_session`
  - A new session is created for the BFF with the PKCE verifier and state
  - Stores: `session_id`, `creation_time`, `last_access_time`, `expiry_time`
- **Table**: `bff_session_attributes`
  - Session attributes including OAuth2 state and PKCE verifier are stored
  - Stores: `session_primary_id`, `attribute_name`, `attribute_bytes`

#### 2. User Arrives at Authorization Server Login Page

**What Happens:**
- Authorization Server receives the authorization request
- Checks if user is authenticated in the auth server session
- If not authenticated, redirects to login page (`/signin`)

**Database Activity:**
- **Table**: `oauth2_authorization` (pending entry)
  - A preliminary authorization record may be created with state information
  - Stores: `id`, `registered_client_id`, `principal_name` (null at this point), `state`, `authorization_grant_type`

#### 3. User Authenticates

Users can authenticate via two methods:

##### 3a. Local Authentication (Username/Password)

**What Happens:**
- User enters email and password on `/signin` page
- Form submits to `/login` (Spring Security's default login processing URL)
- System validates credentials against stored password hash

**Database Activity:**
- **Table**: `user_info`
  - System queries to find user by `provider='LOCAL'` and `email`
  - Validates `password_hash` using BCrypt
  - Checks `email_verified=true` and `enabled=true`
  - Updates `last_login` timestamp upon successful authentication
  - Relevant columns: `id`, `public_id`, `provider`, `sub`, `email`, `password_hash`, `email_verified`, `enabled`, `last_login`
  
- **Table**: `user_roles`
  - Loads user roles for authorization
  - Stores: `user_id`, `role` (e.g., 'USER', 'ADMIN')

- **Table**: `auth_session`
  - Creates a new authenticated session for the auth server
  - Stores: `session_id`, `principal_name` (user's email), `creation_time`, `expiry_time`
  
- **Table**: `auth_session_attributes`
  - Stores authentication details and security context
  - Stores: `session_primary_id`, `attribute_name` (e.g., 'SPRING_SECURITY_CONTEXT'), `attribute_bytes`

##### 3b. Federated Authentication (Google OAuth2)

**What Happens:**
- User clicks "Sign in with Google" button
- Redirected to Google's authorization endpoint with `prompt=login` parameter
- User authenticates with Google
- Google redirects back to auth server with authorization code
- Auth server exchanges code for tokens with Google
- Auth server creates or updates user record

**Database Activity:**
- **Table**: `user_info`
  - Creates new user if first login, or retrieves existing user
  - For Google users: `provider='GOOGLE'`, `sub` is Google's subject identifier
  - Stores: `public_id` (internal UUID), `provider`, `sub`, `email`, `given_name`, `family_name`, `email_verified`, `enabled`
  - Updates `last_login` timestamp
  
- **Table**: `user_roles`
  - Assigns default 'USER' role to new users
  
- **Table**: `auth_session`
  - Creates authenticated session for the user
  - Stores: `session_id`, `principal_name`, `creation_time`, `expiry_time`

#### 4. User Consents to Authorization

**What Happens:**
- After authentication, if `requireAuthorizationConsent=false` (current configuration), consent is implicit
- If consent is required, user sees consent page showing requested scopes
- User approves access for the BFF client

**Database Activity:**
- **Table**: `oauth2_authorization_consent` (if consent is required)
  - Stores user's consent for specific scopes
  - Stores: `registered_client_id`, `principal_name`, `authorities` (granted scopes)
  - Note: Currently not used as `requireAuthorizationConsent=false` in configuration

#### 5. Authorization Code Generation

**What Happens:**
- Authorization Server generates an authorization code
- Stores the authorization code with an expiration time (typically 5 minutes)
- Redirects user back to BFF with authorization code in URL

**Redirect Example:**
```
HTTP/1.1 302 Found
Location: http://localhost:9090/login/oauth2/code/bff?code=<authorization_code>&state=<state>
```

**Database Activity:**
- **Table**: `oauth2_authorization`
  - Creates or updates authorization record with generated code
  - Stores:
    - `id`: Unique identifier
    - `registered_client_id`: 'react-bff'
    - `principal_name`: User's email or identifier
    - `authorization_grant_type`: 'authorization_code'
    - `authorized_scopes`: 'openid profile email'
    - `state`: OAuth2 state parameter for CSRF protection
    - `authorization_code_value`: The generated authorization code (hashed)
    - `authorization_code_issued_at`: Timestamp when code was issued
    - `authorization_code_expires_at`: Timestamp when code expires (typically 5 minutes)
    - `authorization_code_metadata`: JSON with code challenge method and other metadata

#### 6. BFF Receives Authorization Code

**What Happens:**
- User's browser is redirected to BFF callback URL with authorization code
- BFF validates the state parameter to prevent CSRF attacks
- BFF prepares to exchange the code for tokens

**Database Activity:**
- **Table**: `bff_session`
  - BFF retrieves session to validate state parameter
  - Updates `last_access_time`
  
- **Table**: `bff_session_attributes`
  - Retrieves stored state and PKCE code verifier

#### 7. BFF Exchanges Code for Tokens

**What Happens:**
- BFF makes a backend-to-backend request to the token endpoint
- Request includes:
  - Authorization code
  - Client credentials (client_id and client_secret via HTTP Basic Auth)
  - PKCE code verifier
  - Redirect URI (must match original request)

**Request Example:**
```
POST /oauth2/token
Content-Type: application/x-www-form-urlencoded
Authorization: Basic <base64(client_id:client_secret)>

grant_type=authorization_code
&code=<authorization_code>
&redirect_uri=http://localhost:9090/login/oauth2/code/bff
&code_verifier=<pkce_code_verifier>
```

**What the Authorization Server Does:**
1. Validates client credentials against registered client
2. Validates authorization code hasn't expired or been used
3. Validates PKCE code verifier matches code challenge
4. Validates redirect URI matches
5. Generates access token (JWT), ID token (JWT), and refresh token
6. Invalidates the authorization code (one-time use)

**Database Activity:**
- **Table**: `oauth2_registered_client`
  - Validates client credentials
  - Retrieves: `client_id`, `client_secret` (BCrypt hash), `client_authentication_methods`, `token_settings`
  
- **Table**: `oauth2_authorization`
  - Validates authorization code
  - Marks authorization code as used by storing tokens
  - Updates record with:
    - `access_token_value`: Generated JWT access token (hashed)
    - `access_token_issued_at`: Current timestamp
    - `access_token_expires_at`: Current timestamp + 15 minutes (per config)
    - `access_token_type`: 'Bearer'
    - `access_token_scopes`: 'openid profile email'
    - `access_token_metadata`: JSON with token format and additional metadata
    - `oidc_id_token_value`: Generated ID token (JWT, hashed)
    - `oidc_id_token_issued_at`: Current timestamp
    - `oidc_id_token_expires_at`: Current timestamp + token TTL
    - `oidc_id_token_metadata`: JSON with token metadata
    - `refresh_token_value`: Generated refresh token (hashed)
    - `refresh_token_issued_at`: Current timestamp
    - `refresh_token_expires_at`: Current timestamp + 3 days (per config)
    - `refresh_token_metadata`: JSON with token metadata
    - `authorization_code_value`: Cleared or marked as used

#### 8. BFF Receives Tokens

**Token Response Example:**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refresh_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "id_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 900,
  "scope": "openid profile email"
}
```

**JWT Access Token Claims:**
- `sub`: User's public_id (UUID)
- `aud`: Client ID (audience)
- `iss`: Issuer (Authorization Server URL)
- `exp`: Expiration timestamp
- `iat`: Issued at timestamp
- `scope`: Granted scopes
- Custom claims: `email`, `given_name`, `family_name`, `display_name`, `roles`, `provider`

**Database Activity:**
- **Table**: `oauth2_authorized_client`
  - BFF stores the tokens for future use
  - Stores:
    - `client_registration_id`: 'bff'
    - `principal_name`: User identifier
    - `access_token_type`: 'Bearer'
    - `access_token_value`: Access token (encrypted)
    - `access_token_issued_at`: Timestamp
    - `access_token_expires_at`: Timestamp
    - `access_token_scopes`: 'openid profile email'
    - `refresh_token_value`: Refresh token (encrypted)
    - `refresh_token_issued_at`: Timestamp
    - `created_at`: Timestamp

- **Table**: `bff_session_attributes`
  - Updates session with authentication information
  - Stores authenticated principal and OAuth2 authentication details

#### 9. BFF Uses Access Token to Call Resource Servers

**What Happens:**
- BFF includes access token in Authorization header when calling backend APIs
- Resource servers validate the JWT signature using the Authorization Server's public key
- Resource servers extract claims from JWT to determine user identity and permissions

**Request Example:**
```
GET /api/workouts
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Database Activity:**
- No database activity on the Authorization Server (stateless JWT validation)
- Resource servers may query their own databases based on the `sub` claim

#### 10. Token Refresh Flow

**What Happens:**
- When access token expires (after 15 minutes), BFF uses refresh token to obtain new tokens
- BFF makes request to token endpoint with refresh token

**Request Example:**
```
POST /oauth2/token
Content-Type: application/x-www-form-urlencoded
Authorization: Basic <base64(client_id:client_secret)>

grant_type=refresh_token
&refresh_token=<refresh_token>
```

**Database Activity:**
- **Table**: `oauth2_authorization`
  - Validates refresh token hasn't expired or been revoked
  - Since `reuseRefreshTokens=false`, generates new refresh token
  - Updates record with:
    - New `access_token_value` and metadata
    - New `refresh_token_value` and metadata (old token is invalidated)
    - Updated timestamps

- **Table**: `oauth2_authorized_client`
  - BFF updates stored tokens
  - Updates `access_token_value`, `access_token_expires_at`, `refresh_token_value`

### Database Tables Summary

Here's a complete summary of all database tables involved in the OAuth2 flow:

#### Auth Server Tables

1. **auth_session**: Stores user sessions on the authorization server
2. **auth_session_attributes**: Stores session attributes including security context
3. **oauth2_authorization**: Core table storing authorization grants, codes, and tokens
4. **oauth2_authorization_consent**: Stores user consent for OAuth2 clients (if required)
5. **oauth2_registered_client**: Stores registered OAuth2 client configurations
6. **user_info**: Stores user account information (both local and federated)
7. **user_roles**: Stores user role assignments
8. **user_verification**: Stores email verification tokens for local users

#### BFF Client Tables

9. **bff_session**: Stores user sessions on the BFF
10. **bff_session_attributes**: Stores BFF session attributes including OAuth2 state and PKCE
11. **oauth2_authorized_client**: Stores tokens that the BFF has obtained from the auth server

### Security Features

1. **PKCE (Proof Key for Code Exchange)**: Required for all authorization code flows
   - `requireProofKey=true` in client settings
   - Prevents authorization code interception attacks

2. **State Parameter**: Used for CSRF protection during OAuth2 flow

3. **Secure Token Storage**: 
   - Tokens stored in database are hashed/encrypted
   - JWTs are signed with RS256 algorithm

4. **Token Expiration**:
   - Access tokens: 15 minutes
   - Refresh tokens: 3 days
   - Authorization codes: ~5 minutes (default)
   - Refresh tokens are not reused (`reuseRefreshTokens=false`)

5. **Email Verification**: Local users must verify email before authentication succeeds

6. **Password Security**: Passwords hashed with BCrypt

### Local User Registration Flow

For completeness, here's how local users are registered:

1. **User Submits Registration Form** (`POST /signup`)
   - Provides: email, password, given name, family name
   - System validates password requirements and format

2. **Database Activity**:
   - **Table**: `user_info`
     - Creates new user with `provider='LOCAL'`, `email_verified=false`, `enabled=true`
     - Generates `public_id` and `sub` (same UUID for local users)
     - Stores BCrypt hashed password in `password_hash`
   
   - **Table**: `user_verification`
     - Creates verification token with expiration (typically 24 hours)
     - Stores: `token` (UUID), `user_id`, `expiration`

3. **Email Sent**: System sends verification email with link containing token

4. **User Clicks Verification Link** (`GET /verify?token=<token>`)
   - System validates token from `user_verification` table
   - Updates `user_info.email_verified = true`
   - Deletes verification token from `user_verification`

5. **User Can Now Login**: User can authenticate using their email and password

## Getting Started

### Prerequisites

- Java 25
- PostgreSQL database
- Maven (or use included `mvnw`)

### Database Setup

Execute the SQL scripts in order:
1. `src/main/resources/sql/auth_info.sql` - Creates user and role tables
2. `src/main/resources/sql/auth_state.sql` - Creates OAuth2 and session tables

### Configuration

Configure the following in `application-dev.yaml` or environment variables:
- Database connection properties
- OAuth2 client credentials for federated providers
- Registered client credentials for BFF
- Email server configuration (for verification emails)

### Running the Application

```bash
./mvnw spring-boot:run
```

The authorization server will start on the default port (typically 8080).

## API Endpoints

### Authentication Endpoints
- `GET /signin` - Login page
- `POST /login` - Process login (Spring Security default)
- `GET /signup` - Registration page  
- `POST /signup` - Process registration
- `GET /verify?token=<token>` - Verify email address
- `GET /resend-verification` - Resend verification email page
- `POST /resend-verification` - Process resend verification

### OAuth2 Endpoints (Standard)
- `GET /oauth2/authorize` - Authorization endpoint
- `POST /oauth2/token` - Token endpoint
- `POST /oauth2/revoke` - Token revocation endpoint
- `GET /.well-known/openid-configuration` - OIDC discovery endpoint
- `GET /oauth2/jwks` - JSON Web Key Set endpoint

### OAuth2 Login (Federated)
- `GET /oauth2/authorization/google` - Initiate Google login

## License

[Add your license here]

## Contact

[Add your contact information here]
