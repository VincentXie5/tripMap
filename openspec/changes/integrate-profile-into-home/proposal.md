## Why

个人中心（`/profile`）目前是一个完全独立的页面，用户必须离开主工作区才能查看或修改个人信息，操作完再点"返回首页"回到地图。这种割裂的导航体验让用户感觉这是两个不相关的应用，而非一个统一的产品。将其整合到主页布局中，统一导航体验。

## What Changes

- **新增** `AppLayout.vue` — 提取共享 Header（Logo + 头像 + 用户名 + 退出按钮），作为持久布局层
- **改造** `Home.vue` — 移除内联 Header，只保留主页内容区（左侧面板 + 分割条 + 地图）
- **改造** `Profile.vue` — 移除 `返回首页` 按钮和独立容器样式，内容撑满内容区
- **调整** 路由结构 — 嵌套路由：`AppLayout` 作为父路由，`/`（主页）和 `/profile`（个人中心）作为子路由，通过 `<router-view>` 切换
- **新增** Header 用户头像显示 — 圆形头像图标，有图片则显示图片，否则显示昵称首字
- **保持** `/` 和 `/profile` 各自独立 URL，支持浏览器前进/后退

## Capabilities

### New Capabilities

- `app-layout`: 应用统一布局——共享 Header 和子路由内容区切换

### Modified Capabilities

- `user-auth`: Header 中的用户信息展示和头像显示要求（不影响后端 API，只是前端展示方式变化）

## Impact

- Affected code: `src/App.vue`, `src/router/index.ts`, `src/views/Home.vue`, `src/views/Profile.vue`
- New files: `src/views/AppLayout.vue`
- No API changes, no backend changes, no dependency changes
- Breaking: `/profile` 不再是独立页面，去掉"返回首页"按钮
