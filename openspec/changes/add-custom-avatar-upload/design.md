## Context

当前头像系统仅支持 DEFAULT（首字母 SVG）和 GRAVATAR 两种类型，存储完全在代码和外部服务中计算。用户需要自定义头像，且未来有旅行图片分享、私人回忆录等文件存储需求。选择 MinIO 作为统一文件存储后端，本次以头像上传作为首个接入场景。

## Goals / Non-Goals

**Goals:**
- 引入 MinIO 作为文件存储基础设施，与 MySQL 并列作为核心服务
- 支持用户上传自定义头像图片（jpg/png/webp，限制 2MB）
- 所有文件通过后端代理访问，不暴露 MinIO 地址
- 代理接口做好鉴权分层：avatars 路径公开，后续 private 路径需登录

**Non-Goals:**
- 不做前端图片裁剪（后续迭代可加）
- 不实现分享系统或私人空间的具体功能（仅预留目录结构）
- 不改变 DEFAULT / GRAVATAR 的现有行为

## Decisions

### 1. MinIO SDK 选择：使用 MinIO 官方 SDK (`io.minio:minio`)

- **理由**: 原生支持 MinIO，API 简洁，无需引入 AWS SDK 的多余依赖。如果将来迁移到 AWS S3，MinIO SDK 也兼容 S3 协议。
- **替代方案**: AWS SDK (`software.amazon.awssdk:s3`) — 功能更全但依赖重，对当前场景过度。

### 2. 文件访问架构：统一后端代理

用户明确不希望暴露 MinIO 地址，因此所有文件请求走 `FileController`，MinIO 不直接对外暴露端口。

```
浏览器 GET /api/files/avatars/42.jpg
  → FileController → FileService.download("avatars/42.jpg")
  → MinIO SDK getObject() → InputStream
  → ResponseEntity<InputStreamResource> 返回给浏览器
```

- `SecurityConfig` whitelist `/api/files/avatars/**` 和 `/api/files/share/**`
- 后续 `/api/files/private/**` 保持 Spring Security 保护（默认需认证）
- 头像 URL 存入数据库？**不存** — 由 `generateAvatarUrl()` 运行时计算，保持数据库干净。`CUSTOM` 类型时根据 userId 拼接 `/api/files/avatars/{userId}.{ext}`

### 3. 自定义头像文件扩展名处理

用户上传的文件可能是 jpg/png/webp/gif。为避免需要记录扩展名，采用方案：

- 上传时保留原始扩展名，存储为 `avatars/{userId}.{ext}`
- 读取时不依赖扩展名 — MinIO 的 Content-Type 从上传时设置，代理直接透传
- `generateAvatarUrl()` 需要知道扩展名 → **User 实体加 `avatarExt` 字段**（仅存扩展名，如 `jpg`）

**修正**: 如果用户多次上传不同格式的图片（先 jpg 后 png），旧文件可保留或覆盖。方案：上传时删除该用户之前的 avatar 对象（按前缀匹配删除 `avatars/{userId}.*`）。

### 4. Bucket 结构

```
tripmap/                         (单一 bucket)
├── avatars/                     (公开读)
│   └── {userId}.{ext}
├── share/                       (公开读，预留)
│   └── {shareId}/
└── private/                     (需认证，预留)
    └── {userId}/
```

单 bucket 多路径前缀的模型，通过路径前缀区分访问权限，比多 bucket 管理更简单。

### 5. User 实体变更

```java
// AvatarType 枚举新增
public enum AvatarType {
    GRAVATAR, DEFAULT, CUSTOM
}

// 新增字段
@Column(name = "avatar_ext", length = 10)
private String avatarExt;  // 仅 CUSTOM 类型时使用，如 "jpg", "png"
```

`generateAvatarUrl()` 逻辑：
- `DEFAULT` → 内联 SVG data URI（不变）
- `GRAVATAR` → gravatar.com URL（不变）
- `CUSTOM` → `/api/files/avatars/{userId}.{avatarExt}`

### 6. 前端组件

使用 Element Plus `<el-upload>` 替换当前 `<el-radio-group>`：

- 支持点击上传 + 拖拽上传
- 上传前做客户端校验（类型、大小）
- 上传后立即更新头像预览
- 保留 DEFAULT / GRAVATAR 作为备选（用 radio 切换，选 CUSTOM 时显示上传组件）

### 7. 重复上传处理

同一用户再次上传头像时：
1. 后端接收新文件，用 `userId` + 扩展名组合 key
2. MinIO `putObject` 覆盖同名 key（原地替换）
3. 如果扩展名变了（jpg→png），删旧 key，存新 key

## Risks / Trade-offs

- **MinIO 数据持久化**: MinIO 容器数据需挂载到宿主机 volume（与 MySQL 数据目录同样处理），否则容器重建后文件丢失。已在 docker-compose 中配置。
- **大文件上传**: 当前头像限制 2MB，后续大文件场景（旅行照片）需考虑分片上传、断点续传。本次不处理，属于后续迭代。
- **无 CDN**: 所有文件走后端代理，大量图片访问会增加服务器带宽压力。对于当前项目量级可接受，后续可引入 CDN/Nginx 静态缓存层。
- **avatarExt 脏数据风险**: 如果 MinIO 文件被手动删除但 User.avatarExt 仍有值，头像 404。代理接口可返回默认占位图作为降级。

## Open Questions

- MinIO bucket 是否需要启动时自动创建？→ 建议在 `FileService` 中用 `@PostConstruct` 检查并创建，确保首次启动即可用
