## MODIFIED Requirements

### Requirement: User can get avatar URL
The system SHALL generate avatar URL based on user's avatar_type.

#### Scenario: Gravatar type
- **WHEN** user's `avatar_type = 'GRAVATAR'`
- **THEN** system returns Gravatar URL based on user email hash

#### Scenario: Default type
- **WHEN** user's `avatar_type = 'DEFAULT'`
- **THEN** system returns inline SVG data URI with user's initial letter

#### Scenario: Custom type
- **WHEN** user's `avatar_type = 'CUSTOM'`
- **THEN** system returns proxy URL `/api/files/avatars/{userId}.{avatarExt}`
