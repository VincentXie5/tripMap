<template>
  <el-card class="plan-list-card">
    <template #header>
      <span>旅行计划列表</span>
    </template>
    <div v-if="plans.length === 0" class="empty-tip">
      暂无旅行计划，请先创建一个计划
    </div>
    <el-scrollbar v-else height="300px">
      <div
        v-for="plan in plans"
        :key="plan.id"
        class="plan-item"
        :class="{ active: selectedPlanId === plan.id }"
        @click="selectPlan(plan)"
      >
        <div class="plan-info">
          <span class="plan-date">
            {{ plan.startDate }} - {{ plan.endDate }}
          </span>
          <span class="plan-title">{{ plan.title }}</span>
        </div>
      </div>
    </el-scrollbar>
  </el-card>
</template>

<script setup lang="ts">
import { defineProps, defineEmits } from 'vue'

interface Plan {
  id: number
  title: string
  startDate: string
  endDate: string
}

const props = defineProps<{
  plans: Plan[]
  selectedPlanId: number | null
}>()

const emit = defineEmits(['plan-selected'])

const selectPlan = (plan: Plan) => {
  emit('plan-selected', plan)
}
</script>

<style scoped>
.plan-list-card {
  margin-bottom: 20px;
}

.empty-tip {
  text-align: center;
  color: #909399;
  padding: 40px 0;
}

.plan-item {
  padding: 12px 15px;
  margin-bottom: 8px;
  background-color: #f5f7fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  border-left: 4px solid #409eff;
}

.plan-item:hover {
  background-color: #ecf5ff;
}

.plan-item.active {
  background-color: #ecf5ff;
  border-left-color: #67c23a;
}

.plan-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.plan-date {
  font-size: 12px;
  color: #909399;
}

.plan-title {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}
</style>