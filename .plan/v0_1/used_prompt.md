指令 1：创建 SpringBoot 后端项目
请从零创建一个SpringBoot 3.2.5后端项目，使用Maven构建，包名com.travel.plan，集成以下依赖：spring-web、spring-boot-starter-data-jpa、lombok、h2数据库。配置application.yml，设置端口8080，开启H2控制台，配置本地嵌入式H2数据库。生成完整的项目结构，包含启动类、基础配置类。

指令 2：创建 Vue3 前端项目
请从零创建一个Vue3 + Vite前端项目，项目名travel-plan-frontend，安装依赖：element-plus、axios、高德地图JS API依赖。配置vite.config.js代理后端8080接口，配置main.js全局引入Element Plus。生成完整的项目结构，删除默认无用代码。

指令 3：创建数据实体类
根据v0.1 PRD，创建两个JPA实体类：
1. TravelPlan：id(Long,主键自增)、title(String)、startDate(LocalDate)、endDate(LocalDate)
2. DailyPlan：id(Long,主键自增)、planId(Long)、planDate(LocalDate)、time(String)、location(String)
   添加lombok注解，配置数据库表映射。

指令 4：创建 Repository 数据访问层
创建两个JPA Repository接口：
1. TravelPlanRepository
2. DailyPlanRepository
   并实现根据planId查询所有DailyPlan的方法。

指令 5：创建 Service 服务层
创建TravelPlanService和DailyPlanService，实现以下核心方法：
1. 创建旅行计划
2. 查询所有旅行计划
3. 添加每日行程
4. 根据计划ID查询所有行程
   添加简单的业务逻辑，无复杂校验。

指令 6：创建 Controller 接口层
创建TravelPlanController和DailyPlanController，编写RESTful接口：
1. POST /api/plan 创建计划
2. GET /api/plan 查询所有计划
3. POST /api/daily 添加行程
4. GET /api/daily/{planId} 查询计划下所有行程
   接口返回统一格式：code、msg、data。

指令 7：构建主页面布局
修改App.vue，打造左右两栏布局：
左侧宽度40%：旅行计划面板 + 行程列表
右侧宽度60%：高德地图容器
设置基础样式，填满屏幕，无边距滚动。

指令 8：封装 API 请求
在src/api创建travelApi.js，封装所有后端接口请求：
1. createPlan
2. getPlanList
3. addDailyPlan
4. getDailyPlanList
   使用axios调用后端接口，配置统一请求根路径。

指令 9：实现左侧计划 + 行程列表
在前端实现左侧功能：
1. 表单创建旅行计划（名称、开始日期、结束日期）
2. 列表展示所有计划，格式：开始日期-结束日期｜计划名称
3. 点击计划，加载并展示该计划的每日行程，格式：时间：地点
4. 表单添加当日行程（时间、地点）
   使用Element Plus组件实现。

指令 10：集成高德地图并展示点位
在前端右侧集成高德地图JS API，实现：
1. 初始化地图，默认显示中国
2. 监听行程列表变化，自动根据地点名称（location字段）解析经纬度
3. 在地图上添加标记点，所有行程地点都展示在地图上
4. 地图自适应显示所有标记点
   使用高德地图官方Web API，代码简洁可用。

指令 11：完整功能联调指令
帮我检查前后端所有接口连通性，修复跨域、参数错误等问题，确保：
1. 创建计划成功
2. 展示计划列表成功
3. 添加行程成功
4. 左侧显示行程成功
5. 右侧地图显示地点成功
   实现@/.plan\v0_1\tripMap_PRD_v0.1.md 完整可用版本。