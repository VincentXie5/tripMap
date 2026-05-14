## Context

`add-plan-likes-favorites` 已实现收藏 toggle 功能，`PlanFavorite` 实体和 `PlanFavoriteRepository` 已存在。用户可对公开计划收藏/取消收藏，但缺乏集中浏览已收藏计划的入口。本次变更在此基础上新增"星迹"页面。

导航栏当前结构："我的计划"(`/`)、"寻迹"(`/xunji`)。"星迹"(`/favorites`) 作为第三个标签加入。

## Goals / Non-Goals

**Goals:**
- 独立的"星迹"页面，分页展示当前用户所有已收藏计划
- 支持按关键词（标题、地点）搜索已收藏计划
- 展示"收藏于 X 天前"时间信息
- 取消收藏后卡片实时从列表移除
- 卡片列表复用 `PublicPlanCard` 组件，公共列表场景不受影响

**Non-Goals:**
- 不修改寻迹页面的筛选逻辑
- 不在星迹页面中支持标签筛选或创建者筛选（保持简洁）
- 不添加"批量取消收藏"功能

## Decisions

### 1. 查询方式：两段式查询

```
Step 1: planFavoriteRepository.findByUserIdOrderByCreatedAtDesc(userId)
        → 获取全部 PlanFavorite 记录（仅 id + planId + createdAt，不含完整 TravelPlan）
Step 2: 提取所有 planId + 收藏时间 Map<planId, createdAt>
        → travelPlanRepository.findByIdInAndIsPublicTrue(planIds, keyword, pageable)
        → 分页返回 TravelPlan，组装 DTO 时从 Map 取 favoritedAt
```

**为什么先查全部 PlanFavorite？** 收藏列表需要按收藏时间倒序排列，但最终分页的是 TravelPlan。如果只在 PlanFavorite 上分页（12 条），IN 查 TravelPlan 后顺序可能不一致——需要按收藏时间重排。先查出全部收藏的 planId，在 TravelPlan 分页查询中用 IN 保持 planId 列表顺序即可。

**为什么不用 JOIN JPQL？** 两段查询各司其职：PlanFavorite 提供收藏时间和 planId 列表，TravelPlan 走已有普适性查询（支持关键词搜索）。分开更灵活，且 PlanFavorite 数据量小（单用户收藏数有限）。

### 2. favoritedAt 字段：DTO 级复用

`PublicPlanCardDTO` 新增 `favoritedAt: String`（nullable）。公开列表场景下保持 null，收藏列表场景填充。前端 `PublicPlanCard` 类型新增可选字段 `favoritedAt?: string`。卡片组件中仅当该字段存在时显示"收藏于 X 天前"文本。

### 3. 页面架构：独立 Favorites.vue

样式和交互模式参考 `Xunji.vue`，但去掉标签筛选和创建者筛选。组件结构：

```
Favorites.vue
├── 标题区域："星迹" + 副标题
├── 搜索框（防抖 400ms）
├── 空状态（无收藏时："还没有收藏任何旅行计划" → 链接跳转寻迹）
├── 卡片网格（复用 PublicPlanCard）
│   ├── 点赞按钮（保留）
│   ├── 收藏按钮（点击 → 取消收藏 → 卡片消失）
│   └── "收藏于 X 天前"（favoritedAt 存在时显示）
└── 加载更多按钮
```

### 4. 取消收藏后从列表移除

`handleFavorite` 调用 toggle API 成功后，直接从 `cards` 数组中 splice 该卡片。这比重新请求列表响应更快，用户体验更好。

### 5. 导航标签路由激活

`AppLayout.vue` 中新增标签，高亮条件：`route.path.startsWith('/favorites')`。与寻迹标签的 `startsWith('/xunji') || startsWith('/plan/')` 互斥。

## Risks / Trade-offs

- **[全部收藏一次查出] 如果用户收藏了几百个计划，第一步查全部 PlanFavorite 会变慢** → 单用户收藏数通常在几十以内，且 PlanFavorite 表只有 id/userId/planId/createdAt 四列，数据量极小。如果未来达到规模瓶颈，可改为在 PlanFavorite 上分页再顺序保留。
- **[DTO 向后兼容] PublicPlanCardDTO 新增字段** → `favoritedAt` 为 nullable 字符串，公开列表返回 null，前端判断 `v-if`，不影响现有逻辑。
