## 1. MinIO 基础设施

- [x] 1.1 在 docker-compose.yml 中添加 MinIO 服务（端口 9000/9001，挂载 volume）
- [x] 1.2 在 .env.example 中添加 MinIO 配置项，使用示例值（MINIO_ROOT_USER=minioadmin 等）
- [x] 1.3 在 .env 中添加真实的 MinIO 配置项（使用强密码，与 .env.example 保持 key 一致）
- [x] 1.4 在 pom.xml 中添加 MinIO SDK 依赖 (`io.minio:minio`)
- [x] 1.5 在 application.properties 中添加 MinIO 连接配置

## 2. 文件服务层

- [x] 2.1 创建 `FileService` 接口：upload、download、delete、deleteByPrefix 方法
- [x] 2.2 创建 `FileServiceImpl`：使用 MinioClient 实现上传/下载/删除，@PostConstruct 自动创建 bucket
- [x] 2.3 创建 `FileController`：`GET /api/files/**` 代理输出文件流，设置 Content-Type 和缓存头

## 3. 安全配置

- [x] 3.1 在 SecurityConfig 中 whitelist `/api/files/avatars/**` 和 `/api/files/share/**`
- [x] 3.2 确保 `/api/files/private/**` 保持 Spring Security 默认保护（需认证）

## 4. 后端头像上传

- [x] 4.1 User 实体：AvatarType 枚举新增 CUSTOM，新增 avatarExt 字段
- [x] 4.2 修改 `UserServiceImpl.generateAvatarUrl()`：CUSTOM 类型返回 `/api/files/avatars/{userId}.{avatarExt}`
- [x] 4.3 ProfileController 新增 `POST /api/profile/avatar/upload`：接收 multipart 文件，校验类型与大小，调用 FileService 上传
- [x] 4.4 ProfileController 现有 `PUT /api/profile/avatar` 兼容 avatarType=CUSTOM 的切换

## 5. 前端头像上传

- [x] 5.1 在 `api/profile.ts` 中新增 `uploadAvatar` 函数（multipart/form-data POST）
- [x] 5.2 修改 Profile.vue：保留 radio（DEFAULT/GRAVATAR），新增 CUSTOM 选项 + el-upload 上传组件
- [x] 5.3 AppLayout.vue 无需修改（已通过 getProfile 动态加载 avatarUrl）
