## Why

TripMap 正在从本地旅行规划工具升级为社交分享平台。用户需要在平台上创建和分享自己的旅行计划，这要求引入用户认证系统，实现数据隔离和用户身份识别。

## What Changes

- 新增用户注册功能（邮箱 + 密码 + 昵称 + 邮箱验证码）
- 新增用户登录功能（邮箱 + 密码，返回 JWT Token）
- 新增 JWT 认证中间件，保护需要登录的 API
- 新增邮箱验证码发送功能（注册验证）
- 新增 `user` 表和 `email_verify_code` 表
- 改造 `travel_plan` 表，添加 `user_id` 外键关联

## Capabilities

### New Capabilities

- `user-auth`: 用户认证能力，包括注册、登录、邮箱验证、JWT Token 管理
- `email-notification`: 邮件通知能力，支持发送验证码邮件

### Modified Capabilities

- `travel-plan`: 旅行计划能力需要与用户关联，支持数据隔离

## Impact

- **数据库**：新增 `user` 表、`email_verify_code` 表；修改 `travel_plan` 表
- **后端**：新增 AuthController、UserController；新增 JWT 拦截器；新增邮件服务
- **前端**：新增登录/注册页面；添加 JWT Token 管理；添加认证状态管理
- **依赖**：新增 jjwt（JWT 处理）、spring-boot-starter-mail（邮件发送）
