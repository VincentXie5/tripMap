# User Auth Specification - Delta

## MODIFIED Requirements

### Requirement: User can get current user info
The system SHALL allow authenticated users to retrieve their own profile information.

#### Scenario: Get own profile
- **WHEN** authenticated user requests their profile
- **THEN** system returns user info (id, email, nickname, avatar_url, is_active, created_at)

#### Scenario: Unauthenticated request
- **WHEN** unauthenticated user requests profile
- **THEN** system returns error code 401 with message "Unauthorized"

### Requirement: User can get avatar URL
The system SHALL generate avatar URL based on user's avatar_type.

#### Scenario: Gravatar type
- **WHEN** user's `avatar_type = 'GRAVATAR'`
- **THEN** system returns Gravatar URL based on user email hash

#### Scenario: Default type
- **WHEN** user's `avatar_type = 'DEFAULT'`
- **THEN** system returns default avatar URL (generated initials with background color)

## ADDED Requirements

### Requirement: User can change password
The system SHALL allow authenticated users to change their password by verifying the old password first.

#### Scenario: Successful password change
- **WHEN** authenticated user submits correct old_password and valid new_password (8-20 chars)
- **THEN** system updates password to new password hash
- **AND** system returns success message

#### Scenario: Wrong old password
- **WHEN** authenticated user submits incorrect old_password
- **THEN** system returns error code 401 with message "Invalid old password"

#### Scenario: New password too short
- **WHEN** authenticated user submits new_password less than 8 characters
- **THEN** system returns error code 400 with message "Password must be at least 8 characters"

### Requirement: User can update nickname
The system SHALL allow authenticated users to update their nickname.

#### Scenario: Successful nickname update
- **WHEN** authenticated user submits new unique nickname
- **THEN** system updates user's nickname
- **AND** system returns updated profile

#### Scenario: Nickname already taken
- **WHEN** authenticated user submits a nickname that already exists
- **THEN** system returns error code 400 with message "Nickname already taken"

### Requirement: User can update avatar type
The system SHALL allow authenticated users to update their avatar type.

#### Scenario: Update avatar type
- **WHEN** authenticated user submits valid avatar_type (GRAVATAR or DEFAULT)
- **THEN** system updates user's avatar_type
- **AND** system returns updated profile with new avatar_url

#### Scenario: Invalid avatar type
- **WHEN** authenticated user submits invalid avatar_type
- **THEN** system returns error code 400 with message "Invalid avatar type"

### Requirement: User can change email with verification
The system SHALL allow authenticated users to change their email address by verifying with a code sent to the new email.

#### Scenario: Successful email change
- **WHEN** authenticated user submits valid verify_code and new_email
- **THEN** system verifies the code is valid and not expired
- **AND** system updates user's email to new_email
- **AND** system returns updated profile

#### Scenario: Invalid verification code
- **WHEN** authenticated user submits incorrect or expired verification code
- **THEN** system returns error code 400 with message "Invalid or expired verification code"

#### Scenario: Email already registered
- **WHEN** authenticated user submits an email that is already registered by another user
- **THEN** system returns error code 400 with message "Email already registered"
