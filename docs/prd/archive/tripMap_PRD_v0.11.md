# tripMap 旅行规划系统 PRD v0.11

## 1. 版本基本信息

| 项 | 说明 |
|---|---|
| **版本号** | v0.11 |
| **迭代日期** | 2026-04-13 |
| **前序版本** | v0.10 |
| **迭代类型** | 技术架构优化版本 |
| **迭代目标** | 后端接口统一响应封装 + 前端API封装适配 |

---

## 2. 版本范围确认

✅ **本版本确定实现功能：**
1. ✅ 后端：新增统一响应类 `ApiResult<T>`
2. ✅ 后端：新增全局异常处理器 `GlobalExceptionHandler`
3. ✅ 后端：改造所有Controller接口返回 `ApiResult<T>`
4. ✅ 前端：新增统一响应类型定义 `types/api.ts`
5. ✅ 前端：改造 `travelApi.ts` 响应拦截器
6. ✅ 前端：调整Vue组件中的API调用方式

❌ **本版本暂不实现功能：**
- 用户认证/鉴权相关功能
- 日志追踪ID（traceId）
- 其他业务功能

---

## 3. 核心功能详细需求

### 3.1 后端统一响应类 ApiResult

#### 功能描述
创建业界通用的统一响应封装类，所有Controller接口均返回此格式，便于前端统一处理。

#### 详细设计

**字段设计：**
```java
@Data
public class ApiResult<T> {
    private int code;        // 状态码：200成功，400参数错误，401未认证，404未找到，500服务器错误
    private String message;  // 消息：描述本次响应的文字信息
    private T data;         // 数据：泛型，承载实际业务数据
    private long timestamp;  // 时间戳：响应时间毫秒值
}
```

**静态工厂方法：**

| 方法 | 说明 |
|------|------|
| `success(T data)` | 成功响应，默认消息"操作成功" |
| `success(String message, T data)` | 成功响应，自定义消息 |
| `error(int code, String message)` | 错误响应，自定义状态码和消息 |
| `error(String message)` | 错误响应，默认500状态码 |
| `badRequest(String message)` | 快捷方法，400状态码 |
| `notFound(String message)` | 快捷方法，404状态码 |
| `unauthorized(String message)` | 快捷方法，401状态码 |

**代码实现：**

```java
package com.travel.plan.common;

import lombok.Data;

@Data
public class ApiResult<T> {
    private int code;
    private String message;
    private T data;
    private long timestamp;
    
    private ApiResult(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
    
    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(200, "操作成功", data);
    }
    
    public static <T> ApiResult<T> success(String message, T data) {
        return new ApiResult<>(200, message, data);
    }
    
    public static <T> ApiResult<T> error(int code, String message) {
        return new ApiResult<>(code, message, null);
    }
    
    public static <T> ApiResult<T> error(String message) {
        return new ApiResult<>(500, message, null);
    }
    
    public static <T> ApiResult<T> badRequest(String message) {
        return error(400, message);
    }
    
    public static <T> ApiResult<T> notFound(String message) {
        return error(404, message);
    }
    
    public static <T> ApiResult<T> unauthorized(String message) {
        return error(401, message);
    }
}
```

#### 验收标准
- ApiResult类字段完整：code、message、data、timestamp
- 静态工厂方法齐全，可便捷创建成功/失败响应
- 泛型支持正确，可承载任意数据类型

---

### 3.2 全局异常处理器 GlobalExceptionHandler

#### 功能描述
通过 `@RestControllerAdvice` 注解实现全局异常捕获，统一处理所有Controller抛出的异常。

#### 详细设计

**处理异常类型：**

| 异常类型 | 返回状态码 | 说明 |
|---------|----------|------|
| IllegalArgumentException | 400 | 参数校验失败 |
| RuntimeException | 500 | 运行时异常 |
| Exception | 500 | 通用异常兜底 |

**代码实现：**

```java
package com.travel.plan.common;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResult<Void> handleIllegalArgument(IllegalArgumentException e) {
        return ApiResult.badRequest(e.getMessage());
    }
    
    @ExceptionHandler(RuntimeException.class)
    public ApiResult<Void> handleRuntimeException(RuntimeException e) {
        return ApiResult.error(500, e.getMessage());
    }
    
    @ExceptionHandler(Exception.class)
    public ApiResult<Void> handleException(Exception e) {
        return ApiResult.error("服务器内部错误: " + e.getMessage());
    }
}
```

