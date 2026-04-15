# tripMap 旅行规划系统 PRD v0.10

## 1. 版本基本信息

| 项 | 说明 |
|---|---|
| **版本号** | v0.10 |
| **迭代日期** | 2026-04-12 |
| **前序版本** | v0.9 |
| **迭代类型** | 功能补全+体验优化版本 |
| **迭代目标** | 补全编辑行程地址联想、优化日期校验、实现左侧面板可拉伸 |

---

## 2. 版本范围确认

✅ **本版本确定实现功能：**
1. ✅ 编辑每日行程弹窗补全地址搜索联想功能
2. ✅ 旅行计划日期校验（前端+后端）：结束日期 >= 开始日期
3. ✅ 左侧面板可拉伸：支持 20%~50% 范围拖拽调整

❌ **本版本暂不实现功能：**
- 地图导出图片功能
- 行程批量操作
- 行程复制功能
- 用户系统、移动端适配等其他功能

---

## 3. 核心功能详细需求

### 3.1 编辑每日行程弹窗补全地址搜索联想

#### 功能描述
在编辑每日行程弹窗中，为"行程地点"字段补全与新增弹窗一致的地址搜索联想功能，提升用户体验一致性。

#### 详细需求

**现状分析：**
- DailyPlanList.vue 中存在两个弹窗：
  - 添加行程弹窗（addDialog）：已有完整的地址搜索联想功能
  - 编辑行程弹窗（editDialog）：只有普通输入框，缺少搜索联想功能
- 两个弹窗的地址搜索逻辑相同（调用 Nominatim API + 缓存）

**实现方案：**
将添加弹窗中的地址搜索逻辑复用到编辑弹窗中：

1. **编辑弹窗地点字段修改**：
   ```vue
   <el-form-item label="行程地点">
     <div class="location-search-wrapper">
       <el-input
         v-model="editForm.location"
         placeholder="请输入地点"
         @input="handleEditLocationInput"
         @keydown="handleEditKeydown"
       />
       <div v-if="editShowDropdown && editSuggestionList.length > 0" class="location-dropdown">
         <!-- 下拉列表内容复用 addDialog 的样式 -->
       </div>
     </div>
   </el-form-item>
   ```

2. **新增编辑相关的状态变量**：
   - `editSuggestionList`：编辑弹窗的联想列表
   - `editShowDropdown`：编辑弹窗下拉显示状态
   - `editActiveIndex`：编辑弹窗选中索引
   - `editSearchKeyword`：编辑弹窗搜索关键词

3. **新增编辑相关的处理函数**：
   - `handleEditLocationInput`：编辑弹窗地点输入处理
   - `fetchEditLocationSuggestions`：编辑弹窗搜索建议获取
   - `selectEditLocation`：编辑弹窗地点选择
   - `handleEditKeydown`：编辑弹窗键盘导航

4. **复用策略**：
   - 搜索 API 调用逻辑相同，可以抽取为通用函数
   - 缓存机制可以共用同一个 searchCache
   - 下拉样式完全复用现有样式

#### 验收标准
- 编辑弹窗打开时，清空搜索相关状态
- 编辑弹窗输入地点时，显示与添加弹窗一致的下拉联想
- 选中联想项后，地点字段自动填充
- 支持键盘上下选择、回车确认、ESC 关闭
- 搜索结果有缓存，优化频繁搜索体验

---

### 3.2 旅行计划日期校验（前端+后端）

#### 功能描述
确保旅行计划的结束日期大于等于开始日期，在前端和后端同时进行校验，防止非法数据录入。

#### 详细需求

**问题场景：**
- 当前创建/编辑旅行计划时，可以选择开始日期在结束日期之后
- 前端没有日期范围约束
- 后端也没有校验逻辑

**前端实现方案：**

