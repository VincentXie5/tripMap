## ADDED Requirements

### Requirement: App shell with persistent header
The system SHALL render a shared header and a content area that switches between sub-routes on the main page.

#### Scenario: User visits home page
- **WHEN** user navigates to `/`
- **THEN** the shared header is displayed with Logo "TripMap" on the left
- **AND** user avatar and nickname are displayed on the right
- **AND** the home content (plan list, map) is displayed in the content area

#### Scenario: User visits profile page
- **WHEN** user navigates to `/profile`
- **THEN** the shared header remains visible unchanged
- **AND** the profile settings content replaces the home content in the content area

#### Scenario: User navigates back from profile to home
- **WHEN** user is on `/profile` and clicks the Logo
- **THEN** the content area switches back to home content
- **AND** the header remains unchanged

### Requirement: Header displays user avatar
The system SHALL display a circular avatar icon next to the user's nickname in the header.

#### Scenario: User has avatar image
- **WHEN** the user has an avatar image URL
- **THEN** a 36px circular image is displayed showing the user's avatar

#### Scenario: User has no avatar image
- **WHEN** the user does not have an avatar image
- **THEN** a 36px circular placeholder is displayed showing the first character of the user's nickname

### Requirement: Header provides navigation
The system SHALL allow users to navigate between home and profile from the header.

#### Scenario: Click logo to go home
- **WHEN** user clicks the "TripMap" logo in the header
- **THEN** the app navigates to `/`

#### Scenario: Click avatar or username to go to profile
- **WHEN** user clicks the avatar icon or username in the header
- **THEN** the app navigates to `/profile`

### Requirement: Header provides logout
The system SHALL provide a logout button in the header.

#### Scenario: User logs out
- **WHEN** user clicks the "退出" (logout) button in the header
- **THEN** the user is logged out and redirected to `/login`