#### 验收标准
- 全局异常处理器可捕获所有未处理的异常
- 各Controller内的 `@ExceptionHandler` 方法需移除，统一由GlobalExceptionHandler处理
- 异常信息通过ApiResult.message返回

---

### 3.3 Controller接口改造

#### 功能描述
将所有Controller的返回类型从 `ResponseEntity<T>` 改为 `ApiResult<T>`，实现响应格式统一。

#### 3.3.1 TravelPlanController 改造

**改造前：**
```java
@PostMapping
public ResponseEntity<TravelPlan> createTravelPlan(@RequestBody TravelPlan travelPlan) {
    TravelPlan createdPlan = travelPlanService.createTravelPlan(travelPlan);
    return ResponseEntity.ok(createdPlan);
}
```

**改造后：**
```java
@PostMapping
public ApiResult<TravelPlan> createTravelPlan(@RequestBody TravelPlan travelPlan) {
    TravelPlan createdPlan = travelPlanService.createTravelPlan(travelPlan);
    return ApiResult.success(createdPlan);
}

@GetMapping
public ApiResult<List<TravelPlan>> getAllTravelPlans() {
    List<TravelPlan> travelPlans = travelPlanService.getAllTravelPlans();
    return ApiResult.success(travelPlans);
}

@PutMapping("/{id}")
public ApiResult<TravelPlan> updateTravelPlan(@PathVariable Long id, @RequestBody TravelPlan travelPlan) {
    TravelPlan updatedPlan = travelPlanService.updateTravelPlan(id, travelPlan);
    return ApiResult.success(updatedPlan);
}

@DeleteMapping("/{id}")
public ApiResult<Void> deleteTravelPlan(@PathVariable Long id) {
    travelPlanService.deleteTravelPlan(id);
    return ApiResult.success("删除成功", null);
}
```

**注意事项：**
- 删除原有的 `@ExceptionHandler` 方法（已迁移到GlobalExceptionHandler）
- `deleteTravelPlan` 返回 `ApiResult<Void>`，使用 `ApiResult.success("删除成功", null)`

#### 3.3.2 DailyPlanController 改造

```java
@PostMapping
public ApiResult<DailyPlan> addDailyPlan(@RequestBody DailyPlan dailyPlan) {
    DailyPlan createdPlan = dailyPlanService.createDailyPlan(dailyPlan);
    return ApiResult.success(createdPlan);
}

@GetMapping("/{planId}")
public ApiResult<List<DailyPlan>> getDailyPlansByTravelPlanId(@PathVariable Long planId) {
    List<DailyPlan> dailyPlans = dailyPlanService.getAllDailyPlansByTravelPlanId(planId);
    return ApiResult.success(dailyPlans);
}

@PutMapping("/{id}")
public ApiResult<DailyPlan> updateDailyPlan(@PathVariable Long id, @RequestBody DailyPlan dailyPlan) {
    DailyPlan updatedPlan = dailyPlanService.updateDailyPlan(id, dailyPlan);
    return ApiResult.success(updatedPlan);
}

@DeleteMapping("/{id}")
public ApiResult<Void> deleteDailyPlan(@PathVariable Long id) {
    dailyPlanService.deleteDailyPlan(id);
    return ApiResult.success("删除成功", null);
}

@PutMapping("/sort/{planId}")
public ApiResult<List<DailyPlan>> updateSortOrder(@PathVariable Long planId, @RequestBody List<Map<String, Object>> sortOrderList) {
    List<DailyPlan> updatedPlans = dailyPlanService.updateSortOrder(planId, sortOrderList);
    return ApiResult.success(updatedPlans);
}
```

#### 3.3.3 GeocodeController 改造

```java
@GetMapping("/search")
public ApiResult<List<Map<String, Object>>> searchLocation(@RequestParam String keyword) {
    List<Map<String, Object>> locations = geocodeService.searchLocation(keyword);
    return ApiResult.success(locations);
}
```

#### 验收标准
- 所有接口返回 `ApiResult<T>` 格式
- 响应示例：`{"code": 200, "message": "操作成功", "data": {...}, "timestamp": 1713000000000}`
- 错误响应示例：`{"code": 400, "message": "结束日期必须大于等于开始日期", "data": null, "timestamp": 1713000000000}`

---

### 3.4 前端统一响应类型定义

#### 功能描述
新增TypeScript类型定义文件，统一前端对API响应结构的认知。

#### 详细设计

**types/api.ts 文件：**

