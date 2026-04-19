## 1. 后端依赖与配置

- [x] 1.1 添加 jjwt 依赖到 pom.xml
- [x] 1.2 添加 spring-boot-starter-mail 依赖到 pom.xml
- [x] 1.3 配置邮件 SMTP（application.yml 添加 mail 配置）

## 2. 数据库设计

- [x] 2.1 创建 user 表（JPA Entity）
- [x] 2.2 创建 email_verify_code 表（JPA Entity）
- [x] 2.3 修改 travel_plan 表添加 user_id 外键

## 3. 核心功能实现

### 3.1 用户服务层

- [x] 3.1.1 创建 User 实体类（User.java）
- [x] 3.1.2 创建 UserRepository（JPA Repository）
- [x] 3.1.3 创建 UserService 接口
- [x] 3.1.4 创建 UserServiceImpl 实现类（注册、登录、获取用户信息）

### 3.2 邮箱验证码服务

- [x] 3.2.1 创建 EmailVerifyCode 实体类
- [x] 3.2.2 创建 EmailVerifyCodeRepository
- [x] 3.2.3 创建 EmailService 接口
- [x] 3.2.4 创建 EmailServiceImpl（发送验证码邮件）
- [x] 3.2.5 实现验证码生成与存储逻辑
- [x] 3.2.6 实现验证码校验逻辑（过期、单次使用）

### 3.3 JWT 服务

- [x] 3.3.1 创建 JwtService 类（生成、验证 Token）
- [x] 3.3.2 实现 Token 解析与用户信息提取

### 3.4 认证拦截器

- [x] 3.4.1 创建 JwtAuthFilter（Spring Filter）
- [x] 3.4.2 配置 SecurityFilterChain（白名单、认证路径）

## 4. 控制器层

- [x] 4.1 创建 AuthController
  - [x] 4.1.1 POST /api/auth/send-code（发送验证码）
  - [x] 4.1.2 POST /api/auth/register（注册）
  - [x] 4.1.3 POST /api/auth/login（登录）
  - [x] 4.1.4 GET /api/auth/me（获取当前用户）

## 5. 前端实现

### 5.1 认证页面

- [x] 5.1.1 创建登录页面（Login.vue）
- [x] 5.1.2 创建注册页面（Register.vue）
- [x] 5.1.3 实现邮箱验证码倒计时功能

### 5.2 状态管理

- [x] 5.2.1 创建 auth store（Pinia）
- [x] 5.2.2 实现 JWT Token 存储与自动携带
- [x] 5.2.3 实现登录状态管理

### 5.3 路由守卫

- [x] 5.3.1 实现路由守卫（未登录重定向）
- [x] 5.3.2 实现 Token 过期处理

### 5.4 API 封装

- [x] 5.4.1 创建 auth API 模块（auth.ts）
- [x] 5.4.2 配置 Axios 拦截器（自动添加 Token）

## 6. 数据隔离改造

- [x] 6.1 修改 TravelPlan 实体添加 user_id 字段
- [x] 6.2 修改 TravelPlanService 过滤当前用户数据
- [x] 6.3 修改 DailyPlanService 间接关联用户

## 7. 测试

- [x] 7.1 测试注册流程（正常、验证码错误、邮箱已存在）
- [x] 7.2 测试登录流程（正常、密码错误、未激活）
- [x] 7.3 测试数据隔离（用户 A 不能访问用户 B 的计划）
- [x] 7.4 测试 JWT Token 过期与无效处理
