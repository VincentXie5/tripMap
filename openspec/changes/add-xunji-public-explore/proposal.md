## Why

当前所有旅行计划仅创建者本人可见，缺乏社区连接。用户无法发现他人的精彩路线，平台停留在"工具"层面而非"社区"层面。引入公开计划探索功能（寻迹），让旅行爱好者能互相发现路线宝藏，是 TripMap 从规划工具走向旅行社交平台的关键一步。

## What Changes

- TravelPlan 实体增加 `isPublic` 字段，用户可在计划列表中直接切换公开/私有
- 新增"寻迹"页面（`/xunji`），以卡片流形式展示所有公开计划
- 卡片信息包含：标题、日期、路线预览、标签主题色、天数/地点统计、创建者头像昵称
- 支持按关键词搜索（标题、地点名、备注）和按标签筛选
- 点击卡片进入只读地图详情页（`/plan/:id`），复用现有 Home 的地图布局，但不显示编辑功能
- 点击创建者头像可查看该用户的所有公开计划
- 只读详情页支持浏览器后退，返回寻迹列表保持浏览位置

## Capabilities

### New Capabilities

- `public-plan-explore`: 公开计划的浏览与发现功能，包括寻迹页面卡片流、搜索筛选、只读地图详情、按用户查看公开计划
- `plan-visibility-toggle`: 旅行计划公开/私有切换功能

### Modified Capabilities

<!-- No existing capability specs need requirement changes -->

## Impact

- **Entity**: `TravelPlan` — 新增 `isPublic` 字段 (Boolean, default false)
- **Repository**: 新增分页查询、关键字搜索、按用户查公开计划
- **Service/Controller**: 新增公开计划查询接口、可见性切换接口
- **Security**: 公开计划接口需认证但不过滤 owner
- **Frontend Router**: 新增 `/xunji` 和 `/plan/:id` 路由
- **Frontend Views**: 新增 `Xunji.vue`、`PlanDetail.vue`；`PlanList.vue` 增加公开开关
- **Frontend API**: 新增公开计划相关 API 调用
