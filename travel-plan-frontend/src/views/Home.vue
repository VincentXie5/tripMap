<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import PlanList from '../components/PlanList.vue'
import DailyPlanList from '../components/DailyPlanList.vue'
import LeafletMapComponent from '../components/LeafletMapComponent.vue'
import { getPlanList, getDailyPlanList } from '../api/travelApi.ts'
import type { Plan, DailyPlan } from '../types/api'

const plans = ref<Plan[]>([])
const selectedPlan = ref<Plan | null>(null)
const dailyPlans = ref<DailyPlan[]>([])
const highlightedDailyPlanId = ref<number | null>(null)
const highlightedDate = ref<string | null>(null)
const planListRef = ref()

// 左侧面板宽度控制
const leftPanelWidth = ref(40)
const isResizing = ref(false)
const MIN_WIDTH = 20
const MAX_WIDTH = 50

// 开始调整大小
const startResize = (_e: MouseEvent) => {
  isResizing.value = true
  document.addEventListener('mousemove', onResize)
  document.addEventListener('mouseup', stopResize)
  document.body.style.cursor = 'col-resize'
  document.body.style.userSelect = 'none'
}

// 调整大小过程
const onResize = (e: MouseEvent) => {
  if (!isResizing.value) return
  
  const container = document.querySelector('.home-container') as HTMLElement
  if (!container) return
  
  const containerRect = container.getBoundingClientRect()
  const newWidth = ((e.clientX - containerRect.left) / containerRect.width) * 100
  
  leftPanelWidth.value = Math.max(MIN_WIDTH, Math.min(MAX_WIDTH, newWidth))
}

// 停止调整大小
const stopResize = () => {
  isResizing.value = false
  document.removeEventListener('mousemove', onResize)
  document.removeEventListener('mouseup', stopResize)
  document.body.style.cursor = ''
  document.body.style.userSelect = ''
}

onUnmounted(() => {
  document.removeEventListener('mousemove', onResize)
  document.removeEventListener('mouseup', stopResize)
})

const loadPlans = async () => {
  try {
    plans.value = await getPlanList()
  } catch (error) {
    console.error('加载计划列表失败:', error)
  }
}

const handlePlanSelected = async (plan: Plan) => {
  selectedPlan.value = plan
  try {
    dailyPlans.value = await getDailyPlanList(plan.id)
  } catch (error) {
    console.error('加载每日行程失败:', error)
    dailyPlans.value = []
  }
}

const handlePlanCreated = () => {
  loadPlans()
}

const handlePlanUpdated = () => {
  loadPlans()
}

const handlePlanDeleted = () => {
  selectedPlan.value = null
  dailyPlans.value = []
  loadPlans()
}

const handleDailyAdded = () => {
  if (selectedPlan.value) {
    handlePlanSelected(selectedPlan.value)
  }
}

const handleDailyPlanUpdated = () => {
  if (selectedPlan.value) {
    handlePlanSelected(selectedPlan.value)
  }
}

const handleDailyPlanDeleted = () => {
  if (selectedPlan.value) {
    handlePlanSelected(selectedPlan.value)
  }
}

const handleMarkerClick = (planId: number) => {
  highlightedDailyPlanId.value = planId
  highlightedDate.value = null
}

const handleRouteClick = (date: string) => {
  highlightedDate.value = date
  highlightedDailyPlanId.value = null
}

const handleDailyPlanClick = (planId: number) => {
  highlightedDailyPlanId.value = planId
  const plan = dailyPlans.value.find(p => p.id === planId)
  if (plan) {
    highlightedDate.value = plan.planDate
  }
}

const generateMarkdownContent = (plan: Plan, plans: DailyPlan[]) => {
  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  
  let md = `# ${plan.title}\n\n`
  md += `⏰ 出行时间：${plan.startDate} 至 ${plan.endDate}\n\n`
  
  const groups: Record<string, DailyPlan[]> = {}
  plans.forEach(p => {
    if (!groups[p.planDate]) {
      groups[p.planDate] = []
    }
    groups[p.planDate].push(p)
  })
  
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

const handleExportMarkdown = async (plan: Plan) => {
  try {
    const markdown = generateMarkdownContent(plan, await getDailyPlanList(plan.id))
    
    if (planListRef.value) {
      planListRef.value.exportMarkdownContent = markdown
      planListRef.value.exportDialogVisible = true
    }
  } catch (error) {
    console.error('加载行程数据失败:', error)
  }
}

const handleMapClick = () => {
  highlightedDailyPlanId.value = null
  highlightedDate.value = null
}

const handleEditPlan = (planId: number) => {
  const plan = dailyPlans.value.find(p => p.id === planId)
  if (plan) {
    highlightedDailyPlanId.value = planId
    const editEvent = new CustomEvent('edit-daily-plan', { detail: plan })
    window.dispatchEvent(editEvent)
  }
}

const handleDeletePlan = (planId: number) => {
  const plan = dailyPlans.value.find(p => p.id === planId)
  if (plan) {
    const deleteEvent = new CustomEvent('delete-daily-plan', { detail: plan })
    window.dispatchEvent(deleteEvent)
  }
}

const handleLocatePlan = (planId: number) => {
  highlightedDailyPlanId.value = planId
  const plan = dailyPlans.value.find(p => p.id === planId)
  if (plan) {
    highlightedDate.value = plan.planDate
  }
}

onMounted(() => {
  loadPlans()
})
</script>

<template>
  <div class="home-container">
    <div class="main-content">
      <!-- 左侧面板 -->
      <div class="left-panel" :style="{ width: leftPanelWidth + '%' }">
        <PlanList 
          ref="planListRef"
          :plans="plans" 
          :selected-plan-id="selectedPlan?.id || null"
          @plan-selected="handlePlanSelected"
          @plan-updated="handlePlanUpdated"
          @plan-deleted="handlePlanDeleted"
          @plan-created="handlePlanCreated"
          @export-markdown="handleExportMarkdown"
        />
        <DailyPlanList 
          v-if="selectedPlan"
          :daily-plans="dailyPlans" 
          :plan-title="selectedPlan.title"
          :plan-start-date="selectedPlan.startDate"
          :plan-end-date="selectedPlan.endDate"
          :plan-id="selectedPlan.id"
          :highlighted-id="highlightedDailyPlanId"
          :highlighted-date="highlightedDate"
          @daily-plan-updated="handleDailyPlanUpdated"
          @daily-plan-deleted="handleDailyPlanDeleted"
          @daily-plan-click="handleDailyPlanClick"
          @daily-added="handleDailyAdded"
        />
      </div>
      
      <!-- 可拖拽分隔条 -->
      <div 
        class="resize-handle"
        @mousedown="startResize"
      ></div>
      
      <!-- 右侧地图 -->
      <div class="right-panel">
        <LeafletMapComponent 
          :daily-plans="dailyPlans"
          :highlighted-id="highlightedDailyPlanId"
          :highlighted-date="highlightedDate"
          @marker-click="handleMarkerClick"
          @route-click="handleRouteClick"
          @map-click="handleMapClick"
          @edit-plan="handleEditPlan"
          @delete-plan="handleDeletePlan"
          @locate-plan="handleLocatePlan"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.home-container {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.main-content {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.left-panel {
  height: 100%;
  background-color: #f5f5f5;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  min-width: 20%;
  max-width: 50%;
  transition: width 0.1s ease;
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
  flex: 1;
  height: 100%;
  position: relative;
  min-width: 50%;
}
</style>
