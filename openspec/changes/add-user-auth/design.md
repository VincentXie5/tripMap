## Context

TripMap 正从本地工具升级为旅行社交分享平台，需要引入用户认证系统。当前数据库仅有 `travel_plan` 和 `daily_plan` 两张表，无用户相关字段。项目使用 Spring Boot 3.2 + MySQL + Vue3 技术栈。

## Goals / Non-Goals

**Goals:**
- 实现邮箱注册 + 邮箱验证码验证
- 实现邮箱 + 密码登录，返回 JWT Token
- 实现数据隔离（用户只能访问自己的旅行计划）
- 预留第三方登录扩展字段
- 使用 Gravatar 或默认头像

**Non-Goals:**
- 忘记密码功能（后续版本）
- 第三方登录（微信、Google 等，预留字段）
- 公开计划的匿名访问（本次专注用户认证）

## Decisions

### 1. 用户表设计

```sql
CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    nickname VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    avatar_type ENUM('GRAVATAR', 'DEFAULT') DEFAULT 'DEFAULT',
    is_active BOOLEAN DEFAULT FALSE,
    provider VARCHAR(50) DEFAULT 'EMAIL',        -- 预留第三方登录
    provider_id VARCHAR(255),                     -- 预留第三方登录
    created_at DATETIME,
    updated_at DATETIME
);
```

**Why:** 
- `is_active` 用于邮箱验证后才激活账号
- `provider` 和 `provider_id` 预留，为后续第三方登录做准备
- 密码使用 BCrypt 加密存储

### 2. 邮箱验证码表设计

```sql
CREATE TABLE email_verify_code (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    code VARCHAR(10) NOT NULL,
    type ENUM('REGISTER', 'FORGET_PASSWORD') NOT NULL,
    expired_at DATETIME NOT NULL,
    created_at DATETIME,
    INDEX idx_email_type_expired (email, type, expired_at)
);
```

**Why:**
- 验证码 10 分钟过期，防止滥用
- 支持多种验证码类型（注册/忘记密码）
- 自动清理过期验证码

### 3. JWT Token 设计

```json
{
  "userId": 12345,
  "email": "user@example.com",
  "iat": 1713500000,
  "exp": 1714104800
}
```

**Why:**
- Token 有效期 7 天，长期有效提升用户体验
- 只包含必要信息（userId, email），避免敏感信息
- 前端存储在 localStorage

### 4. API 设计

| 接口 | 方法 | 说明 | 认证 |
|------|------|------|------|
| `/api/auth/send-code` | POST | 发送邮箱验证码 | 否 |
| `/api/auth/register` | POST | 用户注册 | 否 |
| `/api/auth/login` | POST | 用户登录 | 否 |
| `/api/auth/me` | GET | 获取当前用户信息 | 是 |

### 5. 技术选型

| 组件 | 选择 | 理由 |
|------|------|------|
| JWT 库 | jjwt | Java 标准 JWT 库，轻量 |
| 邮件发送 | spring-boot-starter-mail + QQ SMTP | 开发环境简单，后续可切换 |
| 密码加密 | BCrypt | Spring Security 标准 |

### 6. 数据关联改造

`travel_plan` 表添加 `user_id` 外键：
```sql
ALTER TABLE travel_plan ADD COLUMN user_id BIGINT NOT NULL;
ALTER TABLE travel_plan ADD CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES user(id);
```

## Risks / Trade-offs

| Risk | Mitigation |
|------|------------|
| 邮箱验证码被暴力破解 | 验证码 6 位、10 分钟过期、限制发送频率 |
| JWT Token 泄露 | Token 设置合理过期时间（7 天） |
| 邮件发送失败/延迟 | 使用可靠 SMTP 服务，记录发送日志 |
| 第三方登录字段设计不足 | provider + provider_id 足够通用，后续可扩展 |

## Migration Plan

1. **开发阶段**：新增表、修改实体类、添加认证逻辑
2. **测试阶段**：测试注册/登录流程、验证数据隔离
3. **部署阶段**：
   - 先在测试环境验证
   - 生产环境执行数据库迁移脚本
   - 前端适配新的认证流程
4. **回滚方案**：保留数据库迁移脚本，可回滚表结构

## Open Questions

1. 邮件 SMTP 配置信息（开发/生产环境）
2. 前端路由守卫具体实现方式
3. 是否需要黑名单机制（限制频繁发送验证码）

## 附录

- [错误码设计](../docs/core/05_错误码设计.md)
