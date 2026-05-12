## Context

当前应用有两个独立视图：`Home.vue`（`/`）和 `Profile.vue`（`/profile`），各自包含完整的页面结构。`App.vue` 只是 `<router-view />`，没有共享布局。切换到个人中心意味着完全离开主工作区。

目标是将 Header 提取为共享布局层，主页和设置页作为内容区在两个子路由间切换。

## Goals / Non-Goals

**Goals:**
- Header（Logo、用户头像、用户名、退出按钮）只在一个地方定义和渲染
- 主页内容（`/`）和个人中心（`/profile`）通过子路由在共享布局内切换
- 用户名旁显示圆形头像：有 avatarUrl 则显示图片，否则显示昵称首字
- 点击 Logo 返回主页，点击头像/用户名进入个人中心

**Non-Goals:**
- 不修改后端 API
- 不修改侧边栏菜单栏的内容结构
- 不把 `keep-alive` 作为硬性要求（后续按需添加）

## Decisions

### 1. 路由结构：嵌套路由 + 共享布局组件

```
/ (AppLayout.vue)            ← 持久 Header + <router-view>
  ├─ / (Home.vue)            ← 默认子路由，主页内容
  └─ /profile (Profile.vue)  ← 个人中心，全宽内容区
```

选择嵌套路由而非条件渲染（如 `v-if="$route.path === '/profile'"`）：
- 每个页面保持独立 URL，支持浏览器导航
- 代码分离更清晰，不把 Home 和 Profile 的逻辑混在一个组件
- Vue Router 原生支持，无需额外状态管理

### 2. 组件拆分

**`AppLayout.vue`** — 新建，包含：
- Header（渐变背景，`height: 50px`，和当前 Home.vue 的 Header 一致）
- 左侧 Logo "TripMap"，点击导航到 `/`
- 右侧用户区：`el-avatar`（36px 圆形） + 用户名链接 + 退出按钮
- `<router-view />` 作为内容插槽

**`Home.vue`** — 改造：
- 移除内联 Header（整个 `.header` 块和 `.user-info` 块）
- 保留 `.main-content`（左侧面板 + 分隔条 + 地图）
- 移除 `handleLogout` 和相关的 `authStore`、`router` 导入（Header 已提取）

**`Profile.vue`** — 改造：
- 移除 `.back-button`（返回首页按钮）及其 `router.push('/')`
- 移除 `.profile-container` 外层 wrapper，改为直接渲染内容
- 保持所有表单功能不变

**`App.vue`** — 改造：
- 从 `<router-view />` 改为渲染 `AppLayout`（通过路由）

### 3. 头像数据来源

Header 需要 `avatarUrl`，但 auth store 的 `UserInfo` 只有 `avatarType` 没有 `avatarUrl`。

**选择**：`AppLayout.vue` 挂载时调用 `getProfile()` 获取 `avatarUrl`，同时复用 auth store 已有的 `userInfo`（nickname、email）。

- `getProfile()` 返回 `avatarUrl`，用于 `<el-avatar :src="avatarUrl">`
- 如果 `avatarUrl` 为空，`el-avatar` 自动 fallback 到 slot 内容（昵称首字）
- 不需要改动后端 API 或 auth store 结构

### 4. el-avatar 替换 el-link

当前 Header 用户名用 `<el-link>` 渲染，替换为：
```
[el-avatar 36px] [el-button text → 用户名] [el-button → 退出]
```
点击头像或用户名都导航到 `/profile`。

## Risks / Trade-offs

- **Profile 编辑状态丢失**：用户在 Profile 填写表单中途，点击 Logo 回到主页会丢失未保存的内容。这是现有行为（`/profile` → `/`），本次改动不改变这一点。
- **重复请求**：`AppLayout` 调用 `getProfile()`，`Profile.vue` 也调用 `getProfile()`——两次请求相同数据。可以用缓存在后续优化，当前不引入额外复杂度。
