# Email Notification Specification

## ADDED Requirements

### Requirement: System can send verification code email
The system SHALL send verification code emails to user-provided email addresses.

#### Scenario: Send registration verification email
- **WHEN** system needs to send registration verification code
- **THEN** system sends email with subject "TripMap - Email Verification"
- **AND** email contains 6-digit verification code
- **AND** email includes message that code expires in 10 minutes

#### Scenario: Email delivery failure
- **WHEN** email delivery fails due to invalid email address
- **THEN** system logs the error
- **AND** system returns error to user with generic message "Failed to send email"

### Requirement: Verification code expires after 10 minutes
The system SHALL invalidate verification codes after 10 minutes.

#### Scenario: Expired code rejection
- **WHEN** user submits a verification code that has passed its expiry time
- **THEN** system treats it as invalid
- **AND** returns appropriate error message

### Requirement: Verification code is single-use
The system SHALL invalidate a verification code after successful use.

#### Scenario: Reuse prevention
- **WHEN** user attempts to use a verification code that has already been used
- **THEN** system returns error "Verification code already used"
