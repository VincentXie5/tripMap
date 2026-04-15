# tripMap v1.0 PRD - 用户系统基础版

## 1. 版本概述

- **版本号**：v1.0
- **版本名称**：用户系统基础版
- **发布时间**：待定
- **主要功能**：引入用户系统，实现用户注册、登录、登出功能，建立用户与旅行计划的数据关联，实现公开/私有权限控制
- **前置版本**：v0.10

---

## 2. 功能清单

### 2.1 用户注册
- 用户名（必填，唯一性校验）
- 邮箱（选填，用于账号找回）
- 密码（必填，常规复杂度要求）
- 确认密码（必填，与密码一致校验）
- 直接注册即可登录，无需邮箱验证

### 2.2 用户登录
- 支持用户名或邮箱 + 密码登录
- JWT Token认证
- Token有效期：7天
- Token中存储userId

### 2.3 用户登出
- 前端清除本地存储的Token（localStorage/sessionStorage）
- 后端使用Redis + Token Blacklist机制（将Token加入黑名单，失效处理）

### 2.4 旅行计划权限控制
- 公开模式（is_public=true, is_private=false）：所有人都可以查看
- 私有模式（is_public=false, is_private=true）：仅自己可见
- 创建计划时默认私有模式

### 2.5 前端页面
- 独立登录页面（/login）
- 登录页包含注册入口（切换到注册页面）
- 注册页面（/register）
- 未登录用户可浏览公开计划
- 首页展示所有公开的旅行计划
- 登录后计划列表区分"我的计划"和"公开的计划"

---

## 3. 数据库设计

### 3.1 新增表：sys_user

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 用户ID |
| username | VARCHAR(50) | UNIQUE, NOT NULL | 用户名 |
| email | VARCHAR(100) | UNIQUE, NULL | 邮箱（选填） |
| password_hash | VARCHAR(255) | NOT NULL | 密码哈希 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

### 3.2 变更表：travel_plan

| 字段名 | 类型 | 约束 | 默认值 | 说明 |
|--------|------|------|--------|------|
| user_id | BIGINT | FK -> sys_user.id, NOT NULL | - | 所属用户ID |
| is_public | TINYINT(1) | NOT NULL | 0 | 是否公开（1=公开，0=不公开） |
| is_private | TINYINT(1) | NOT NULL | 1 | 是否私有（1=私有，0=不私有） |

> **说明**：is_public和is_private共同构成权限控制
> - 公开模式：is_public=1, is_private=0
> - 私有模式：is_public=0, is_private=1

---

## 4. API设计

### 4.1 用户相关接口

#### 4.1.1 用户注册
```
POST /api/user/register
Content-Type: application/json

Request:
{
  "username": "string",      // 必填，用户名
  "email": "string",        // 选填，邮箱
  "password": "string",    // 必填，密码
  "confirmPassword": "string"  // 必填，确认密码
}

Response (200):
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "userId": 1,
    "username": "string"
  }
}

Response (400): 用户名已存在 / 密码不一致
```

#### 4.1.2 用户登录
```
POST /api/user/login
Content-Type: application/json

Request:
{
  "loginId": "string",  // 用户名或邮箱
  "password": "string"  // 密码
}

Response (200):
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": 1,
    "username": "string"
  }
}

Response (401): 用户名或密码错误
```

#### 4.1.3 用户登出
```
POST /api/user/logout
Headers: Authorization: Bearer <token>

Response (200):
{
  "code": 200,
  "message": "登出成功"
}
```

#### 4.1.4 获取当前用户信息
```
GET /api/user/current
Headers: Authorization: Bearer <token>

Response (200):
{
  "code": 200,
  "data": {
    "userId": 1,
    "username": "string",
    "email": "string",
    "createdAt": "2026-04-13T00:00:00"
  }
}

Response (401): Token无效或已过期
```

### 4.2 旅行计划相关接口变更

#### 4.2.1 获取计划列表
```
GET /api/travelPlan
Headers: Authorization: Bearer <token>  // 登录用户必带

Query Parameters:
- type: "my" | "public" | "all"  // my=我的计划, public=公开计划, all=全部（默认all）

Response (200):
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "title": "string",
      "startDate": "2026-04-01",
      "endDate": "2026-04-05",
      "isPublic": true,
      "isPrivate": false,
      "userId": 1,
      "username": "string"  // 计划所有者用户名（公开计划时返回）
    }
  ]
}

说明：
- 未带Token：仅返回is_public=true的计划（公开计划）
- 带Token + type=my：返回当前用户的计划（is_private=true 或 user_id匹配）
- 带Token + type=public：返回所有公开计划（is_public=true）
- 带Token + type=all：返回所有可访问的计划
```

#### 4.2.2 创建计划（需登录）
```
POST /api/travelPlan
Headers: Authorization: Bearer <token>
Content-Type: application/json

Request:
{
  "title": "string",
  "startDate": "2026-04-01",
  "endDate": "2026-04-05",
  "isPublic": false,  // 可选，默认false
  "isPrivate": true   // 可选，默认true
}

Response (200):
{
  "code": 200,
  "data": {
    "id": 1,
    "title": "string",
    "startDate": "2026-04-01",
    "endDate": "2026-04-05",
    "isPublic": false,
    "isPrivate": true,
    "userId": 1
  }
}
```