1. **创建计划弹窗（PlanList.vue）**：
   ```vue
   <el-form-item label="开始日期" prop="startDate">
     <el-date-picker
       v-model="createForm.startDate"
       type="date"
       placeholder="选择开始日期"
       value-format="YYYY-MM-DD"
       style="width: 100%"
       @change="handleStartDateChange"
     />
   </el-form-item>
   <el-form-item label="结束日期" prop="endDate">
     <el-date-picker
       v-model="createForm.endDate"
       type="date"
       placeholder="选择结束日期"
       value-format="YYYY-MM-DD"
       style="width: 100%"
       :disabled-date="disabledCreateEndDate"
     />
   </el-form-item>
   ```

2. **编辑计划弹窗**：
   - 同样增加 `disabled-date` 限制
   - 编辑时需要根据当前已选的开始日期动态计算禁用范围

3. **辅助函数**：
   ```typescript
   // 创建弹窗的结束日期禁用逻辑
   const disabledCreateEndDate = (time: Date) => {
     if (!createForm.startDate) return false
     const startDateObj = new Date(createForm.startDate)
     return time.getTime() < startDateObj.getTime()
   }

   // 编辑弹窗的结束日期禁用逻辑
   const disabledEditEndDate = (time: Date) => {
     if (!editForm.value.startDate) return false
     const startDateObj = new Date(editForm.value.startDate)
     return time.getTime() < startDateObj.getTime()
   }
   ```

4. **表单校验规则增强**：
   ```typescript
   const dateValidator = (rule: any, value: string, callback: any) => {
     if (createForm.startDate && createForm.endDate) {
       const start = new Date(createForm.startDate)
       const end = new Date(createForm.endDate)
       if (end < start) {
         callback(new Error('结束日期必须大于等于开始日期'))
       } else {
         callback()
       }
     } else {
       callback()
     }
   }
   ```

**后端实现方案：**

1. **TravelPlan 实体校验（可选，推荐使用注解）**：
   ```java
   // 在 TravelPlan 实体中添加自定义校验注解
   @ValidDateRange(message = "结束日期必须大于等于开始日期")
   public class TravelPlan {
       // ...
   }
   ```

2. **Service 层校验**：
   ```java
   // TravelPlanServiceImpl.java
   @Override
   public TravelPlan createPlan(TravelPlan plan) {
       validateDateRange(plan.getStartDate(), plan.getEndDate());
       return travelPlanRepository.save(plan);
   }

   @Override
   public TravelPlan updatePlan(Long id, TravelPlan plan) {
       TravelPlan existing = travelPlanRepository.findById(id)
           .orElseThrow(() -> new RuntimeException("计划不存在"));
       
       validateDateRange(plan.getStartDate(), plan.getEndDate());
       
       // 更新逻辑...
       return travelPlanRepository.save(existing);
   }

   private void validateDateRange(LocalDate startDate, LocalDate endDate) {
       if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
           throw new IllegalArgumentException("结束日期必须大于等于开始日期");
       }
   }
   ```

3. **Controller 层异常处理**：
   ```java
   @ExceptionHandler(IllegalArgumentException.class)
   public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException e) {
       return ResponseEntity.badRequest().body(e.getMessage());
   }
   ```

#### 验收标准
- 前端：选择开始日期后，结束日期选择器自动禁用早于开始日期的日期
- 前端：提交表单时，如果结束日期早于开始日期，显示校验错误提示
- 后端：创建/更新计划时，如果结束日期早于开始日期，返回 400 错误
- 后端：错误信息明确："结束日期必须大于等于开始日期"
- 前后端校验逻辑一致，用户体验流畅

---

### 3.3 左侧面板可拉伸

#### 功能描述
将左侧面板从固定宽度改为可拖拽调整，实现 20%~50% 范围的拉伸功能，提升用户自定义布局体验。

#### 详细需求

**现状分析：**
- App.vue 中左侧面板固定 `width: 40%`
- 右侧面板固定 `width: 60%`
- 用户无法根据需要调整两侧比例

**实现方案：**

1. **App.vue 布局结构调整**：
   ```vue
   <template>
     <div class="app-container">
       <!-- 左侧面板 -->
       <div class="left-panel" :style="{ width: leftPanelWidth + '%' }">
         <PlanList ref="planListRef" ... />
         <DailyPlanList v-if="selectedPlan" ... />
       </div>
       
       <!-- 可拖拽分隔条 -->
       <div 
         class="resize-handle" 
         @mousedown="startResize"
       ></div>
       
       <!-- 右侧面板 -->
       <div class="right-panel">
         <LeafletMapComponent ... />
       </div>
     </div>
   </template>
   ```

