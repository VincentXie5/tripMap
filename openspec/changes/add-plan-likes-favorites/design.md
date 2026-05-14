## Context

寻迹页面目前只读展示公开旅行计划卡片，无用户互动能力。`TravelPlan` 实体仅有基础字段，`PublicPlanCardDTO` 不包含互动数据。前端 `Xunji.vue` 通过 `GET /api/travelPlan/public` 分页获取卡片列表。

寻迹页面需要登录才能访问（路由守卫 `requiresAuth`），因此所有请求都带有用户身份，可以直接判断当前用户与每个计划的互动状态。

## Goals / Non-Goals

**Goals:**
- 用户可对公开计划点赞/取消点赞（toggle 模式），卡片和详情页实时反馈
- 用户可对公开计划收藏/取消收藏（toggle 模式），卡片和详情页实时反馈
- 点赞数和收藏数在卡片列表和详情页正确展示
- 当前用户的点赞/收藏状态在卡片和详情页正确标记

**Non-Goals:**
- "我的收藏"列表页面（后续需求）
- "仅看我的收藏"筛选（后续需求）
- 点赞/收藏的通知推送（后续需求）
- 自己的计划被点赞/收藏的消息提示

## Decisions

### 1. 数据模型：两张独立实体表 + TravelPlan 冗余计数

```
plan_like: id, user_id (FK→user), plan_id (FK→travel_plan), created_at
  UNIQUE(user_id, plan_id)

plan_favorite: id, user_id (FK→user), plan_id (FK→travel_plan), created_at
  UNIQUE(user_id, plan_id)

travel_plan 新增:
  like_count     INT NOT NULL DEFAULT 0
  favorite_count INT NOT NULL DEFAULT 0
```

**为什么冗余计数？** 卡片列表每次加载 12 条，如果每次都 COUNT 子查询或 JOIN，会引入 N+1 或复杂 JPQL。冗余字段让读路径简单高效，写路径在 toggle 时同步更新。

**为什么独立表而不是 ManyToMany？** 独立表更灵活，方便后续扩展（如按时间排序收藏列表），且与项目现有模式一致（单实体 + Repository）。

**为什么 `(user_id, plan_id)` 唯一约束？** 防止同一用户对同一计划重复点赞/收藏，toggle 逻辑依赖此约束。

### 2. API 设计：Toggle 模式

```
POST /api/travelPlan/{id}/like      → { liked: boolean, likeCount: int }
POST /api/travelPlan/{id}/favorite  → { favorited: boolean, favoriteCount: int }
```

同一个端点同时处理添加和取消。后端逻辑：
1. 查询当前用户是否已有记录 → 有则删除、计数 -1 → 无则插入、计数 +1
2. 返回操作后的状态和最新计数

**为什么 toggle 而非 POST + DELETE 分离？** 前端只需一个按钮，不需要追踪当前状态再决定调用哪个接口。一个 toggle 端点简化前后端交互。

### 3. DTO 扩展：附带当前用户状态

`PublicPlanCardDTO` 和 `PublicPlanDetailDTO` 各新增 4 个字段：
- `likeCount: Integer` — 从 TravelPlan.likeCount 直接读
- `favoriteCount: Integer` — 从 TravelPlan.favoriteCount 直接读
- `isLiked: Boolean` — 批量查询 plan_like 表后标记
- `isFavorited: Boolean` — 批量查询 plan_favorite 表后标记

Service 层 `getPublicPlans()` 流程：
1. 现有分页查询 TravelPlan（不变）
2. 收集所有 planId
3. `planLikeRepository.findByUserIdAndPlanIdIn(userId, planIds)` → `Set<Long> likedPlanIds`
4. `planFavoriteRepository.findByUserIdAndPlanIdIn(userId, planIds)` → `Set<Long> favoritedPlanIds`
5. 组装 DTO 时 O(1) 查找 `isLiked` / `isFavorited`

**为什么批量查询而非逐条？** 12 条卡片，逐条查询会产生 24 次数据库调用；批量查询只需 2 次。

### 4. 前端数据流

```
PublicPlanCard 点击 ❤️ → emit('like', planId) → Xunji.vue 调用 toggleLike(planId)
  → 更新 cards 数组中对应项的 isLiked 和 likeCount

PublicPlanCard 点击 ⭐ → emit('favorite', planId) → Xunji.vue 调用 toggleFavorite(planId)
  → 更新 cards 数组中对应项的 isFavorited 和 favoriteCount

PlanDetail.vue 自身调用 API（持有独立的 detail 数据）
```

## Risks / Trade-offs

- **[数据不一致] 冗余计数可能与实体表数据不同步** → 所有 toggle 操作放在 `@Transactional` 方法中，实体插入/删除和计数字段更新在同一事务内完成。Hibernate `ddl-auto: update` 自动管理 schema。
- **[并发点赞] 同一用户快速双击可能触发两次** → 数据库唯一约束是最后防线，service 层捕获 `DataIntegrityViolationException` 降级为幂等操作。前端按钮可以加防抖，但后端不做防抖依赖。
- **[性能] 列表接口增加 2 次批量查询** → 每次查最多 12 个 planId，IN 查询走索引，开销极低。