```typescript
// 统一响应结构
export interface ApiResponse<T = any> {
  code: number      // 状态码
  message: string   // 消息
  data: T           // 数据
  timestamp?: number // 时间戳（可选）
}

// HTTP状态码枚举
export enum HttpCode {
  OK = 200,
  BAD_REQUEST = 400,
  UNAUTHORIZED = 401,
  FORBIDDEN = 403,
  NOT_FOUND = 404,
  SERVER_ERROR = 500
}

// 业务状态码（与后端ApiResult.code对应）
export enum BizCode {
  SUCCESS = 200,
  BAD_REQUEST = 400,
  UNAUTHORIZED = 401,
  FORBIDDEN = 403,
  NOT_FOUND = 404,
  SERVER_ERROR = 500
}
```

#### 验收标准
- ApiResponse类型正确定义统一响应结构
- 可通过泛型指定data类型
- 相关枚举值正确

---

### 3.5 前端API封装改造

#### 功能描述
改造 `travelApi.ts`，通过响应拦截器统一处理后端返回的 `ApiResult` 结构。

#### 详细设计

**travelApi.ts 改造后：**

```typescript
import axios, { AxiosInstance, AxiosResponse, AxiosError } from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiResponse } from '@/types/api'

// 创建axios实例
const request: AxiosInstance = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 5000
})

// 请求拦截器（可选扩展）
request.interceptors.request.use(
  (config) => {
    // 可在此处添加Token等通用请求头
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器 - 统一处理ApiResult
request.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const res = response.data
    
    // 业务层面错误处理
    if (res.code !== 200) {
      ElMessage.error(res.message || '操作失败')
      return Promise.reject(new Error(res.message || '操作失败'))
    }
    
    // 成功时直接返回data，便于业务代码直接使用
    return res.data
  },
  (error: AxiosError<ApiResponse>) => {
    // HTTP层面错误处理
    if (error.response) {
      const status = error.response.status
      const data = error.response.data
      
      // 优先使用后端返回的错误信息
      if (data?.message) {
        ElMessage.error(data.message)
      } else {
        switch (status) {
          case 400: ElMessage.error('请求参数错误'); break
          case 401: ElMessage.error('未授权，请重新登录'); break
          case 403: ElMessage.error('禁止访问'); break
          case 404: ElMessage.error('资源不存在'); break
          case 500: ElMessage.error('服务器内部错误'); break
          default: ElMessage.error('请求失败')
        }
      }
    } else {
      ElMessage.error('网络连接失败，请检查网络')
    }
    
    return Promise.reject(error)
  }
)

// ============ API接口定义 ============

// 旅行计划相关接口
export const createPlan = (data: { title: string; startDate: string; endDate: string }) => 
  request.post('/api/travelPlan', data)

export const getPlanList = () => request.get('/api/travelPlan')

export const updatePlan = (id: number, data: { title: string; startDate: string; endDate: string }) => 
  request.put(`/api/travelPlan/${id}`, data)

export const deletePlan = (id: number) => request.delete(`/api/travelPlan/${id}`)

// 每日计划相关接口
export const addDailyPlan = (data: { 
  travelPlan: { id: number }; 
  time: string; 
  location: string; 
  planDate: string; 
  remark?: string; 
  tag?: number 
}) => request.post('/api/dailyPlan', data)

export const getDailyPlanList = (planId: number) => request.get(`/api/dailyPlan/${planId}`)

export const updateDailyPlan = (id: number, data: { 
  travelPlan: { id: number }; 
  time: string; 
  location: string; 
  planDate: string; 
  remark?: string; 
  tag?: number 
}) => request.put(`/api/dailyPlan/${id}`, data)

export const deleteDailyPlan = (id: number) => request.delete(`/api/dailyPlan/${id}`)

export const updateDailyPlanSort = (planId: number, sortOrderList: { id: number; sortOrder: number }[]) => 
  request.put(`/api/dailyPlan/sort/${planId}`, sortOrderList)

// 地理编码相关接口
export const searchLocations = (keyword: string) => 
  request.get(`/api/geocode/search?keyword=${encodeURIComponent(keyword)}`)
```

#### 验收标准
- 响应拦截器正确解析 `ApiResult` 结构
- 业务code非200时，通过ElMessage展示错误信息
- HTTP错误时，通过ElMessage展示友好提示
- 成功时直接返回data，业务代码可直接使用

---

### 3.6 前端业务代码适配

