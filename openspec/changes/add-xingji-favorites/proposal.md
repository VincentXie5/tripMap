## Why

用户已经可以收藏公开旅行计划，但没有统一的入口来查看和管理已收藏的计划。收藏后只能通过寻迹页面一个个翻找，体验割裂。作为社交互动闭环的重要一环，"星迹"页面让用户集中浏览和搜索自己标记过的计划，提升收藏功能的价值。

## What Changes

- 新增"星迹"页面（`/favorites`），展示当前用户收藏的所有公开计划
- 支持关键词搜索已收藏计划（按标题和地点匹配）
- 收藏列表展示"收藏于 X 天前"时间信息
- 导航栏新增"星迹"标签入口
- 新增 `GET /api/travelPlan/favorites` 接口，分页返回已收藏计划
- `PublicPlanCardDTO` 新增 `favoritedAt` 字段

## Capabilities

### New Capabilities
- `xingji-favorites`: 用户通过"星迹"页面浏览、搜索已收藏的公开旅行计划，查看收藏时间，取消收藏后卡片实时从列表移除

### Modified Capabilities
- `plan-likes-favorites`: `PublicPlanCardDTO` 新增 `favoritedAt` 字段，在收藏列表场景下填充收藏时间

## Impact

- **后端**: `PlanFavoriteRepository` 新增分页查询方法，`TravelPlanService` 新增 `getFavoritePlans`，`TravelPlanController` 新增端点
- **前端**: 新增 `Favorites.vue` 页面，`AppLayout.vue` 导航栏新增标签，`router/index.ts` 新增路由，`types/api.ts` 新增字段
- **依赖**: 基于已实现的 `add-plan-likes-favorites` 中的 `PlanFavorite` 实体和 Repository