2. **状态变量**：
   ```typescript
   const leftPanelWidth = ref(40)  // 左侧面板宽度百分比
   const isResizing = ref(false)   // 是否正在拖拽
   const MIN_WIDTH = 20            // 最小宽度 20%
   const MAX_WIDTH = 50            // 最大宽度 50%
   ```

3. **拖拽处理逻辑**：
   ```typescript
   const startResize = (e: MouseEvent) => {
     isResizing.value = true
     document.addEventListener('mousemove', onResize)
     document.addEventListener('mouseup', stopResize)
     document.body.style.cursor = 'col-resize'
     document.body.style.userSelect = 'none'
   }

   const onResize = (e: MouseEvent) => {
     if (!isResizing.value) return
     
     const container = document.querySelector('.app-container') as HTMLElement
     if (!container) return
     
     const containerRect = container.getBoundingClientRect()
     const newWidth = ((e.clientX - containerRect.left) / containerRect.width) * 100
     
     // 限制在 20%~50% 范围内
     leftPanelWidth.value = Math.max(MIN_WIDTH, Math.min(MAX_WIDTH, newWidth))
   }

   const stopResize = () => {
     isResizing.value = false
     document.removeEventListener('mousemove', onResize)
     document.removeEventListener('mouseup', stopResize)
     document.body.style.cursor = ''
     document.body.style.userSelect = ''
   }
   ```

4. **样式调整**：
   ```css
   <style scoped>
   .app-container {
     display: flex;
     width: 100vw;
     height: 100vh;
     position: relative;
   }

   .left-panel {
     height: 100%;
     min-width: 20%;
     max-width: 50%;
     transition: width 0.1s ease;  /* 可选：添加过渡效果 */
   }

   .resize-handle {
     width: 6px;
     height: 100%;
     background-color: #e0e0e0;
     cursor: col-resize;
     position: relative;
     z-index: 10;
     flex-shrink: 0;
   }

   .resize-handle:hover,
   .resize-handle:active {
     background-color: #409eff;
   }

   .right-panel {
     flex: 1;  /* 占据剩余空间 */
     height: 100%;
     min-width: 50%;
   }
   </style>
   ```

#### 验收标准
- 页面初始加载时左侧面板保持 40% 宽度
- 鼠标悬停在分隔条时，光标变为 col-resize
- 拖拽分隔条时，左侧面板宽度在 20%~50% 范围内变化
- 右侧面板自动调整宽度，始终填满剩余空间
- 拖拽停止后，布局稳定保持
- 刷新页面后宽度重置为默认值 40%

---

## 4. 技术实现要点

### 4.1 文件变更清单

| 文件 | 变更类型 | 变更内容 |
|------|---------|---------|
| DailyPlanList.vue | 修改 | 补全编辑弹窗地址搜索联想功能 |
| PlanList.vue | 修改 | 增强日期校验逻辑（前端） |
| App.vue | 修改 | 实现左侧面板可拉伸功能 |
| TravelPlanServiceImpl.java | 修改 | 后端日期校验逻辑 |

### 4.2 DailyPlanList.vue 详细变更

| 变更项 | 说明 |
|--------|------|
| 新增状态变量 | editSuggestionList, editShowDropdown, editActiveIndex, editSearchKeyword |
| 新增处理函数 | handleEditLocationInput, fetchEditLocationSuggestions, selectEditLocation, handleEditKeydown |
| 修改编辑弹窗 | 将 `el-input` 替换为 `location-search-wrapper` 组件结构 |
| 样式复用 | 完全复用现有的 location-dropdown 样式类 |

### 4.3 PlanList.vue 详细变更