#### 功能描述
调整Vue组件中的API调用方式，适应拦截器直接返回data的变化。

#### 3.6.1 调用方式变化

**改造前（PlanList.vue等）：**
```typescript
import { getPlanList, createPlan } from '@/api/travelApi'

const fetchPlans = async () => {
  try {
    const res = await getPlanList()
    planList.value = res.data  // 从 res.data 取值
  } catch (error) {
    console.error('获取计划列表失败', error)
  }
}

const handleCreate = async () => {
  try {
    await createPlan(createForm.value)
    // 成功后处理
  } catch (error) {
    // 错误已由拦截器处理
  }
}
```

**改造后：**
```typescript
import { getPlanList, createPlan } from '@/api/travelApi'

const fetchPlans = async () => {
  try {
    const data = await getPlanList()  // 直接获取data
    planList.value = data
  } catch (error) {
    console.error('获取计划列表失败', error)
  }
}

const handleCreate = async () => {
  try {
    await createPlan(createForm.value)
    // 成功后处理，错误已由拦截器通过ElMessage展示
  } catch (error) {
    // 如需额外处理可在此处处理
  }
}
```

#### 3.6.2 需修改的Vue文件

| 文件 | 修改说明 |
|------|---------|
| PlanList.vue | getPlanList、createPlan、updatePlan、deletePlan返回值取值方式 |
| DailyPlanList.vue | addDailyPlan、getDailyPlanList、updateDailyPlan、deleteDailyPlan、updateDailyPlanSort返回值取值方式 |
| LeafletMapComponent.vue（或相关地图组件） | searchLocations返回值取值方式 |

#### 验收标准
- 所有API调用处直接使用返回值，无需再取 `.data`
- 错误提示已由拦截器统一处理，业务代码中无需重复展示错误

---

## 4. 技术实现要点

### 4.1 文件变更清单

| 文件路径 | 变更类型 | 说明 |
|---------|---------|------|
| `src/main/java/com/travel/plan/common/ApiResult.java` | **新增** | 统一响应封装类 |
| `src/main/java/com/travel/plan/common/GlobalExceptionHandler.java` | **新增** | 全局异常处理器 |
| `src/main/java/com/travel/plan/controller/TravelPlanController.java` | 修改 | 返回类型改为ApiResult，移除内嵌ExceptionHandler |
| `src/main/java/com/travel/plan/controller/DailyPlanController.java` | 修改 | 返回类型改为ApiResult |
| `src/main/java/com/travel/plan/controller/GeocodeController.java` | 修改 | 返回类型改为ApiResult |
| `travel-plan-frontend/src/types/api.ts` | **新增** | 统一响应类型定义 |
| `travel-plan-frontend/src/api/travelApi.ts` | 修改 | 响应拦截器统一处理 |
| `travel-plan-frontend/src/views/PlanList.vue` | 修改 | API调用返回值取值方式调整 |
| `travel-plan-frontend/src/views/DailyPlanList.vue` | 修改 | API调用返回值取值方式调整 |
| `travel-plan-frontend/src/components/LeafletMapComponent.vue` | 修改（如有） | 地图组件中API调用调整 |

### 4.2 目录结构

```
src/main/java/com/travel/plan/
├── common/
│   ├── ApiResult.java              # 新增
│   └── GlobalExceptionHandler.java  # 新增
└── controller/
    ├── TravelPlanController.java   # 修改
    ├── DailyPlanController.java    # 修改
    └── GeocodeController.java      # 修改

travel-plan-frontend/src/
├── types/
│   └── api.ts                      # 新增
├── api/
│   └── travelApi.ts                # 修改
└── views/
    ├── PlanList.vue                # 修改
    └── DailyPlanList.vue           # 修改
```

---

## 5. 响应格式示例

### 5.1 成功响应

**获取旅行计划列表：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "title": "北京三日游",
      "startDate": "2026-05-01",
      "endDate": "2026-05-03"
    }
  ],
  "timestamp": 1713000000000
}
```

**获取单个旅行计划：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "title": "北京三日游",
    "startDate": "2026-05-01",
    "endDate": "2026-05-03"
  },
  "timestamp": 1713000000000
}
```

