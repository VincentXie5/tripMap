## ADDED Requirements

### Requirement: 用户可上传自定义头像
The system SHALL 允许用户通过 multipart/form-data 上传自定义头像图片。

#### Scenario: 上传头像成功
- **WHEN** 已认证用户 POST `/api/profile/avatar/upload` 附带 jpg/png/webp 图片（不超过 2MB）
- **THEN** 系统将文件上传至 MinIO `avatars/{userId}.{ext}`
- **AND** 更新用户 `avatar_type` 为 `CUSTOM`
- **AND** 更新用户 `avatar_ext` 为文件扩展名
- **AND** 返回更新后的 ProfileResponse（含新的 avatarUrl）

#### Scenario: 文件类型不合法
- **WHEN** 用户上传非图片格式文件（如 pdf、exe）
- **THEN** 返回 400 错误 "仅支持 JPG、PNG、GIF、WebP 格式"

#### Scenario: 文件大小超限
- **WHEN** 用户上传超过 2MB 的图片
- **THEN** 返回 400 错误 "图片大小不能超过 2MB"

#### Scenario: 重复上传覆盖
- **WHEN** 用户再次上传新头像且文件格式与之前不同（如 jpg→png）
- **THEN** 系统删除旧的 `avatars/{userId}.{oldExt}` 文件
- **AND** 存储新的 `avatars/{userId}.{newExt}`
- **AND** 更新 `avatar_ext` 字段

#### Scenario: 未认证用户上传
- **WHEN** 未认证用户请求上传头像
- **THEN** 返回 401 未授权

### Requirement: 自定义头像 URL 生成
The system SHALL 在 `avatar_type` 为 `CUSTOM` 时返回后端代理的头像 URL。

#### Scenario: 返回代理 URL
- **WHEN** 用户 `avatar_type = CUSTOM` 且 `avatar_ext = "jpg"`
- **THEN** `generateAvatarUrl()` 返回 `/api/files/avatars/{userId}.jpg`

#### Scenario: CUSTOM 类型但 avatar_ext 为空（降级）
- **WHEN** 用户 `avatar_type = CUSTOM` 但 `avatar_ext` 为 null
- **THEN** `generateAvatarUrl()` 降级返回 DEFAULT 类型的 SVG
