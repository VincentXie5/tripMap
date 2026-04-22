## Why

用户注册登录系统已上线，但缺少个人中心页面。用户无法自主管理自己的头像、昵称、邮箱等个人信息，也无法修改密码。这影响了用户体验和账号安全性。

## What Changes

### 后端 API 新增
- `PUT /profile/avatar` - 更新头像类型（Gravatar / 默认）
- `PUT /profile/nickname` - 修改昵称
- `PUT /profile/email` - 修改邮箱（需验证码确认新邮箱）
- `PUT /profile/password` - 修改密码（需验证原密码）
- `POST /profile/send-email-code` - 发送邮箱验证码（用于邮箱修改）
- `GET /profile` - 获取完整个人资料（含邮箱验证状态）

### 前端页面新增
- `/profile` - 个人中心页面
  - 头像展示与修改
  - 昵称展示与修改
  - 邮箱信息展示、验证状态、修改功能
  - 密码修改功能
  - 邮件通知偏好设置（可选）

## Capabilities

### New Capabilities
- `user-profile`: 用户个人中心管理能力，包括头像、昵称、邮箱、密码的管理

### Modified Capabilities
- `user-auth`: 扩展现有用户认证能力，新增密码修改、邮箱修改等相关接口

## Impact

### 后端改动
- `UserController` - 新增 profile 相关接口
- `UserService` - 新增 profile 更新方法
- 新增 `ProfileRequest` / `ProfileResponse` DTO
- 新增错误码（昵称重复、邮箱已被使用、原密码错误等）

### 前端改动
- 新增 `/src/pages/Profile.tsx` 个人中心页面
- 新增 `/src/api/profile.ts` API 调用
- 路由配置添加 profile 页面
- 头部导航添加"个人中心"入口

### 数据库改动
- 暂无新字段（复用现有 avatar_type、email、nickname、password 字段）
