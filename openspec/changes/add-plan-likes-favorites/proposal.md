## Why

寻迹页面展示公开旅行计划，但用户只能浏览，无法表达对计划的认可或保存感兴趣的计划供日后参考。无互动的社区缺乏社交驱动力，用户粘性低。添加点赞和收藏功能让用户能够参与互动，为后续的社交功能和个性化推荐奠定基础。

## What Changes

- 新增点赞功能：用户可以对公开计划点赞或取消点赞，卡片和详情页展示点赞数及当前用户状态
- 新增收藏功能：用户可以对公开计划收藏或取消收藏，卡片和详情页展示收藏数及当前用户状态
- 公开计划卡片 DTO 新增 likeCount、favoriteCount、isLiked、isFavorited 字段
- 公开计划详情 DTO 新增同样 4 个字段
- 寻迹页面和计划详情页面展示点赞/收藏按钮与计数

## Capabilities

### New Capabilities
- `plan-likes-favorites`: 用户对公开旅行计划进行点赞和收藏互动，包括点赞/取消点赞、收藏/取消收藏、在卡片列表和详情页查看互动数据

### Modified Capabilities
<!-- 无现有 capability 需要修改 —— 此为全新功能 -->

## Impact

- **数据模型**: 新增 `plan_like` 和 `plan_favorite` 两张表，`travel_plan` 表新增 `like_count` 和 `favorite_count` 冗余字段
- **后端 API**: `TravelPlanController` 新增 2 个 toggle 端点，`PublicPlanCardDTO` 和 `PublicPlanDetailDTO` 各新增 4 个字段
- **前端**: `PublicPlanCard.vue`、`Xunji.vue`、`PlanDetail.vue` 新增互动 UI，`types/api.ts` 和 `travelApi.ts` 新增类型与 API 函数
- **无外部依赖变更**
