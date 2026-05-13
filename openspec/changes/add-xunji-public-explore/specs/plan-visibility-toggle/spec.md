## ADDED Requirements

### Requirement: User can toggle travel plan visibility
The system SHALL allow users to switch a travel plan between public and private directly from the plan list.

#### Scenario: Make plan public
- **WHEN** user toggles the visibility switch to "公开" on their private plan
- **THEN** system sets `isPublic = true` on the plan
- **AND** the plan becomes visible on the 寻迹 page for all users

#### Scenario: Make plan private
- **WHEN** user toggles the visibility switch to "私有" on their public plan
- **THEN** system sets `isPublic = false` on the plan
- **AND** the plan is no longer visible on the 寻迹 page

#### Scenario: Default visibility
- **WHEN** user creates a new travel plan
- **THEN** the plan's `isPublic` defaults to `false` (private)

#### Scenario: Only owner can toggle
- **WHEN** a user attempts to toggle visibility of a plan they do not own
- **THEN** system returns error code 403

### Requirement: Visibility toggle UI in plan list
The system SHALL display a toggle switch for each plan in the user's plan list.

#### Scenario: Toggle shows current state
- **WHEN** user views their plan list
- **THEN** each plan shows a switch indicating its current public/private state
- **AND** private is the default position (off)

#### Scenario: Toggle is not shown for other users' plans
- **WHEN** user views a public plan detail page for someone else's plan
- **THEN** the visibility toggle is not displayed
