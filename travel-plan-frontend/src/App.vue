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
        :plans="plans" 
        :selected-plan-id="selectedPlan?.id || null"
        @plan-selected="handlePlanSelected"
        @plan-updated="handlePlanUpdated"
        @plan-deleted="handlePlanDeleted"
      />
      <DailyPlanList 
        v-if="selectedPlan"
        :daily-plans="dailyPlans" 
        :plan-title="selectedPlan.title"
        @daily-plan-updated="handleDailyPlanUpdated"
        @daily-plan-deleted="handleDailyPlanDeleted"
      />
      <DailyPlanForm 
        v-if="selectedPlan"
        :plan-id="selectedPlan.id"
        @daily-added="handleDailyAdded" 
      />
    </div>
    
    <!-- 右侧地图 60% -->
    <div class="right-panel">
      <LeafletMapComponent :daily-plans="dailyPlans" />
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