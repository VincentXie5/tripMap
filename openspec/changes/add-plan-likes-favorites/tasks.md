## 1. 数据模型

- [x] 1.1 创建 `PlanLike` 实体（`entity/PlanLike.java`），包含 id、userId、planId、createdAt，添加 `@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "plan_id"}))`
- [x] 1.2 创建 `PlanFavorite` 实体（`entity/PlanFavorite.java`），结构与 PlanLike 相同
- [x] 1.3 在 `TravelPlan` 实体新增 `likeCount` 和 `favoriteCount` 字段（INT，默认 0）
- [x] 1.4 创建 `PlanLikeRepository` 接口，包含 `findByUserIdAndPlanIdIn`、`findByUserIdAndPlanId`、`existsByUserIdAndPlanId` 查询方法
- [x] 1.5 创建 `PlanFavoriteRepository` 接口，方法同上

## 2. 后端 API

- [x] 2.1 在 `TravelPlanService` 接口中新增 `toggleLike` 和 `toggleFavorite` 方法签名，返回包含操作后状态和计数的 Map
- [x] 2.2 在 `TravelPlanServiceImpl` 中实现 `toggleLike`：查询是否存在 → 存在则删除并计数-1，不存在则插入并计数+1，所有操作在 `@Transactional` 中
- [x] 2.3 在 `TravelPlanServiceImpl` 中实现 `toggleFavorite`，逻辑同上
- [x] 2.4 修改 `PublicPlanCardDTO`，新增 `likeCount`、`favoriteCount`、`isLiked`、`isFavorited` 四个字段
- [x] 2.5 修改 `PublicPlanDetailDTO`，新增同样的四个字段
- [x] 2.6 修改 `getPublicPlans` 方法：批量查询当前用户的 PlanLike 和 PlanFavorite，组装 DTO 时标记 isLiked/isFavorited，从 TravelPlan 读取 likeCount/favoriteCount
- [x] 2.7 修改 `getPublicPlanDetail` 方法：查询当前用户对该计划的 PlanLike 和 PlanFavorite 状态，组装 DTO
- [x] 2.8 在 `TravelPlanController` 中新增 `POST /api/travelPlan/{id}/like` 和 `POST /api/travelPlan/{id}/favorite` 端点

## 3. 前端类型与 API

- [x] 3.1 在 `types/api.ts` 的 `PublicPlanCard` 和 `PublicPlanDetail` 接口中新增 `likeCount`、`favoriteCount`、`isLiked`、`isFavorited` 字段
- [x] 3.2 在 `travelApi.ts` 中新增 `toggleLike(planId: number)` 和 `toggleFavorite(planId: number)` 函数

## 4. 前端 UI

- [x] 4.1 修改 `PublicPlanCard.vue`：在卡片底部新增点赞按钮和收藏按钮，区分已互动/未互动状态，点击 emit 事件
- [x] 4.2 修改 `Xunji.vue`：处理卡片的 like/favorite 事件，调用 API 后更新 cards 数组
- [x] 4.3 修改 `PlanDetail.vue`：在详情头部新增点赞和收藏按钮，直接调用 API 更新状态

## 5. 验证

- [ ] 5.1 启动后端和前端，验证卡片列表展示点赞数和收藏数
- [ ] 5.2 验证点赞/取消点赞 toggle 功能正常，数字实时更新
- [ ] 5.3 验证收藏/取消收藏 toggle 功能正常，数字实时更新
- [ ] 5.4 验证详情页点赞和收藏功能正常
- [ ] 5.5 验证 `isLiked`/`isFavorited` 状态在不同用户间正确隔离
