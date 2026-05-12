## 1. Create shared AppLayout

- [x] 1.1 Create `src/views/AppLayout.vue` with shared Header (Logo + avatar + username + logout button)
- [x] 1.2 Fetch avatar URL in AppLayout via `getProfile()` and store in local `ref`
- [x] 1.3 Add `<router-view />` for sub-route content area below Header

## 2. Refactor Home.vue

- [x] 2.1 Remove the inline `.header` block (Logo, user-info, logout) from template
- [x] 2.2 Remove `handleLogout`, `authStore`, and `router` references from script that are only used for header
- [x] 2.3 Adjust `.home-container` to fill remaining height (Header now provided by AppLayout)

## 3. Refactor Profile.vue

- [x] 3.1 Remove `.back-button` div from template
- [x] 3.2 Replace `.profile-container` wrapper with a simpler container matching the content area layout
- [x] 3.3 Remove `useRouter` and `router` usage related to back navigation

## 4. Update router structure

- [x] 4.1 Add `AppLayout` as parent route with path `/`, nesting `Home` as default child (`path: ''`)
- [x] 4.2 Nest `Profile` as child route (`path: 'profile'`) under AppLayout
- [x] 4.3 Keep Login and Register as top-level routes (outside AppLayout)

## 5. Update App.vue

- [x] 5.1 Ensure `App.vue` renders `<router-view />` (no changes needed if still correct after route restructure)

## 6. Verify

- [x] 6.1 Run `cd travel-plan-frontend && npx vue-tsc -b` and fix any type errors
- [ ] 6.2 Manually test: navigate `/` → `/profile` → `/` and verify header persistence
- [ ] 6.3 Manually test: click Logo navigates home, click avatar/username navigates to profile
- [ ] 6.4 Manually test: logout redirects to login, login redirects to home
