## ADDED Requirements

### Requirement: Toggle like on a public plan

The system SHALL allow authenticated users to like or unlike a public travel plan via a toggle endpoint. Calling the endpoint when the user has not liked the plan SHALL create a like record and increment the plan's like count by 1. Calling the endpoint when the user has already liked the plan SHALL remove the like record and decrement the plan's like count by 1. The response SHALL return the updated liked state and like count.

#### Scenario: Like a plan for the first time

- **WHEN** a logged-in user sends POST to `/api/travelPlan/{id}/like` for a plan they have not liked
- **THEN** a PlanLike record is created with userId and planId, the plan's likeCount increments by 1, and the response returns `{ liked: true, likeCount: <new count> }`

#### Scenario: Unlike a previously liked plan

- **WHEN** a logged-in user sends POST to `/api/travelPlan/{id}/like` for a plan they have already liked
- **THEN** the existing PlanLike record is deleted, the plan's likeCount decrements by 1, and the response returns `{ liked: false, likeCount: <new count> }`

#### Scenario: Like/unlike operations are atomic

- **WHEN** the toggle operation is performed
- **THEN** the PlanLike record insert/delete and the TravelPlan likeCount update SHALL occur within the same transaction

#### Scenario: Duplicate like prevented at database level

- **WHEN** a concurrent request attempts to create a duplicate like for the same user and plan
- **THEN** the unique constraint on (userId, planId) prevents duplicate records, and the service layer handles the exception gracefully

---

### Requirement: Toggle favorite on a public plan

The system SHALL allow authenticated users to favorite or unfavorite a public travel plan via a toggle endpoint. Calling the endpoint when the user has not favorited the plan SHALL create a favorite record and increment the plan's favorite count by 1. Calling the endpoint when the user has already favorited the plan SHALL remove the favorite record and decrement the plan's favorite count by 1. The response SHALL return the updated favorited state and favorite count.

#### Scenario: Favorite a plan for the first time

- **WHEN** a logged-in user sends POST to `/api/travelPlan/{id}/favorite` for a plan they have not favorited
- **THEN** a PlanFavorite record is created with userId and planId, the plan's favoriteCount increments by 1, and the response returns `{ favorited: true, favoriteCount: <new count> }`

#### Scenario: Unfavorite a previously favorited plan

- **WHEN** a logged-in user sends POST to `/api/travelPlan/{id}/favorite` for a plan they have already favorited
- **THEN** the existing PlanFavorite record is deleted, the plan's favoriteCount decrements by 1, and the response returns `{ favorited: false, favoriteCount: <new count> }`

#### Scenario: Favorite/unfavorite operations are atomic

- **WHEN** the toggle operation is performed
- **THEN** the PlanFavorite record insert/delete and the TravelPlan favoriteCount update SHALL occur within the same transaction

---

### Requirement: Public plan card DTO includes interaction data

The `PublicPlanCardDTO` and `PublicPlanDetailDTO` SHALL include likeCount, favoriteCount, isLiked, and isFavorited fields. The counts SHALL reflect the current state from the TravelPlan entity. The boolean flags SHALL reflect the current user's interaction status.

#### Scenario: Card list shows interaction data for current user

- **WHEN** a logged-in user requests the public plan list via `GET /api/travelPlan/public`
- **THEN** each card in the response includes `likeCount`, `favoriteCount`, `isLiked`, and `isFavorited`, and `isLiked`/`isFavorited` are true only for plans the current user has interacted with

#### Scenario: Plan detail shows interaction data for current user

- **WHEN** a logged-in user requests a public plan detail via `GET /api/travelPlan/public/{id}`
- **THEN** the response includes `likeCount`, `favoriteCount`, `isLiked`, and `isFavorited`

#### Scenario: Unauthenticated access returns default values

- **WHEN** a user without authentication accesses public plan endpoints
- **THEN** `isLiked` and `isFavorited` SHALL be false

---

### Requirement: Frontend displays like and favorite buttons on plan cards

The `PublicPlanCard` component SHALL display like and favorite buttons with current counts. The buttons SHALL visually distinguish liked/favorited states from unliked/unfavorited states. Clicking the buttons SHALL call the corresponding toggle API and update the UI optimistically.

#### Scenario: Card shows like button with count

- **WHEN** a public plan card is rendered
- **THEN** the card displays a like button with the like count, and the button shows a filled icon if the current user has liked it, or an outlined icon otherwise

#### Scenario: Card shows favorite button with count

- **WHEN** a public plan card is rendered
- **THEN** the card displays a favorite button with the favorite count, and the button shows a filled icon if the current user has favorited it, or an outlined icon otherwise

#### Scenario: Clicking like button toggles state

- **WHEN** a user clicks the like button on a public plan card
- **THEN** the API is called, and upon success the button state and count update immediately

#### Scenario: Clicking favorite button toggles state

- **WHEN** a user clicks the favorite button on a public plan card
- **THEN** the API is called, and upon success the button state and count update immediately

---

### Requirement: Frontend displays like and favorite buttons on plan detail page

The `PlanDetail` page SHALL display like and favorite buttons in the header area. Clicking the buttons SHALL toggle the interaction state using the same toggle API endpoints.

#### Scenario: Detail page shows interaction buttons

- **WHEN** a public plan detail page is rendered
- **THEN** like and favorite buttons are visible with current counts and interaction state

#### Scenario: Toggle from detail page updates UI

- **WHEN** a user clicks like or favorite on the plan detail page
- **THEN** the API is called and the button state updates upon success
