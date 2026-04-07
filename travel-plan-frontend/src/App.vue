<script setup lang="ts">
import { ref, onMounted } from 'vue'
import TravelPlanForm from './components/TravelPlanForm.vue'
import PlanList from './components/PlanList.vue'
import DailyPlanList from './components/DailyPlanList.vue'
import DailyPlanForm from './components/DailyPlanForm.vue'
import LeafletMapComponent from './components/LeafletMapComponent.vue'
import { getPlanList, getDailyPlanList } from './api/travelApi.ts'

interface Plan {
  id: number
  title: string
  startDate: string
  endDate: string
}

interface DailyPlan {
  id: number
  time: string
  location: string
  planDate: string
}

const plans = ref<Plan[]>([])
const selectedPlan = ref<Plan | null>(null)
const dailyPlans = ref<DailyPlan[]>([])
const highlightedDailyPlanId = ref<number | null>(null)
const highlightedDate = ref<string | null>(null)
const planListRef = ref()

// 加载所有旅行计划
const loadPlans = async () => {
  try {
    const response = await getPlanList()
    plans.value = response.data
  } catch (error) {
    console.error('加载计划列表失败:', error)
  }
}

// 选择计划并加载每日行程
const handlePlanSelected = async (plan: Plan) => {
  selectedPlan.value = plan
  try {
    const response = await getDailyPlanList(plan.id)
    dailyPlans.value = response.data
  } catch (error) {
    console.error('加载每日行程失败:', error)
    dailyPlans.value = []
  }
}

// 计划创建成功后刷新列表
const handlePlanCreated = () => {
  loadPlans()
}

// 计划更新成功后刷新列表
const handlePlanUpdated = () => {
  loadPlans()
}

// 计划删除成功后刷新列表
const handlePlanDeleted = () => {
  selectedPlan.value = null
  dailyPlans.value = []
  loadPlans()
}

// 行程添加成功后刷新每日行程列表
const handleDailyAdded = () => {
  if (selectedPlan.value) {
    handlePlanSelected(selectedPlan.value)
  }
}

// 行程更新成功后刷新每日行程列表
const handleDailyPlanUpdated = () => {
  if (selectedPlan.value) {
    handlePlanSelected(selectedPlan.value)
  }
}

// 行程删除成功后刷新每日行程列表
const handleDailyPlanDeleted = () => {
  if (selectedPlan.value) {
    handlePlanSelected(selectedPlan.value)
  }
}

// 地图点位点击触发行程高亮
const handleMarkerClick = (planId: number) => {
  highlightedDailyPlanId.value = planId
  highlightedDate.value = null
}

// 地图路线点击触发日期高亮
const handleRouteClick = (date: string) => {
  highlightedDate.value = date
  highlightedDailyPlanId.value = null
}

// 行程项点击触发地图联动
const handleDailyPlanClick = (planId: number) => {
  highlightedDailyPlanId.value = planId
  // 同时高亮该行程所在日期的路线
  const plan = dailyPlans.value.find(p => p.id === planId)
  if (plan) {
    highlightedDate.value = plan.planDate
  }
}

// 生成Markdown导出内容
const generateMarkdownContent = (plan: Plan, plans: DailyPlan[]) => {
  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  
  let md = `# ${plan.title}\n\n`
  md += `⏰ 出行时间：${plan.startDate} 至 ${plan.endDate}\n\n`
  
  // 按日期分组
  const groups: Record<string, DailyPlan[]> = {}
  plans.forEach(p => {
    if (!groups[p.planDate]) {
      groups[p.planDate] = []
    }
    groups[p.planDate].push(p)
  })
  
  // 按日期排序
  const sortedDates = Object.keys(groups).sort()
  
  sortedDates.forEach(date => {
    const d = new Date(date)
    const weekDay = weekDays[d.getDay()]
    md += `## ${date} ${weekDay}\n`
    
    groups[date].sort((a, b) => a.time.localeCompare(b.time)).forEach(item => {
      const time = item.time.substring(0, 5)
      md += `- ${time} ${item.location}`
      if (item.remark) {
        md += `  \n  > ${item.remark}`
      }
      md += '\n'
    })
    md += '\n'
  })
  
  md += `---\n*由 tripMap 旅行规划系统自动生成*\n`
  
  return md
}

