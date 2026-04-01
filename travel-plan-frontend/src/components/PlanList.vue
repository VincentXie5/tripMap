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
        <div class="plan-actions">
          <el-button type="primary" size="small" @click.stop="editPlan(plan)">编辑</el-button>
          <el-button type="danger" size="small" @click.stop="deletePlan(plan)">删除</el-button>
        </div>
      </div>
    </el-scrollbar>

    <!-- 编辑计划弹窗 -->
    <el-dialog v-model="editDialogVisible" title="编辑旅行计划" width="400px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="计划名称">
          <el-input v-model="editForm.title" placeholder="请输入计划名称" />
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker v-model="editForm.startDate" type="date" placeholder="选择开始日期" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="editForm.endDate" type="date" placeholder="选择结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmEdit">确定</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { defineProps, defineEmits, ref } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { updatePlan, deletePlan as deletePlanApi } from '../api/travelApi'

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

const emit = defineEmits(['plan-selected', 'plan-updated', 'plan-deleted'])

const editDialogVisible = ref(false)
const editForm = ref({
  id: 0,
  title: '',
  startDate: '',
  endDate: ''
})

const selectPlan = (plan: Plan) => {
  emit('plan-selected', plan)
}

const editPlan = (plan: Plan) => {
  editForm.value = {
    id: plan.id,
    title: plan.title,
    startDate: plan.startDate,
    endDate: plan.endDate
  }
  editDialogVisible.value = true
}

const confirmEdit = async () => {
  try {
    await updatePlan(editForm.value.id, {
      title: editForm.value.title,
      startDate: editForm.value.startDate,
      endDate: editForm.value.endDate
    })
    ElMessage.success('更新成功')
    editDialogVisible.value = false
    emit('plan-updated')
  } catch (error) {
    ElMessage.error('更新失败')
  }
}

const deletePlan = async (plan: Plan) => {
  try {
    await ElMessageBox.confirm(
      '确定删除该旅行计划吗？关联的行程数据也将被删除。',
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await deletePlanApi(plan.id)
    ElMessage.success('删除成功')
    emit('plan-deleted')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
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
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.plan-actions {
  display: flex;
  gap: 8px;
}
</style>
