# tripMap 旅行规划系统 PRD v0.8

## 1. 版本基本信息

| 项 | 说明 |
|---|---|
| **版本号** | v0.8 |
| **迭代日期** | 2026-04-11 |
| **前序版本** | v0.7 |
| **迭代类型** | 数据持久化版本 |
| **迭代目标** | 将数据存储从H2内存数据库迁移到MySQL，实现本地持久化，同时增强日期校验逻辑 |

---

## 2. 版本范围确认

✅ **本版本确定实现功能：**
1. ✅ MySQL数据库持久化 (Docker Compose)
2. ✅ DailyPlan日期校验

❌ **本版本暂不实现功能：**
- 地图导出图片功能
- 行程批量操作
- 行程复制功能
- 用户系统、移动端适配等其他功能

---

## 3. 核心功能详细需求

### 3.1 MySQL数据库持久化 (Docker Compose)

#### 功能描述
使用Docker Compose启动MySQL容器，实现数据本地持久化，服务重启数据不丢失。

#### 详细配置
| 项目 | 内容 |
|------|------|
| MySQL镜像 | mysql:8.0 |
| 容器名 | tripmap-mysql |
| 端口 | 3306:3306 |
| 数据库 | tripMap |
| 用户 | tripMap |
| Root密码 | root |
| 用户密码 | P@ssword! |
| 数据卷 | mysql-data:/var/lib/mysql |
| 重启策略 | unless-stopped |

#### Docker Compose 配置
```yaml
services:
  mysql:
    image: mysql:8.0
    container_name: tripmap-mysql
    ports:
      - "3306:3306"
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: tripMap
      MYSQL_USER: tripMap
      MYSQL_PASSWORD: P@ssword!
    volumes:
      - mysql-data:/var/lib/mysql
    restart: unless-stopped

volumes:
  mysql-data:
```

#### 启动流程
```bash
# 首次运行 - 启动MySQL容器
docker-compose up -d

# 启动后端
mvn spring-boot:run

# 启动前端
cd travel-plan-frontend && npm run dev
```

#### 后端配置变更
1. **pom.xml** - 新增依赖
```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

2. **application.yml** - 数据源配置
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/tripMap?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: tripMap
    password: P@ssword!
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
```

#### 验收标准
- Docker Compose 一键启动MySQL容器成功
- Spring Boot连接MySQL正常
- 数据持久化，重启服务数据不丢失
- JPA自动建表成功

---

### 3.2 DailyPlan日期校验

#### 功能描述
新增/编辑DailyPlan时，日期必须在对应TravelPlan的时间范围内，否则阻止保存并提示用户。

#### 详细需求
1. **校验时机**：保存前校验
2. **校验逻辑**：`travelPlan.startDate <= dailyPlan.planDate <= travelPlan.endDate`
3. **错误提示**："行程日期必须在旅行计划时间范围内 ({startDate} - {endDate})"

#### 后端实现 - DailyPlanServiceImpl.java
```java
public DailyPlan saveDailyPlan(DailyPlan dailyPlan) {
    TravelPlan travelPlan = dailyPlan.getTravelPlan();
    if (travelPlan != null) {
        LocalDate planDate = dailyPlan.getPlanDate();
        LocalDate startDate = travelPlan.getStartDate();
        LocalDate endDate = travelPlan.getEndDate();
        
        if (planDate != null && startDate != null && endDate != null) {
            if (planDate.isBefore(startDate) || planDate.isAfter(endDate)) {
                throw new IllegalArgumentException(
                    "行程日期必须在旅行计划时间范围内 (" + startDate + " - " + endDate + ")"
                );
            }
        }
    }
    return dailyPlanRepository.save(dailyPlan);
}
```

#### 前端优化 - DailyPlanForm.vue
- 日期选择器设置 min/max 限制
- 提供更友好的交互体验

#### 验收标准
- 日期超出范围时正确拦截并提示
- 错误提示信息清晰准确
- 边界日期(开始/结束)可正常保存

---

## 4. 技术实现要点

### 4.1 后端变更
| 文件 | 变更内容 |
|------|---------|
| pom.xml | 新增mysql-connector-j依赖 |
| application.yml | 数据源改为MySQL |
| DailyPlanServiceImpl.java | 添加日期校验 |

### 4.2 Docker新增
| 文件 | 说明 |
|------|------|
| docker-compose.yml | MySQL容器配置 |

### 4.3 前端变更
- 无重大变更，100%兼容v0.7
- 可选：日期选择器min/max限制

---

## 5. 兼容性说明
✅ **本版本有数据库变更，需要首次启动时运行docker-compose**
✅ 数据结构向下兼容
✅ 所有v0.7功能正常

---

## 6. 交付标准
1. ✅ Docker Compose一键启动MySQL成功
2. ✅ Spring Boot连接MySQL正常
3. ✅ 数据持久化，重启不丢失
4. ✅ 日期校验功能正常，提示清晰准确
5. ✅ 所有v0.7功能不受影响
6. ✅ 无控制台错误

---

## 7. 测试要点
1. Docker Compose容器启动测试
2. MySQL数据持久化验证
3. 日期校验边界测试 (开始日期、结束日期、超出范围)
4. 原有功能回归测试 (v0.7所有功能)
5. Docker环境兼容性测试

---

## 8. 启动说明

### 首次启动
```bash
# 1. 启动MySQL容器
docker-compose up -d

# 2. 启动后端
mvn spring-boot:run

# 3. 启动前端 (新终端)
cd travel-plan-frontend && npm run dev
```

### 后续启动
```bash
# 如果MySQL容器已运行，无需docker-compose
mvn spring-boot:run
cd travel-plan-frontend && npm run dev
```

### 停止MySQL
```bash
docker-compose down