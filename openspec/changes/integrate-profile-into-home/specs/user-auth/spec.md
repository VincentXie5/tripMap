## MODIFIED Requirements

### Requirement: User can get avatar URL
The system SHALL generate avatar URL based on user's avatar_type, and SHALL display the avatar in the application header.

#### Scenario: Gravatar type
- **WHEN** user's `avatar_type = 'GRAVATAR'`
- **THEN** system returns Gravatar URL based on user email hash

#### Scenario: Default type
- **WHEN** user's `avatar_type = 'DEFAULT'`
- **THEN** system returns default avatar URL

#### Scenario: Avatar displayed in header
- **WHEN** user is logged in
- **THEN** the header displays the user's avatar as a 36px circular icon
- **AND** if no avatar image is available, the first character of the user's nickname is shown instead
- **AND** clicking the avatar navigates to the profile settings page
