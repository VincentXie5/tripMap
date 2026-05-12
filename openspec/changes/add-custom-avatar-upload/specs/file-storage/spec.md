## ADDED Requirements

### Requirement: MinIO 文件存储服务可用
The system SHALL 在 Docker Compose 中部署 MinIO 服务，并确保 Spring Boot 后端能通过 MinIO SDK 连接。

#### Scenario: MinIO 服务启动
- **WHEN** `docker-compose up -d` 执行
- **THEN** MinIO 服务在端口 9000 (API) 和 9001 (Console) 启动
- **AND** 数据持久化到宿主机 volume

#### Scenario: Bucket 自动创建
- **WHEN** Spring Boot 应用启动
- **THEN** `FileService` 检查并自动创建 `tripmap` bucket（如不存在）
- **AND** bucket 设置为公开读策略（仅 avatars 和 share 路径前缀）

### Requirement: 文件上传
The system SHALL 提供文件上传接口，将文件存储到 MinIO 的指定路径。

#### Scenario: 上传文件成功
- **WHEN** 调用 `FileService.upload(key, inputStream, contentType, size)`
- **THEN** 文件存储到 MinIO `tripmap/{key}`
- **AND** Content-Type 正确设置

#### Scenario: 路径生成
- **WHEN** 上传头像文件
- **THEN** 文件存储为 `avatars/{userId}.{extension}`

### Requirement: 文件下载代理
The system SHALL 通过 `GET /api/files/**` 接口代理输出 MinIO 文件内容，不直接暴露 MinIO 地址。

#### Scenario: 代理输出文件
- **WHEN** 浏览器请求 `GET /api/files/avatars/42.jpg`
- **THEN** `FileController` 从 MinIO 读取文件流
- **AND** 以正确的 Content-Type 返回文件内容
- **AND** 设置合理的缓存头（Cache-Control: max-age=3600）

#### Scenario: 文件不存在
- **WHEN** 请求的文件在 MinIO 中不存在
- **THEN** 返回 404 状态码

### Requirement: 文件删除
The system SHALL 支持删除 MinIO 中的文件，支持按路径前缀批量删除。

#### Scenario: 删除单个文件
- **WHEN** 调用 `FileService.delete(key)`
- **THEN** MinIO 中对应的文件被删除

#### Scenario: 按前缀删除
- **WHEN** 调用 `FileService.deleteByPrefix("avatars/42")`
- **THEN** 所有以 `avatars/42` 开头的文件被删除

### Requirement: 文件路径鉴权
The system SHALL 根据文件路径前缀区分访问权限。

#### Scenario: 公共路径无需认证
- **WHEN** 请求 `/api/files/avatars/**` 或 `/api/files/share/**`
- **THEN** 无需携带 JWT token 即可访问

#### Scenario: 私有路径需认证
- **WHEN** 请求 `/api/files/private/**` 且未携带有效 token
- **THEN** 返回 401 未授权
