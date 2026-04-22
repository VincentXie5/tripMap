## Context

用户认证系统已实现（注册、登录、JWT），但缺少用户自主管理个人信息的模块。用户需要能够：
- 管理头像（当前支持 Gravatar / 默认两种类型）
- 修改昵称
- 修改邮箱（需安全验证）
- 修改密码

当前系统已有 `avatar_type` 字段，支持通过邮箱 hash 生成 Gravatar URL。

## Goals / Non-Goals

**Goals:**
- 提供完整的用户资料管理 API
- 修改密码需要验证原密码，确保安全
- 修改邮箱需要新邮箱验证码确认，防止账号被劫持
- 前端提供简洁易用的个人中心页面

**Non-Goals:**
- 不实现文件上传自定义头像（后续可扩展）
- 不实现第三方登录绑定（OAuth 后续可扩展）
- 不实现数据导出/账号注销（GDPR 相关后续考虑）

## Decisions

### 1. API 路由设计

采用 `/profile` 前缀统一管理：
```
GET    /profile          - 获取完整资料
PUT    /profile/avatar   - 更新头像类型
PUT    /profile/nickname - 修改昵称
PUT    /profile/email    - 修改邮箱
PUT    /profile/password - 修改密码
POST   /profile/send-code - 发送邮箱验证码
```

**理由**：所有接口都需要认证，集中在 `/profile` 下便于权限控制和 Swagger 文档组织。

### 2. 邮箱修改流程

采用两步验证：
1. 用户提交新邮箱 → 系统发送验证码到新邮箱
2. 用户提交验证码 + 新邮箱 → 系统验证并更新

**理由**：防止用户输入错误邮箱导致无法登录，也防止恶意修改他人邮箱。

### 3. 密码修改验证

必须提供原密码进行验证：
```
输入: { oldPassword, newPassword }
验证: oldPassword === 数据库中的密码 hash
如果失败: 返回错误
如果成功: 更新为 newPassword 的 hash
```

**理由**：只验证新密码强度不够，需要确保是本人操作。

### 4. 头像类型

当前只支持两种类型：
- `DEFAULT`: 显示默认头像（如首字母 + 背景色）
- `GRAVATAR`: 使用 Gravatar 服务（基于邮箱 hash）

**扩展性**：后续可添加 `CUSTOM` 类型支持文件上传。

### 5. 错误码设计

复用现有 `UserCode` 枚举，新增：
- `NICKNAME_ALREADY_EXISTS`
- `EMAIL_ALREADY_EXISTS`  
- `INVALID_OLD_PASSWORD`
- `EMAIL_NOT_CHANGED`（新旧邮箱相同）

## Risks / Trade-offs

| Risk | Mitigation |
|------|-----------|
| 频繁修改昵称/邮箱导致数据不一致 | 前端添加修改冷却提示（如修改后需等待 N 分钟） |
| 验证码过期导致用户体验差 | 验证码有效期设为 10 分钟，可重新发送 |
| 邮箱修改后原 JWT 仍包含旧邮箱 | JWT 只存 userId，不影响；邮箱变更不影响已登录会话 |

## Open Questions

1. **是否需要在修改邮箱时使旧 JWT 失效？** 
   - 当前方案：JWT 无邮箱信息，只依赖 userId
   - 建议：暂不处理，后续有安全需求再加

2. **邮件通知偏好是否现在做？**
   - 建议：第一版 MVP 先不做，后续根据 email-notification spec 扩展

3. **头像 URL 生成逻辑放在前端还是后端？**
   - 建议：后端在 UserResponse 中直接返回完整 avatarUrl，前端直接使用