// 处理导出Markdown事件
const handleExportMarkdown = async (plan: Plan) => {
  // 先加载该计划的所有行程
  try {
    const response = await getDailyPlanList(plan.id)
    const markdown = generateMarkdownContent(plan, response.data)
    
    // 触发PlanList组件显示导出弹窗
    // 通过ref调用子组件方法
    if (planListRef.value) {
      planListRef.value.exportMarkdownContent = markdown
      planListRef.value.exportDialogVisible = true
    }
  } catch (error) {
    console.error('加载行程数据失败:', error)
  }
}

// 点击空白区域取消高亮
const handleMapClick = () => {
  highlightedDailyPlanId.value = null
  highlightedDate.value = null
}

onMounted(() => {
  loadPlans()
})
</script>

<template>
  <div class="app-container">
    <!-- 左侧面板 40% -->
    <div class="left-panel">
      <TravelPlanForm @plan-created="handlePlanCreated" />
      <PlanList 
        ref="planListRef"
        :plans="plans" 
        :selected-plan-id="selectedPlan?.id || null"
        @plan-selected="handlePlanSelected"
        @plan-updated="handlePlanUpdated"
        @plan-deleted="handlePlanDeleted"
        @export-markdown="handleExportMarkdown"
      />
      <DailyPlanList 
        v-if="selectedPlan"
        :daily-plans="dailyPlans" 
        :plan-title="selectedPlan.title"
        :highlighted-id="highlightedDailyPlanId"
        :highlighted-date="highlightedDate"
        @daily-plan-updated="handleDailyPlanUpdated"
        @daily-plan-deleted="handleDailyPlanDeleted"
        @daily-plan-click="handleDailyPlanClick"
      />
      <DailyPlanForm 
        v-if="selectedPlan"
        :plan-id="selectedPlan.id"
        @daily-added="handleDailyAdded" 
      />
    </div>
    
    <!-- 右侧地图 60% -->
    <div class="right-panel">
      <LeafletMapComponent 
        :daily-plans="dailyPlans"
        :highlighted-id="highlightedDailyPlanId"
        :highlighted-date="highlightedDate"
        @marker-click="handleMarkerClick"
        @route-click="handleRouteClick"
        @map-click="handleMapClick"
      />
    </div>
  </div>
</template>

<style scoped>
.app-container {
  display: flex;
  width: 100vw;
  height: 100vh;
  margin: 0;
  padding: 0;
  overflow: hidden;
}

.left-panel {
  width: 40%;
  height: 100%;
  background-color: #f5f5f5;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}

.travel-plan-panel {
  padding: 20px;
  background-color: #ffffff;
  border-bottom: 1px solid #e0e0e0;
}

.travel-plan-panel h2 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 24px;
}

.travel-plan-panel p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.itinerary-list {
  flex: 1;
  padding: 20px;
  background-color: #ffffff;
}

.itinerary-list h3 {
  margin: 0 0 15px 0;
  color: #333;
  font-size: 18px;
}

.itinerary-list ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.itinerary-list li {
  padding: 12px 15px;
  margin-bottom: 8px;
  background-color: #f9f9f9;
  border-radius: 8px;
  border-left: 4px solid #4CAF50;
  color: #333;
  font-size: 14px;
  transition: background-color 0.2s;
}

.itinerary-list li:hover {
  background-color: #e8f5e9;
}

.right-panel {
  width: 60%;
  height: 100%;
  position: relative;
}

.map-container {
  width: 100%;
  height: 100%;
  background-color: #e8e8e8;
}

.map-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.map-placeholder h2 {
  margin: 0 0 10px 0;
  font-size: 28px;
}

.map-placeholder p {
  margin: 0;
  font-size: 16px;
  opacity: 0.9;
}
</style>