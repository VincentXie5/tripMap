<template>
  <el-card class="daily-plan-card">
    <template #header>
      <span>每日行程 - {{ planTitle }}</span>
    </template>
    <div v-if="dailyPlans.length === 0" class="empty-tip">
      暂无行程安排，请添加当日行程
    </div>
    <el-scrollbar v-else height="250px">
      <div
        v-for="plan in dailyPlans"
        :key="plan.id"
        class="daily-item"
      >
        <div class="daily-info">
          <span class="daily-time">{{ formatTime(plan.time) }}</span>
          <span class="daily-location">{{ plan.location }}</span>
        </div>
      </div>
    </el-scrollbar>
  </el-card>
</template>

<script setup lang="ts">
import { defineProps } from 'vue'

interface DailyPlan {
  id: number
  time: string
  location: string
  planDate: string
}

const props = defineProps<{
  dailyPlans: DailyPlan[]
  planTitle: string
}>()

const formatTime = (time: string) => {
  if (!time) return ''
  // 处理 LocalTime 格式，只显示小时和分钟
  const parts = time.split(':')
  if (parts.length >= 2) {
    return `${parts[0]}:${parts[1]}`
  }
  return time
}
</script>

<style scoped>
.daily-plan-card {
  margin-bottom: 20px;
}

.empty-tip {
  text-align: center;
  color: #909399;
  padding: 40px 0;
}

.daily-item {
  padding: 12px 15px;
  margin-bottom: 8px;
  background-color: #f0f9ff;
  border-radius: 8px;
  border-left: 4px solid #67c23a;
}

.daily-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.daily-time {
  font-size: 14px;
  color: #409eff;
  font-weight: 600;
  min-width: 60px;
}

.daily-location {
  font-size: 14px;
  color: #303133;
}
</style>