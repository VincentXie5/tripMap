## 1. Backend - Error Codes

- [x] 1.1 Add new error codes to UserCode: NICKNAME_ALREADY_EXISTS, EMAIL_ALREADY_EXISTS, INVALID_OLD_PASSWORD, EMAIL_NOT_CHANGED

## 2. Backend - DTOs

- [x] 2.1 Create ProfileResponse DTO with id, email, nickname, avatar_type, is_active, created_at, avatar_url
- [x] 2.2 Create AvatarUpdateRequest with avatar_type field
- [x] 2.3 Create NicknameUpdateRequest with nickname field
- [x] 2.4 Create PasswordChangeRequest with old_password and new_password fields
- [x] 2.5 Create EmailChangeRequest with email and verify_code fields
- [x] 2.6 Create SendCodeRequest with email field

## 3. Backend - UserService

- [x] 3.1 Add getProfile(userId) method returning UserResponse with avatarUrl
- [x] 3.2 Add updateAvatar(userId, avatarType) method
- [x] 3.3 Add updateNickname(userId, newNickname) method
- [x] 3.4 Add changePassword(userId, oldPassword, newPassword) method
- [x] 3.5 Add sendEmailChangeCode(userId, newEmail) method
- [x] 3.6 Add changeEmail(userId, newEmail, verifyCode) method
- [x] 3.7 Add generateAvatarUrl(user) helper method

## 4. Backend - ProfileController

- [x] 4.1 Add GET /profile endpoint
- [x] 4.2 Add PUT /profile/avatar endpoint
- [x] 4.3 Add PUT /profile/nickname endpoint
- [x] 4.4 Add PUT /profile/password endpoint
- [x] 4.5 Add POST /profile/send-code endpoint
- [x] 4.6 Add PUT /profile/email endpoint

## 5. Backend - Update AuthController

- [x] 5.1 Update GET /auth/me to return avatar_url in UserResponse

## 6. Frontend - API Layer

- [x] 6.1 Create src/api/profile.ts with getProfile, updateAvatar, updateNickname, changePassword, sendEmailCode, changeEmail functions

## 7. Frontend - Profile Page

- [x] 7.1 Create src/views/Profile.vue with profile form
- [x] 7.2 Add avatar display and selection component
- [x] 7.3 Add nickname input with validation
- [x] 7.4 Add email display with verification status and change flow
- [x] 7.5 Add password change form with old/new password fields

## 8. Frontend - Routing & Navigation

- [x] 8.1 Add /profile route to router configuration
- [x] 8.2 Add user name link in header navigation that navigates to profile

## 9. Testing

- [x] 9.1 Implementation complete - ready for testing
- [x] 9.2 Frontend implementation complete - ready for testing