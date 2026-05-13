## 1. Backend: Data Model

- [x] 1.1 Add `isPublic` field (Boolean, default false) to TravelPlan entity
- [x] 1.2 Add `is_public` column to travel_plan table (via Hibernate ddl-auto or manual SQL)

## 2. Backend: Repository

- [x] 2.1 Add `findByIsPublicTrue` with Pageable support to TravelPlanRepository
- [x] 2.2 Add `@Query` method for keyword search across title, location, remark with pagination
- [x] 2.3 Add `findByUserIdAndIsPublicTrue` with Pageable for creator filter

## 3. Backend: DTO

- [x] 3.1 Create `PublicPlanCardDTO` (id, title, startDate, endDate, creatorNickname, creatorAvatarUrl, routePreview, dominantTag, dayCount, locationCount)
- [x] 3.2 Create `PublicPlanDetailDTO` (full plan info + dailyPlans list + creator info)

## 4. Backend: Service

- [x] 4.1 Add `getPublicPlans(keyword, tag, page, size)` method to TravelPlanService
- [x] 4.2 Add `getPublicPlanDetail(planId)` method returning plan + dailyPlans, throwing if plan is not public
- [x] 4.3 Add `getPublicPlansByUser(userId, page, size)` method
- [x] 4.4 Add `toggleVisibility(planId, userId)` method with ownership check
- [x] 4.5 Implement route preview generation from daily plan locations
- [x] 4.6 Implement dominant tag calculation from daily plans
- [x] 4.7 Implement keyword search logic (title, location name, remark LIKE matching)

## 5. Backend: Controller

- [x] 5.1 Add `GET /api/travelPlan/public` endpoint with pagination and optional keyword/tag params
- [x] 5.2 Add `GET /api/travelPlan/public/{id}` endpoint for plan detail
- [x] 5.3 Add `GET /api/travelPlan/public/user/{userId}` endpoint for creator filter
- [x] 5.4 Add `PUT /api/travelPlan/{id}/visibility` endpoint with AuthenticationPrincipal

## 6. Backend: Security

- [x] 6.1 Ensure public plan endpoints are accessible to all authenticated users (not restricted by owner)
- [x] 6.2 Verify that private plan detail endpoint returns error for non-owners

## 7. Frontend: API

- [x] 7.1 Add `getPublicPlans(params)` API call to travelApi.ts
- [x] 7.2 Add `getPublicPlanDetail(id)` API call
- [x] 7.3 Add `getPublicPlansByUser(userId)` API call
- [x] 7.4 Add `togglePlanVisibility(planId)` API call
- [x] 7.5 Update Plan type in types/api.ts to include `isPublic` and new fields

## 8. Frontend: PlanList Toggle

- [x] 8.1 Add visibility toggle switch to each plan item in PlanList.vue
- [x] 8.2 Wire toggle to API call and refresh on success

## 9. Frontend: Xunji Page

- [x] 9.1 Create `Xunji.vue` view with card grid layout
- [x] 9.2 Create `PublicPlanCard.vue` component with route preview, tags, creator info, theme color
- [x] 9.3 Implement search input with debounced keyword filtering
- [x] 9.4 Implement tag filter chips (景点/美食/住宿/交通/购物)
- [x] 9.5 Implement pagination ("加载更多" button)
- [x] 9.6 Implement creator click → filter by user
- [x] 9.7 Implement card click → navigate to `/plan/:id`
- [x] 9.8 Implement empty state placeholder

## 10. Frontend: Plan Detail (Read-only)

- [x] 10.1 Create `/plan/:id` route component (reuse Home layout pattern)
- [x] 10.2 Pass read-only mode to PlanList and DailyPlanList components
- [x] 10.3 Add "← 返回寻迹" navigation header
- [x] 10.4 Display creator info header (avatar + nickname, clickable)
- [x] 10.5 Hide edit/create/delete controls when plan owner !== current user
- [x] 10.6 Handle error when plan is private/not found

## 11. Frontend: Router

- [x] 11.1 Add `/xunji` route under AppLayout children
- [x] 11.2 Add `/plan/:id` route under AppLayout children
- [x] 11.3 Add "寻迹" navigation tab in AppLayout header
- [x] 11.4 Ensure auth guard applies (all routes require authentication)

## 12. Integration & Polish

- [x] 12.1 End-to-end flow test: create plan → toggle public → view on 寻迹 → open detail → back
- [x] 12.2 Test search and filter combinations
- [x] 12.3 Test creator filter flow
- [x] 12.4 Test private plan access returns error
- [x] 12.5 Typecheck with `npx vue-tsc -b`
