## 1. 后端数据层

- [x] 1.1 在 `PlanFavoriteRepository` 中新增 `findByUserIdOrderByCreatedAtDesc(Long userId)` 方法
- [x] 1.2 修改 `PublicPlanCardDTO`，新增 `favoritedAt` 字段（String，nullable）
- [x] 1.3 在 `TravelPlanRepository` 中新增 `findByIdInAndIsPublicTrue(List<Long> ids, Pageable)` 分页方法，支持按 id 列表和 isPublic=true 查询

## 2. 后端 API

- [x] 2.1 在 `TravelPlanService` 接口中新增 `Page<PublicPlanCardDTO> getFavoritePlans(Long userId, String keyword, int page, int size)` 方法签名
- [x] 2.2 在 `TravelPlanServiceImpl` 中实现 `getFavoritePlans`：查全部 PlanFavorite → 构建 planId→createdAt Map → 分页查 TravelPlan(IN+keyword) → 组装 DTO（isFavorited=true, favoritedAt=收藏时间）
- [x] 2.3 在 `TravelPlanController` 中新增 `GET /api/travelPlan/favorites` 端点

## 3. 前端类型与 API

- [x] 3.1 在 `types/api.ts` 的 `PublicPlanCard` 和 `PublicPlanDetail` 接口中新增 `favoritedAt?: string` 字段
- [x] 3.2 在 `travelApi.ts` 中新增 `getFavoritePlans(params: { page?, size?, keyword? })` 函数

## 4. 前端页面与导航

- [x] 4.1 新增 `router/index.ts` 中的 `/favorites` 路由（在 AppLayout children 中），指向新的 `Favorites.vue`
- [x] 4.2 修改 `AppLayout.vue`，导航栏新增"星迹"标签，`/favorites` 时高亮
- [x] 4.3 创建 `views/Favorites.vue` 页面：搜索框（防抖 400ms）+ 卡片网格（复用 PublicPlanCard）+ 分页加载更多 + 空状态"还没有收藏任何旅行计划"
- [x] 4.4 在 `PublicPlanCard.vue` 中：当 `favoritedAt` 有值时，在卡片日期行附近显示"收藏于 X 天前"
- [x] 4.5 在 `Favorites.vue` 中实现取消收藏后从列表移除卡片的逻辑

## 5. 验证

- [ ] 5.1 验证星迹页面正常展示已收藏计划列表，卡片上显示"收藏于 X 天前"
- [ ] 5.2 验证搜索功能：按关键词过滤已收藏计划
- [ ] 5.3 验证取消收藏后卡片实时从列表移除
- [ ] 5.4 验证空收藏列表时显示引导信息并链接到寻迹页面
- [ ] 5.5 验证导航栏标签高亮在 `/favorites` 时正确
