<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import DailyPlanList from '../components/DailyPlanList.vue'
import LeafletMapComponent from '../components/LeafletMapComponent.vue'
import { getPublicPlanDetail } from '../api/travelApi'
import type { PublicPlanDetail, PlanDailyPlan, DailyPlan } from '../types/api'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

const planDetail = ref<PublicPlanDetail | null>(null)
const dailyPlans = ref<DailyPlan[]>([])
const highlightedDailyPlanId = ref<number | null>(null)
const highlightedDate = ref<string | null>(null)
const loading = ref(true)

// 左侧面板宽度
const leftPanelWidth = ref(40)
const isResizing = ref(false)
const MIN_WIDTH = 20
const MAX_WIDTH = 50

const startResize = (_e: MouseEvent) => {
  isResizing.value = true
  document.addEventListener('mousemove', onResize)
  document.addEventListener('mouseup', stopResize)
  document.body.style.cursor = 'col-resize'
  document.body.style.userSelect = 'none'
}

const onResize = (e: MouseEvent) => {
  if (!isResizing.value) return
  const container = document.querySelector('.plan-detail-container') as HTMLElement
  if (!container) return
  const containerRect = container.getBoundingClientRect()
  const newWidth = ((e.clientX - containerRect.left) / containerRect.width) * 100
  leftPanelWidth.value = Math.max(MIN_WIDTH, Math.min(MAX_WIDTH, newWidth))
}

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

const convertToDailyPlan = (pdp: PlanDailyPlan): DailyPlan => ({
  id: pdp.id,
  travelPlan: {
    id: planDetail.value!.id,
    title: planDetail.value!.title,
  },
  time: pdp.time,
  location: pdp.location,
  planDate: pdp.planDate,
  remark: pdp.remark ?? undefined,
  tag: pdp.tag,
  sortOrder: pdp.sortOrder,
  latitude: pdp.latitude ?? undefined,
  longitude: pdp.longitude ?? undefined,
})

const loadPlan = async () => {
  loading.value = true
  try {
    const id = Number(route.params.id)
    const data: any = await getPublicPlanDetail(id)
    planDetail.value = data
    dailyPlans.value = (data.dailyPlans || []).map(convertToDailyPlan)
  } catch (error: any) {
    ElMessage.error('无法访问此计划：该计划未公开或不存在')
    router.replace('/xunji')
  } finally {
    loading.value = false
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

const handleMapClick = () => {
  highlightedDailyPlanId.value = null
  highlightedDate.value = null
}

onMounted(() => {
  loadPlan()
})
</script>

<template>
  <div v-if="loading" class="loading-full">
    <span>加载中...</span>
  </div>
  <div v-else-if="planDetail" class="plan-detail-container">
    <div class="main-content">
      <!-- 左侧面板 -->
      <div class="left-panel" :style="{ width: leftPanelWidth + '%' }">
        <div class="detail-header">
          <el-button text @click="router.back()">
            ← 返回寻迹
          </el-button>
          <div class="creator-bar">
            <el-avatar :size="32" :src="planDetail.creatorAvatarUrl">
              {{ planDetail.creatorNickname?.charAt(0) }}
            </el-avatar>
            <span class="creator-name">{{ planDetail.creatorNickname }}</span>
          </div>
          <h3 class="detail-title">{{ planDetail.title }}</h3>
          <p class="detail-date">{{ planDetail.startDate }} ~ {{ planDetail.endDate }}</p>
        </div>
        <DailyPlanList
          v-if="dailyPlans.length > 0"
          :daily-plans="dailyPlans"
          :plan-title="planDetail.title"
          :plan-id="planDetail.id"
          :plan-start-date="planDetail.startDate"
          :plan-end-date="planDetail.endDate"
          :highlighted-id="highlightedDailyPlanId"
          :highlighted-date="highlightedDate"
          :readonly="true"
          @daily-plan-click="handleDailyPlanClick"
        />
        <div v-else class="empty-daily">
          <p>该计划暂无行程安排</p>
        </div>
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
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.plan-detail-container {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.loading-full {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  font-size: 18px;
  color: #909399;
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
}

.detail-header {
  padding: 16px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
}

.creator-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 12px 0;
}

.creator-name {
  font-size: 14px;
  color: #409eff;
  font-weight: 500;
}

.detail-title {
  margin: 0 0 4px;
  font-size: 20px;
  color: #303133;
}

.detail-date {
  margin: 0;
  font-size: 13px;
  color: #909399;
}

.empty-daily {
  padding: 40px;
  text-align: center;
  color: #909399;
}

.resize-handle {
  width: 6px;
  height: 100%;
  background-color: #e0e0e0;
  cursor: col-resize;
  flex-shrink: 0;
}

.resize-handle:hover {
  background-color: #409eff;
}

.right-panel {
  flex: 1;
  height: 100%;
  position: relative;
}
</style>