**删除操作（无返回数据）：**
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null,
  "timestamp": 1713000000000
}
```

### 5.2 错误响应

**参数校验失败：**
```json
{
  "code": 400,
  "message": "结束日期必须大于等于开始日期",
  "data": null,
  "timestamp": 1713000000000
}
```

**资源不存在：**
```json
{
  "code": 404,
  "message": "计划不存在",
  "data": null,
  "timestamp": 1713000000000
}
```

**服务器内部错误：**
```json
{
  "code": 500,
  "message": "服务器内部错误: xxx",
  "data": null,
  "timestamp": 1713000000000
}
```

---

## 6. 兼容性说明

✅ **完全兼容v0.10版本**
- 数据结构完全不变，仅改变响应包装格式
- 业务逻辑无变化
- 前端通过拦截器透明处理兼容

⚠️ **注意事项**
- 前端需升级对 `ApiResult` 格式的适配
- 原有直接返回实体类型的接口不再可用
- 需要更新前端API调用方式

---

## 7. 交付标准

### 功能交付
1. ✅ 所有Controller接口返回统一 `ApiResult<T>` 格式
2. ✅ 全局异常处理器正确捕获所有未处理异常
3. ✅ 前端响应拦截器统一处理 `ApiResult` 结构
4. ✅ 业务代码无需关心HTTP状态码，只需判断code
5. ✅ 错误信息统一通过ElMessage展示

### 兼容性交付
1. ✅ v0.10版本所有功能正常
2. ✅ 数据结构完全兼容，无需数据迁移
3. ✅ API接口语义不变，仅响应格式变化

### 测试交付
1. ✅ 后端单元测试覆盖ApiResult各种场景
2. ✅ 前端各Vue组件联调测试
3. ✅ 错误场景测试（参数校验、资源不存在等）

---

## 8. 测试要点

### 8.1 后端测试

**ApiResult单元测试：**
1. 测试 `success(T data)` 返回正确格式
2. 测试 `success(String, T)` 自定义消息
3. 测试 `error(int, String)` 自定义状态码
4. 测试 `badRequest/notFound/unauthorized` 快捷方法

**Controller接口测试：**
1. 正常场景：验证返回 `ApiResult` 格式正确
2. 参数校验失败：验证返回 400 状态码
3. 资源不存在：验证返回 404 状态码
4. 服务异常：验证返回 500 状态码

### 8.2 前端测试

**API封装测试：**
1. 验证成功响应直接返回data
2. 验证业务错误时ElMessage展示
3. 验证HTTP错误时ElMessage展示

**Vue组件测试：**
1. PlanList.vue：验证计划列表加载正常
2. PlanList.vue：验证新增/编辑/删除操作正常
3. DailyPlanList.vue：验证行程列表加载正常
4. DailyPlanList.vue：验证行程CRUD操作正常
5. 地图组件：验证地点搜索功能正常

### 8.3 回归测试

1. 旅行计划 CRUD 功能
2. 每日行程 CRUD 功能
3. 行程拖拽排序功能
4. 地图联动功能
5. 地址搜索联想功能
6. 日期校验功能

---

## 9. 启动说明

### 9.1 后端启动

```bash
# 1. 启动 MySQL 容器（如未启动）
docker-compose up -d

# 2. 启动后端
mvn spring-boot:run
```

### 9.2 前端启动

```bash
# 新终端窗口
cd travel-plan-frontend
npm run dev
```

### 9.3 验证步骤

1. 打开浏览器访问 http://localhost:5173
2. 测试旅行计划列表加载
3. 测试新增旅行计划
4. 测试编辑旅行计划
5. 测试删除旅行计划
6. 测试每日行程相关操作
7. 测试地址搜索功能
8. 观察浏览器控制台无报错

---

## 10. 预计工作量

| 模块 | 工作量 | 说明 |
|------|--------|------|
| 后端 ApiResult 类 | 1h | 编写+单元测试 |
| 后端 GlobalExceptionHandler | 0.5h | 迁移+测试 |
| 后端 Controller改造 | 1h | 3个Controller修改 |
| 前端 类型定义 | 0.5h | types/api.ts |
| 前端 API封装 | 1h | 拦截器改造 |
| 前端 业务代码适配 | 2h | Vue组件调用方式调整 |
| 联调测试 | 1.5h | 前后端联调+回归测试 |
| **合计** | **7.5h** | - |

---

## 11. 附录

### 11.1 参考资料

- [Spring Boot统一响应封装最佳实践](https://example.com)
- [Axios拦截器配置指南](https://axios-http.com/)

### 11.2 相关文档

- [tripMap 总PRD](../../tripMap_PRD.md)
- [v0.10 PRD](tripMap_PRD_v0.10.md)
