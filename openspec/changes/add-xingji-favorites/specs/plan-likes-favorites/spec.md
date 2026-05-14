## MODIFIED Requirements

### Requirement: Public plan card DTO includes interaction data

The `PublicPlanCardDTO` and `PublicPlanDetailDTO` SHALL include likeCount, favoriteCount, isLiked, isFavorited, and favoritedAt fields. The counts SHALL reflect the current state from the TravelPlan entity. The boolean flags SHALL reflect the current user's interaction status. The `favoritedAt` field SHALL be populated with the ISO-format timestamp when the card is returned from the favorites endpoint, and SHALL be null otherwise.

#### Scenario: Card list shows interaction data for current user

- **WHEN** a logged-in user requests the public plan list via `GET /api/travelPlan/public`
- **THEN** each card in the response includes `likeCount`, `favoriteCount`, `isLiked`, and `isFavorited`, and `isLiked`/`isFavorited` are true only for plans the current user has interacted with, and `favoritedAt` is null

#### Scenario: Plan detail shows interaction data for current user

- **WHEN** a logged-in user requests a public plan detail via `GET /api/travelPlan/public/{id}`
- **THEN** the response includes `likeCount`, `favoriteCount`, `isLiked`, and `isFavorited`

#### Scenario: Unauthenticated access returns default values

- **WHEN** a user without authentication accesses public plan endpoints
- **THEN** `isLiked` and `isFavorited` SHALL be false

#### Scenario: favoritedAt populated in favorites context

- **WHEN** a logged-in user requests the favorites list via `GET /api/travelPlan/favorites`
- **THEN** each card's `favoritedAt` contains the ISO datetime string of when the plan was favorited