| 变更项 | 说明 |
|--------|------|
| 新增日期禁用函数 | disabledCreateEndDate, disabledEditEndDate |
| 修改创建弹窗 | 为结束日期 picker 添加 :disabled-date 属性 |
| 修改编辑弹窗 | 为结束日期 picker 添加 :disabled-date 属性 |
| 表单校验增强 | 添加自定义日期范围校验规则 |

### 4.4 App.vue 详细变更

| 变更项 | 说明 |
|--------|------|
| 新增状态变量 | leftPanelWidth, isResizing, MIN_WIDTH, MAX_WIDTH |
| 新增处理函数 | startResize, onResize, stopResize |
| 模板结构调整 | 添加 resize-handle 分隔条元素 |
| 样式调整 | 移除固定宽度，添加分隔条样式 |

### 4.5 TravelPlanServiceImpl.java 详细变更

| 变更项 | 说明 |
|--------|------|
| 新增校验方法 | validateDateRange(LocalDate startDate, LocalDate endDate) |
| 修改 createPlan | 调用日期校验方法 |
| 修改 updatePlan | 调用日期校验方法 |
| 异常处理 | 在 Controller 层添加 @ExceptionHandler 处理 IllegalArgumentException |

---

## 5. 兼容性说明

✅ **前端+后端实现**
- 前端：v0.9 版本前端文件修改
- 后端：TravelPlanServiceImpl.java 修改
- 数据结构完全兼容，无需数据迁移

✅ **接口兼容性**
- 所有现有接口功能正常
- 新增日期校验在现有接口逻辑中内嵌，不改变接口签名
- 校验失败返回 400 错误，前端需处理该错误提示

---

## 6. 交付标准

### 功能交付
1. ✅ 编辑行程弹窗地点字段有下拉地址联想（与添加弹窗一致）
2. ✅ 选择开始日期后，结束日期picker自动禁用早于开始日期的日期
3. ✅ 提交表单时校验结束日期 >= 开始日期
4. ✅ 后端创建/更新计划时校验日期范围
5. ✅ 左侧面板可拖拽调整，范围 20%~50%
6. ✅ 拖拽分隔条时右侧面板自动调整

### 视觉交付
1. ✅ 分隔条悬停时变色提示可拖拽
2. ✅ 布局调整平滑，无闪烁

### 兼容性交付
1. ✅ v0.9 版本所有功能正常
2. ✅ 无控制台错误
3. ✅ 后端启动正常

---

## 7. 测试要点

### 功能测试
1. **编辑弹窗地址联想测试**
   - 打开编辑弹窗
   - 输入地点关键词
   - 验证下拉联想显示
   - 选中联想项，验证地点填充

2. **日期校验前端测试**
   - 创建计划：选择开始日期后，验证结束日期禁用范围
   - 创建计划：选择结束日期早于开始日期，验证提交失败
   - 编辑计划：修改开始日期，验证结束日期范围更新

3. **日期校验后端测试**
   - API 创建计划：结束日期早于开始日期，验证返回 400
   - API 更新计划：结束日期早于开始日期，验证返回 400

4. **左侧面板拉伸测试**
   - 验证初始宽度 40%
   - 向左拖拽，验证最小 20%
   - 向右拖拽，验证最大 50%
   - 验证右侧面板自动调整

### 回归测试
1. 旅行计划 CRUD 功能
2. 每日行程 CRUD 功能
3. 地图联动功能
4. 导出 Markdown 功能

---

## 8. 启动说明

与 v0.9 版本启动方式一致：

```bash
# 1. 启动 MySQL 容器
docker-compose up -d

# 2. 启动后端
mvn spring-boot:run

# 3. 启动前端 (新终端)
cd travel-plan-frontend && npm run dev
```

---

## 9. 预计工作量

| 功能 | 工作量 | 说明 |
|------|--------|------|
| 编辑弹窗地址联想 | 2h | 复用现有搜索逻辑 |
| 日期校验（前端） | 1h | 禁用日期 + 表单校验 |
| 日期校验（后端） | 1h | Service 层 + 异常处理 |
| 左侧面板拉伸 | 2h | 拖拽逻辑 + 样式调整 |
| 测试 | 1h | 功能 + 回归测试 |
| **合计** | **7h** | - |