## ADDED Requirements

### Requirement: Authenticated user can view favorited plans

The system SHALL provide a paginated endpoint that returns the current user's favorited public travel plans, ordered by the time they were favorited (most recent first). Each card SHALL include the favorite timestamp (`favoritedAt`) and SHALL have `isFavorited` set to true.

#### Scenario: User retrieves their favorites list

- **WHEN** a logged-in user sends GET to `/api/travelPlan/favorites` with optional `page`, `size`, and `keyword` parameters
- **THEN** the response is a paginated list of `PublicPlanCardDTO` items, each with `isFavorited: true` and a non-null `favoritedAt` timestamp

#### Scenario: Empty favorites list

- **WHEN** a logged-in user who has not favorited any plans sends GET to `/api/travelPlan/favorites`
- **THEN** the response returns an empty page with `content: [], totalElements: 0, empty: true`

#### Scenario: Plans deleted or made private are excluded

- **WHEN** a plan that the user favorited becomes private or is deleted
- **THEN** it SHALL NOT appear in the favorites endpoint response

---

### Requirement: Search favorited plans by keyword

The favorites endpoint SHALL support an optional `keyword` parameter that filters plans by matching the keyword against the plan title or associated daily plan locations.

#### Scenario: Search by title keyword

- **WHEN** a user requests favorites with `keyword=三亚`
- **THEN** only favorited plans whose title contains "三亚" are returned

#### Scenario: Search by location keyword

- **WHEN** a user requests favorites with `keyword=海滩`
- **THEN** favorited plans whose daily plan locations contain "海滩" are included in the results

---

### Requirement: PublicPlanCardDTO includes optional favorite timestamp

The `PublicPlanCardDTO` SHALL include a nullable `favoritedAt` field. In the favorites endpoint, this field SHALL be populated with the ISO-format timestamp from the `PlanFavorite.createdAt` record. In the public plans endpoint, this field SHALL remain null.

#### Scenario: favoritedAt populated in favorites list

- **WHEN** the favorites endpoint returns a card
- **THEN** `favoritedAt` contains the ISO datetime string of when the user favorited the plan

#### Scenario: favoritedAt is null in public list

- **WHEN** the public plans endpoint returns a card
- **THEN** `favoritedAt` is null

---

### Requirement: "星迹" page displays favorited plans

The frontend SHALL provide a "星迹" (`/favorites`) page that displays the current user's favorited plans in a card grid with a search box. The navigation bar SHALL include a "星迹" tab that is active when on this page.

#### Scenario: Page renders favorited plan cards

- **WHEN** a logged-in user navigates to `/favorites`
- **THEN** a paginated card grid of their favorited plans is displayed, reusing the `PublicPlanCard` component

#### Scenario: Card shows "favorited X days ago"

- **WHEN** a card with a non-null `favoritedAt` is rendered
- **THEN** the card displays a human-readable relative time (e.g., "收藏于 3 天前")

#### Scenario: Search within favorites

- **WHEN** the user types a keyword in the search box with debounce
- **THEN** the favorites list filters to matching plans

#### Scenario: Unfavorite removes card from list

- **WHEN** a user clicks the favorite (⭐) button on a card in the favorites page
- **THEN** the toggle API is called, and upon success the card is removed from the displayed list

#### Scenario: Navigation tab is active

- **WHEN** the user is on `/favorites`
- **THEN** the "星迹" tab in the navigation bar is highlighted as active

#### Scenario: Empty state with guidance

- **WHEN** a user with no favorites visits `/favorites`
- **THEN** a message "还没有收藏任何旅行计划" is displayed with a link to navigate to the 寻迹 page