#### 4.2.3 更新计划（需登录，仅所有者可操作）
```
PUT /api/travelPlan/{id}
Headers: Authorization: Bearer <token>
Content-Type: application/json

Request:
{
  "title": "string",
  "startDate": "2026-04-01",
  "endDate": "2026-04-05",
  "isPublic": false,
  "isPrivate": true
}

Response (200): 更新成功
Response (403): 无权限操作
Response (404): 计划不存在
```

#### 4.2.4 删除计划（需登录，仅所有者可操作）
```
DELETE /api/travelPlan/{id}
Headers: Authorization: Bearer <token>

Response (200): 删除成功
Response (403): 无权限操作
Response (404): 计划不存在
```

### 4.3 每日行程相关接口变更

> 保持原有接口逻辑，增加权限校验：
> - 用户必须登录才能操作
> - 只能操作自己创建的计划的行程

---

## 5. 安全设计

### 5.1 密码存储
- 使用BCrypt或类似哈希算法存储密码
- 不存储明文密码

### 5.2 JWT Token
- 使用JWT标准格式
- Token有效期：7天
- Token中Payload包含：userId, username, exp（过期时间）
- 密钥存储在配置文件中

### 5.3 Token Blacklist
- 使用Redis缓存实现Token黑名单机制
- 用户登出时将Token加入黑名单
- 请求验证时检查黑名单

### 5.4 接口鉴权
- 写操作（POST/PUT/DELETE）需要登录
- 公开计划读取无需登录
- 私有计划读取需要登录且为所有者

---

## 6. 前端设计

### 6.1 路由配置
| 路径 | 组件 | 权限 | 说明 |
|------|------|------|------|
| / | HomeView | 公开 | 首页，展示公开计划 |
| /login | LoginView | 公开 | 登录页 |
| /register | RegisterView | 公开 | 注册页 |
| /plan/:id | PlanDetailView | 需登录/公开 | 计划详情页 |
| /my-plans | MyPlansView | 需登录 | 我的计划列表 |
| /create-plan | CreatePlanView | 需登录 | 创建计划 |

### 6.2 页面设计

#### 6.2.1 首页（/）
- 展示所有公开的旅行计划（is_public=true）
- 显示计划标题、日期、创建者用户名
- 点击进入计划详情页

#### 6.2.2 登录页（/login）
- 标题：登录
- 表单：用户名/邮箱输入框 + 密码输入框 + 登录按钮
- 底部链接：还没有账号？去注册

#### 6.2.3 注册页（/register）
- 标题：注册
- 表单：用户名输入框 + 邮箱输入框 + 密码输入框 + 确认密码输入框 + 注册按钮
- 底部链接：已有账号？去登录

#### 6.2.4 我的计划页（/my-plans）
- 登录后才可访问
- 展示当前用户创建的所有计划（包括公开和私有）
- 支持切换查看公开/私有

### 6.3 Token管理
- 登录成功后，Token存储在localStorage
- 请求时在Authorization Header中携带Token
- 登出时清除Token

---

## 7. 技术实现

### 7.1 后端技术
- SpringBoot 3.2.x
- Spring Security + JWT
- Spring Data JPA
- MySQL
- Redis（Token Blacklist）
- 新增依赖：jjwt-api, jjwt-impl, jjwt-jackson, spring-boot-starter-data-redis

### 7.2 前端技术
- Vue3 + TypeScript
- Vue Router（路由守卫：requiresAuth）
- Axios（请求拦截器：添加Token）
- Element Plus（表单组件）

---

## 8. 兼容性说明

### 8.1 数据迁移
- v1.0上线前清空所有旧数据（travel_plan, daily_plan表）
- 新数据库表：sys_user

### 8.2 API兼容性
- v1.0版本API接口需携带JWT Token认证
- 无Token请求：仅返回公开计划
- 带Token请求：返回用户有权限访问的计划

---

## 9. 验收标准

### 9.1 用户注册
- [ ] 可以成功注册新用户
- [ ] 用户名唯一性校验有效
- [ ] 两次密码输入一致才能注册成功
- [ ] 注册成功后自动登录

### 9.2 用户登录
- [ ] 用户名/邮箱+密码可以登录成功
- [ ] 登录失败提示错误信息
- [ ] 登录成功后Token正确返回

### 9.3 用户登出
- [ ] 登出按钮可见且点击有效
- [ ] 登出后Token被清除
- [ ] 登出后需要重新登录才能访问受保护资源

### 9.4 权限控制
- [ ] 公开计划可以被所有人查看
- [ ] 私有计划仅所有者可见
- [ ] 非所有者无法编辑/删除他人计划

### 9.5 前端交互
- [ ] 登录页和注册页正常切换
- [ ] 未登录用户可查看公开计划
- [ ] 登录后计划列表正确区分"我的计划"和"公开的计划"

---

## 10. 版本影响评估

### 10.1 需要修改的文件
- 后端：新增User实体、Repository、Service、Controller
- 后端：修改TravelPlan实体、Service
- 前端：新增登录页、注册页组件
- 前端：修改计划列表、详情页增加权限判断

### 10.2 不兼容变更
- API需要Token认证
- 旧数据需要清空
- 前端需要处理登录态

---

## 11. 待后续版本实现

- 用户信息编辑（头像、昵称、简介）
- 修改密码
- 账户注销
- 私密模式（通过链接访问需获得授权）
- 旅行路线分享功能（v2.0）

---

*文档创建时间：2026-04-13*
