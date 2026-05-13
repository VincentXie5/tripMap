## ADDED Requirements

### Requirement: User can browse public travel plans
The system SHALL display all public travel plans in a card grid on the 寻迹 page, ordered by most recently updated.

#### Scenario: View public plans list
- **WHEN** authenticated user navigates to `/xunji`
- **THEN** system displays a paginated grid of public plan cards
- **AND** each card shows: title, date range, route preview, tag-based theme color, day count, location count, creator nickname, and creator avatar

#### Scenario: Empty state
- **WHEN** no public plans exist
- **THEN** system displays an empty state placeholder with message "还没有人分享旅行计划，来做第一个吧！"

#### Scenario: Pagination
- **WHEN** more than 12 public plans exist
- **THEN** system shows "加载更多" button at the bottom of the list
- **AND** clicking it appends the next page of results

### Requirement: User can search and filter public plans
The system SHALL allow users to search public plans by keyword and filter by tag.

#### Scenario: Search by keyword
- **WHEN** user enters a keyword in the search box
- **THEN** system filters plans where title, location name, or remark contains the keyword (case-insensitive)

#### Scenario: Filter by tag
- **WHEN** user selects a tag filter (景点/美食/住宿/交通/购物)
- **THEN** system shows only plans whose daily plans contain at least one item with the selected tag

#### Scenario: Combined search and filter
- **WHEN** user enters a keyword AND selects a tag filter
- **THEN** system applies both conditions simultaneously

### Requirement: User can view public plan detail in read-only mode
The system SHALL allow users to view a public plan's full details on a read-only map page.

#### Scenario: Open plan detail
- **WHEN** user clicks a public plan card
- **THEN** system navigates to `/plan/:id`
- **AND** displays the plan's daily plans in a read-only left panel
- **AND** displays all locations on the right-side Leaflet map
- **AND** hides all edit/create/delete controls

#### Scenario: Return to explore list
- **WHEN** user clicks "← 返回寻迹" or presses browser back
- **THEN** system returns to `/xunji` maintaining previous scroll position and search state

#### Scenario: Private plan access denied
- **WHEN** user attempts to directly access `/plan/:id` for a private plan
- **THEN** system returns error indicating the plan is not publicly accessible

### Requirement: User can view a creator's public plans
The system SHALL allow users to click a creator's avatar or nickname to see all their public plans.

#### Scenario: View creator's plans
- **WHEN** user clicks a creator's avatar or nickname on a plan card or detail page
- **THEN** system filters the 寻迹 page to show only that creator's public plans
- **AND** displays the creator's nickname as a filter indicator

#### Scenario: Creator has no other public plans
- **WHEN** user clicks a creator who has only one public plan
- **THEN** system shows that single plan (no error)

### Requirement: Public plan card shows route preview
The system SHALL generate a route preview string from the plan's daily plan locations.

#### Scenario: Route preview with multiple locations
- **WHEN** a plan has daily plans in multiple locations
- **THEN** the card shows a route preview like "三亚湾 → 亚龙湾 → 蜈支洲岛"
- **AND** maximum 4 location nodes are shown, with "..." for overflow

#### Scenario: Route preview with single location
- **WHEN** a plan has daily plans all in the same location
- **THEN** the card shows that single location without arrows

### Requirement: Public plan card shows tag-based theme color
The system SHALL apply a theme color to each plan card based on the most frequent tag in its daily plans.

#### Scenario: Food-dominant plan
- **WHEN** a plan's daily plans have more 美食(2) tags than any other tag
- **THEN** the card's accent color is orange (#E6A23C)

#### Scenario: No daily plans yet
- **WHEN** a plan has no daily plans
- **THEN** the card uses the default blue accent color (#409EFF)
