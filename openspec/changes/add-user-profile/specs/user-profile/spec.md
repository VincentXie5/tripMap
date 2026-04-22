# User Profile Specification

## ADDED Requirements

### Requirement: User can get profile
The system SHALL allow authenticated users to retrieve their complete profile information.

#### Scenario: Successful profile retrieval
- **WHEN** authenticated user requests `GET /profile`
- **THEN** system returns profile info including id, email, nickname, avatar_type, is_active, created_at, avatar_url

#### Scenario: Unauthenticated request
- **WHEN** unauthenticated user requests `GET /profile`
- **THEN** system returns error code 401 with message "Unauthorized"

### Requirement: User can update avatar type
The system SHALL allow authenticated users to update their avatar type.

#### Scenario: Update to Gravatar
- **WHEN** authenticated user requests `PUT /profile/avatar` with `avatar_type = "GRAVATAR"`
- **THEN** system updates user's avatar_type to GRAVATAR
- **AND** system returns success with updated profile

#### Scenario: Update to Default avatar
- **WHEN** authenticated user requests `PUT /profile/avatar` with `avatar_type = "DEFAULT"`
- **THEN** system updates user's avatar_type to DEFAULT
- **AND** system returns success with updated profile

#### Scenario: Invalid avatar type
- **WHEN** authenticated user requests `PUT /profile/avatar` with invalid avatar_type
- **THEN** system returns error code 400 with message "Invalid avatar type"

### Requirement: User can update nickname
The system SHALL allow authenticated users to update their nickname.

#### Scenario: Successful nickname update
- **WHEN** authenticated user requests `PUT /profile/nickname` with new unique nickname
- **THEN** system updates user's nickname
- **AND** system returns success with updated profile

#### Scenario: Nickname already taken
- **WHEN** authenticated user requests `PUT /profile/nickname` with a nickname that already exists
- **THEN** system returns error code 400 with message "Nickname already taken"

#### Scenario: Same nickname
- **WHEN** authenticated user requests `PUT /profile/nickname` with their current nickname
- **THEN** system returns success without making changes

### Requirement: User can change password
The system SHALL allow authenticated users to change their password by verifying the old password first.

#### Scenario: Successful password change
- **WHEN** authenticated user requests `PUT /profile/password` with correct old_password and valid new_password (8-20 chars)
- **THEN** system updates password to new password hash
- **AND** system returns success message

#### Scenario: Wrong old password
- **WHEN** authenticated user requests `PUT /profile/password` with incorrect old_password
- **THEN** system returns error code 401 with message "Invalid old password"

#### Scenario: New password too short
- **WHEN** authenticated user requests `PUT /profile/password` with new_password less than 8 characters
- **THEN** system returns error code 400 with message "Password must be at least 8 characters"

#### Scenario: Same password
- **WHEN** authenticated user requests `PUT /profile/password` with new_password same as current password
- **THEN** system returns error code 400 with message "New password must be different from current password"

### Requirement: User can request email change verification
The system SHALL allow authenticated users to request a verification code for changing their email address.

#### Scenario: Send verification code to new email
- **WHEN** authenticated user requests `POST /profile/send-code` with a new email different from current
- **THEN** system generates 6-digit verification code
- **AND** system sends email containing the code to the new email address
- **AND** system returns success message

#### Scenario: Rate limiting
- **WHEN** user requests more than 5 verification codes within 10 minutes
- **THEN** system returns error code 429 with message "Too many requests, please try again later"

#### Scenario: Same email
- **WHEN** authenticated user requests `POST /profile/send-code` with the same email as current
- **THEN** system returns error code 400 with message "Email unchanged"

### Requirement: User can change email with verification
The system SHALL allow authenticated users to change their email by providing the verification code sent to the new email.

#### Scenario: Successful email change
- **WHEN** authenticated user requests `PUT /profile/email` with correct verify_code and new_email
- **THEN** system verifies the code is valid and not expired
- **AND** system updates user's email to new_email
- **AND** system returns success with updated profile

#### Scenario: Invalid verification code
- **WHEN** authenticated user requests `PUT /profile/email` with incorrect or expired verification code
- **THEN** system returns error code 400 with message "Invalid or expired verification code"

#### Scenario: Email already registered
- **WHEN** authenticated user requests `PUT /profile/email` with an email that is already registered by another user
- **THEN** system returns error code 400 with message "Email already registered"

### Requirement: User can get avatar URL
The system SHALL generate avatar URL based on user's avatar_type.

#### Scenario: Gravatar type
- **WHEN** user's `avatar_type = 'GRAVATAR'`
- **THEN** system returns Gravatar URL based on user email hash

#### Scenario: Default type
- **WHEN** user's `avatar_type = 'DEFAULT'`
- **THEN** system returns default avatar URL (e.g., generated initials or placeholder image)
