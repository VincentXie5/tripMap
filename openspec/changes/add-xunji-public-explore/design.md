## Context

TripMap 目前所有旅行计划仅创建者本人可见。`TravelPlan` 实体尚无 `isPublic` 字段，`GET /api/travelPlan` 只返回当前用户的计划。前端路由仅有 `/login`、`/register` 和登录后的 `Home`、`Profile`。本次变更引入"寻迹"页面，让用户能发现和浏览其他人公开的旅行计划。

## Goals / Non-Goals

**Goals:**
- 用户可将自己的计划设为公开，在计划列表中一键 toggle
- 寻迹页面以卡片流展示所有公开计划，支持关键词搜索和标签筛选
- 点击卡片进入只读地图详情页，复用现有 Home 的地图布局
- 点击创建者头像查看该用户的全部公开计划
- 浏览器前进/后退在寻迹列表和详情页间自然导航

**Non-Goals:**
- 收藏、点赞功能（留待后续）
- 分享链接/二维码生成（留待后续）
- 评论/互动功能（留待后续）
- 公开计划的封面图（当前无图片数据）
- 未登录用户访问（寻迹需登录）

## Decisions

### 1. 数据模型：单字段 Boolean

在 `TravelPlan` 加 `isPublic: Boolean, default false`。不使用枚举（如 PRIVATE/PUBLIC），当前场景下布尔值足够，后续若需扩展（如"仅好友可见"）可再迁移为枚举。

**Alternatives considered:**
- 独立表 `plan_visibility` — 过度设计，当前只有两种状态
- 枚举字段 — 灵活性好但当前无需求，YAGNI

### 2. API 设计

```
PUT  /api/travelPlan/{id}/visibility    ← 切换 isPublic
GET  /api/travelPlan/public             ← 分页查询公开计划
GET  /api/travelPlan/public/{id}        ← 单个公开计划详情（含 dailyPlans）
GET  /api/travelPlan/public/user/{id}   ← 某用户的公开计划
```

- 公开接口需要认证（携带 JWT），但不校验 owner
- 分页参数：`page`, `size`, 可选 `keyword`, 可选 `tag`
- 返回 DTO 包含 creator 信息（nickname, avatarUrl），避免前端二次查询

### 3. 路线预览生成

后端查询公开计划时，LEFT JOIN `daily_plan` 表，按 `plan_date` 和 `sort_order` 排序，提取 `location` 字段的前缀（逗号或空格前的地名）去重后拼接。示例：`daily_plans` 中有 "三亚湾海滩"、"亚龙湾" → 路线预览 "三亚湾 → 亚龙湾"。

最多显示 4 个节点，超出用 "..." 省略。

### 4. 卡片主题色

根据计划内 daily plans 出现最多的 tag 决定：
- 景点(1) → `#409EFF` (蓝)
- 美食(2) → `#E6A23C` (橙)
- 住宿(3) → `#67C23A` (绿)
- 交通(4) → `#909399` (灰)
- 购物(5) → `#F56C6C` (红)
- 其他(0) / 无数据 → `#409EFF` (默认蓝)

### 5. 只读详情页复用策略

`/plan/:id` 路由使用与 Home 相同的左右分栏布局，通过 prop 或路由 meta 控制编辑功能隐藏：
- PlanList 只读模式：隐藏创建、编辑、删除按钮，隐藏公开 toggle（自己的才能在详情页改）
- DailyPlanList 只读模式：隐藏添加、编辑、删除按钮
- 左上角显示"← 返回寻迹"链接，用 `router.back()` 实现

**Alternatives considered:**
- 新建独立 `PlanDetail.vue` — 大量重复代码，维护成本高
- 弹窗/Dialog — 限制了地图的展示空间，体验不好

### 6. 搜索实现

后端 SQL 层面实现：`WHERE is_public = true AND (title LIKE %keyword% OR location LIKE %keyword% OR remark LIKE %keyword%)`。使用 JPA `@Query` 或 Specification。不引入 Elasticsearch 等额外依赖。

### 7. 前端路由

```
/ (AppLayout, requiresAuth)
  ├── /             Home（我的计划）
  ├── /xunji        寻迹（公开计划浏览）
  ├── /plan/:id     只读地图详情
  └── /profile      个人中心
```

`/xunji` 在 AppLayout 的 children 中，共享顶部导航栏。

## Risks / Trade-offs

- **性能**: 公开计划数量增长后，全表扫描模糊搜索会变慢 → 初期数据量小，后续可加 MySQL 全文索引或切换搜索引擎
- **缓存一致性**: 用户 toggle 公开状态后，寻迹列表需刷新 → 前端每次进入寻迹页面时重新请求即可，暂不引入缓存
- **数据安全**: 需确保 `GET /api/travelPlan/public/{id}` 只返回 `isPublic=true` 的计划，防止通过直接访问 URL 偷看私有计划
