# User Auth Specification

## ADDED Requirements

### Requirement: User can register with email
The system SHALL allow users to register an account using email, password, nickname, and verification code.

#### Scenario: Successful registration
- **WHEN** user submits valid email, password (8-20 chars), nickname, and correct verification code
- **THEN** system creates user account with `is_active = true`
- **AND** system returns JWT token
- **AND** user can access protected endpoints immediately

#### Scenario: Email already registered
- **WHEN** user submits an email that is already registered
- **THEN** system returns error code 400 with message "Email already registered"

#### Scenario: Nickname already taken
- **WHEN** user submits a nickname that is already taken
- **THEN** system returns error code 400 with message "Nickname already taken"

#### Scenario: Invalid verification code
- **WHEN** user submits an incorrect or expired verification code
- **THEN** system returns error code 400 with message "Invalid or expired verification code"

#### Scenario: Password too weak
- **WHEN** user submits a password with less than 8 characters
- **THEN** system returns error code 400 with message "Password must be at least 8 characters"

### Requirement: User can send verification code
The system SHALL allow users to request a 6-digit verification code to their email for registration.

#### Scenario: Successful code send
- **WHEN** user submits a valid email address
- **THEN** system generates a 6-digit code
- **AND** system sends email containing the code
- **AND** system returns success message

#### Scenario: Rate limiting
- **WHEN** user requests more than 5 verification codes within 10 minutes
- **THEN** system returns error code 429 with message "Too many requests, please try again later"

### Requirement: User can login with email and password
The system SHALL allow registered users to login using email and password, returning a JWT token.

#### Scenario: Successful login
- **WHEN** user submits correct email and password
- **AND** user's account `is_active = true`
- **THEN** system returns JWT token valid for 7 days

#### Scenario: Wrong password
- **WHEN** user submits correct email but wrong password
- **THEN** system returns error code 401 with message "Invalid email or password"

#### Scenario: Account not activated
- **WHEN** user submits correct credentials but account `is_active = false`
- **THEN** system returns error code 403 with message "Account not activated, please verify your email"

### Requirement: User can get current user info
The system SHALL allow authenticated users to retrieve their own profile information.

#### Scenario: Get own profile
- **WHEN** authenticated user requests their profile
- **THEN** system returns user info (id, email, nickname, avatar_url, created_at)

#### Scenario: Unauthenticated request
- **WHEN** unauthenticated user requests profile
- **THEN** system returns error code 401 with message "Unauthorized"

### Requirement: JWT token authentication
The system SHALL protect API endpoints by validating JWT tokens in the Authorization header.

#### Scenario: Valid token
- **WHEN** request includes valid JWT token in `Authorization: Bearer <token>`
- **THEN** system extracts user info from token
- **AND** request proceeds with user context

#### Scenario: Expired token
- **WHEN** request includes expired JWT token
- **THEN** system returns error code 401 with message "Token expired"

#### Scenario: Invalid token
- **WHEN** request includes malformed or invalid JWT token
- **THEN** system returns error code 401 with message "Invalid token"

### Requirement: User can get avatar URL
The system SHALL generate avatar URL based on user's avatar_type.

#### Scenario: Gravatar type
- **WHEN** user's `avatar_type = 'GRAVATAR'`
- **THEN** system returns Gravatar URL based on user email hash

#### Scenario: Default type
- **WHEN** user's `avatar_type = 'DEFAULT'`
- **THEN** system returns default avatar URL (e.g., generated initials or placeholder image)
