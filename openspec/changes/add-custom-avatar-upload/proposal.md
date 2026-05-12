## Why

当前用户只能选择"默认头像"（首字母 SVG）或 Gravatar，无法上传自定义图片。同时项目未来需要文件存储能力支持旅行图片分享、私人回忆录等功能，需要引入独立的文件存储基础设施。

## What Changes

- 在 docker-compose 中新增 MinIO 服务作为文件存储系统
- 新建 `FileService` 封装 MinIO 上传/下载/删除操作
- 新建 `FileController` 作为文件访问代理（所有文件请求走后端，不暴露 MinIO 地址）
- `User.AvatarType` 枚举新增 `CUSTOM` 类型
- 新增 `POST /api/profile/avatar/upload` 接口支持 multipart 文件上传
- `GET /api/files/**` 统一代理输出文件内容，`/api/files/avatars/**` 为公开路径
- 前端头像选择区新增文件上传组件，替换纯 radio 选择
- 保持 DEFAULT 和 GRAVATAR 作为备选头像方案

## Capabilities

### New Capabilities
- `file-storage`: MinIO 文件存储基础设施，包括 Docker 部署、SDK 集成、统一的文件上传/下载/删除服务，以及通过后端代理的文件访问接口。后续分享、私人空间等功能都基于此能力。
- `custom-avatar`: 用户在 DEFAULT 和 GRAVATAR 之外可选择上传自定义头像图片，后端接收 multipart 上传、校验文件类型与大小、存储至 MinIO 并返回代理 URL。

### Modified Capabilities
- `user-auth`: "User can get avatar URL" 需求需扩展 — 新增 CUSTOM 类型场景，当 avatar_type 为 CUSTOM 时返回 MinIO 文件的代理 URL。

## Impact

- **Docker**: `docker-compose.yml` 新增 MinIO 服务
- **配置**: `.env.example`、`application.properties` 新增 MinIO 连接参数
- **依赖**: `pom.xml` 新增 MinIO SDK (`io.minio:minio`)
- **后端新建**: `FileService`、`FileController`、相关 DTO
- **后端修改**: `User.AvatarType` 枚举、`UserServiceImpl.generateAvatarUrl()`、`ProfileController`
- **前端新建**: 文件上传组件
- **前端修改**: `Profile.vue` 头像区域、`api/profile.ts`
- **安全**: `SecurityConfig` 需 whitelist `/api/files/avatars/**`
