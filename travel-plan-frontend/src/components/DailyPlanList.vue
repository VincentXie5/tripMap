<template>
  <el-card class="daily-plan-card">
    <template #header>
      <span>每日行程 - {{ planTitle }}</span>
    </template>
    <div v-if="dailyPlans.length === 0" class="empty-tip">
      暂无行程安排，请添加当日行程
    </div>
    <el-scrollbar v-else height="250px">
      <draggable
        v-model="localDailyPlans"
        item-key="id"
        handle=".drag-handle"
        :animation="200"
        ghost-class="ghost-item"
        @end="handleDragEnd"
      >
        <template #item="{ element }">
          <div
            :ref="setDailyItemRef(element.id)"
            class="daily-item"
            :class="{ highlighted: highlightedId === element.id }"
            @click="handleItemClick(element)"
          >
            <div class="drag-handle">⋮⋮</div>
            <div class="daily-info">
              <span class="daily-time">{{ formatTime(element.time) }}</span>
              <span class="daily-location">{{ element.location }}</span>
            </div>
            <div class="daily-actions">
              <el-button type="primary" size="small" @click.stop="editDailyPlan(element)">编辑</el-button>
              <el-button type="danger" size="small" @click.stop="deleteDailyPlan(element)">删除</el-button>
            </div>
          </div>
        </template>
      </draggable>
    </el-scrollbar>

    <!-- 编辑行程弹窗 -->
    <el-dialog v-model="editDialogVisible" title="编辑每日行程" width="400px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="行程日期">
          <el-date-picker v-model="editForm.planDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="行程时间">
          <el-time-picker v-model="editForm.time" placeholder="选择时间" value-format="HH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="行程地点">
          <el-input v-model="editForm.location" placeholder="请输入地点" />
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
import { defineProps, defineEmits, ref, watch, nextTick } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import draggable from 'vuedraggable'
import { updateDailyPlan, deleteDailyPlan as deleteDailyPlanApi, updateDailyPlanSort } from '../api/travelApi'

interface DailyPlan {
  id: number
  time: string
  location: string
  planDate: string
  planId?: number
}

const props = defineProps<{
  dailyPlans: DailyPlan[]
  planTitle: string
  highlightedId?: number | null
}>()

const emit = defineEmits(['daily-plan-updated', 'daily-plan-deleted', 'daily-plan-click'])

const editDialogVisible = ref(false)
const editForm = ref({
  id: 0,
  planId: 0,
  planDate: '',
  time: '',
  location: ''
})

const dailyItemRefs = ref<Record<number, HTMLElement>>({})
const localDailyPlans = ref<DailyPlan[]>([...props.dailyPlans])
const loading = ref(false)

const formatTime = (time: string) => {
  if (!time) return ''
  // 处理 LocalTime 格式，只显示小时和分钟
  const parts = time.split(':')
  if (parts.length >= 2) {
    return `${parts[0]}:${parts[1]}`
  }
  return time
}

const editDailyPlan = (plan: DailyPlan) => {
  editForm.value = {
    id: plan.id,
    planId: plan.planId || 0,
    planDate: plan.planDate,
    time: plan.time,
    location: plan.location
  }
  editDialogVisible.value = true
}

const confirmEdit = async () => {
  try {
    await updateDailyPlan(editForm.value.id, {
      travelPlan: { id: editForm.value.planId},
      planDate: editForm.value.planDate,
      time: editForm.value.time,
      location: editForm.value.location
    })
    ElMessage.success('更新成功')
    editDialogVisible.value = false
    emit('daily-plan-updated')
  } catch (error) {
    ElMessage.error('更新失败')
  }
}

const deleteDailyPlan = async (plan: DailyPlan) => {
  try {
    await ElMessageBox.confirm(
      '确定删除该行程吗？',
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await deleteDailyPlanApi(plan.id)
    ElMessage.success('删除成功')
    emit('daily-plan-deleted')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleItemClick = (plan: DailyPlan) => {
  emit('daily-plan-click', plan.id)
}

const setDailyItemRef = (id: number) => (el: any) => {
  if (el) {
    dailyItemRefs.value[id] = el
  }
}

watch(() => props.highlightedId, async (newId) => {
  if (newId && dailyItemRefs.value[newId]) {
    await nextTick()
    dailyItemRefs.value[newId].scrollIntoView({
      behavior: 'smooth',
      block: 'center'
    })
  }
})

watch(() => props.dailyPlans, (newPlans) => {
  localDailyPlans.value = [...newPlans]
}, { deep: true })

const handleDragEnd = async () => {
  loading.value = true
  try {
    const sortOrderList = localDailyPlans.value.map((plan, index) => ({
      id: plan.id,
      sortOrder: index
    }))
    await updateDailyPlanSort(props.dailyPlans[0]?.planId || 0, sortOrderList)
    ElMessage.success('排序更新成功')
    emit('daily-plan-updated')
  } catch (error) {
    ElMessage.error('排序更新失败')
    localDailyPlans.value = [...props.dailyPlans]
  } finally {
    loading.value = false
  }
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
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.daily-item:hover {
  background-color: #e6f7ff;
  transform: translateX(2px);
}

.daily-item.highlighted {
  background-color: #fff7e6;
  border-left-color: #faad14;
  box-shadow: 0 2px 8px rgba(250, 173, 20, 0.3);
  transform: translateX(4px);
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

.daily-actions {
  display: flex;
  gap: 8px;
}

.drag-handle {
  cursor: grab;
  color: #909399;
  font-size: 16px;
  padding: 4px;
  margin-right: 8px;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.drag-handle:hover {
  background-color: #e6e6e6;
  color: #606266;
}

.drag-handle:active {
  cursor: grabbing;
}

.ghost-item {
  opacity: 0.5;
  background-color: #c6e2ff !important;
  border-left-color: #409eff !important;
}
</style>
