<template>
  <el-card class="daily-plan-card">
    <template #header>
      <span>每日行程 - {{ planTitle }}</span>
    </template>
    <div v-if="dailyPlans.length === 0" class="empty-tip">
      暂无行程安排，请添加当日行程
    </div>
    <el-scrollbar v-else height="400px">
      <div v-for="(group, dateKey) in groupedDailyPlans" :key="dateKey" class="date-group">
        <div class="date-header" @click="toggleGroup(dateKey)">
          <span class="toggle-icon">{{ expandedGroups[dateKey] ? '▼' : '▶' }}</span>
          <span class="date-title">📅 {{ formatDate(dateKey) }}</span>
          <span class="date-count">{{ group.length }} 个行程</span>
        </div>
        
        <div v-show="expandedGroups[dateKey]" class="date-content">
          <div class="timeline-line"></div>
          <draggable
            v-model="groupedDailyPlans[dateKey]"
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
                :class="{ highlighted: highlightedId === element.id, [`tag-${element.tag}`]: element.tag }"
                @click="handleItemClick(element)"
              >
                <div class="timeline-dot" :class="[`tag-dot-${element.tag}`]"></div>
                <div class="drag-handle">⋮⋮</div>
                <div class="daily-content">
                  <div class="daily-info">
                    <span class="daily-time">{{ formatTime(element.time) }}</span>
                    <span class="daily-location">{{ element.location }}</span>
                    <span class="daily-tag" v-if="element.tag">{{ getTagName(element.tag) }}</span>
                  </div>
                  <div v-if="element.remark" class="daily-remark">{{ element.remark }}</div>
                </div>
                <div class="daily-actions">
                  <el-button type="primary" size="small" @click.stop="editDailyPlan(element)">编辑</el-button>
                  <el-button type="danger" size="small" @click.stop="deleteDailyPlan(element)">删除</el-button>
                </div>
              </div>
            </template>
          </draggable>
        </div>
      </div>
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
        <el-form-item label="行程标签">
          <el-radio-group v-model="editForm.tag" size="small">
            <el-radio :label="0">无标签</el-radio>
            <el-radio :label="1">🏛️ 景点</el-radio>
            <el-radio :label="2">🍜 美食</el-radio>
            <el-radio :label="3">🏨 住宿</el-radio>
            <el-radio :label="4">🚗 交通</el-radio>
            <el-radio :label="5">🛒 购物</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="行程备注">
          <el-input
            v-model="editForm.remark"
            type="textarea"
            :rows="3"
            placeholder="行程备注、注意事项、交通信息等（可选）"
            maxlength="1000"
            show-word-limit
          />
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
import { defineProps, defineEmits, ref, watch, nextTick, computed, onMounted, onUnmounted } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import draggable from 'vuedraggable'
import { updateDailyPlan, deleteDailyPlan as deleteDailyPlanApi, updateDailyPlanSort } from '../api/travelApi'

const tagNames: Record<number, string> = {
  0: '无标签',
  1: '🏛️ 景点',
  2: '🍜 美食',
  3: '🏨 住宿',
  4: '🚗 交通',
  5: '🛒 购物'
}

interface DailyPlan {
  id: number
  time: string
  location: string
  planDate: string
  planId?: number
  remark?: string
  tag?: number
}

const props = defineProps<{
  dailyPlans: DailyPlan[]
  planTitle: string
  highlightedId?: number | null
  highlightedDate?: string | null
}>()

const emit = defineEmits(['daily-plan-updated', 'daily-plan-deleted', 'daily-plan-click'])

const editDialogVisible = ref(false)
const editForm = ref({
  id: 0,
  planId: 0,
  planDate: '',
  time: '',
  location: '',
  remark: '',
  tag: 0
})

const dailyItemRefs = ref<Record<number, HTMLElement>>({})
const localDailyPlans = ref<DailyPlan[]>([...props.dailyPlans])
const loading = ref(false)

// 分组展开状态
const expandedGroups = ref<Record<string, boolean>>({})

// 按日期分组的计算属性
const groupedDailyPlans = computed(() => {
  const groups: Record<string, DailyPlan[]> = {}
  
  // 初始化展开状态
  localDailyPlans.value.forEach(plan => {
    const dateKey = plan.planDate
    if (!groups[dateKey]) {
      groups[dateKey] = []
      // 默认全部展开
      if (expandedGroups.value[dateKey] === undefined) {
        expandedGroups.value[dateKey] = true
      }
    }
    groups[dateKey].push(plan)
  })
  
  // 每个分组内按时间排序
  Object.keys(groups).forEach(dateKey => {
    groups[dateKey].sort((a, b) => a.time.localeCompare(b.time))
  })
  
  // 按日期排序
  const sortedKeys = Object.keys(groups).sort()
  const sortedGroups: Record<string, DailyPlan[]> = {}
  sortedKeys.forEach(key => {
    sortedGroups[key] = groups[key]
  })
  
  return sortedGroups
})

const getTagName = (tag: number) => tagNames[tag] || tagNames[0]

const formatTime = (time: string) => {
  if (!time) return ''
  // 处理 LocalTime 格式，只显示小时和分钟
  const parts = time.split(':')
  if (parts.length >= 2) {
    return `${parts[0]}:${parts[1]}`
  }
  return time
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return dateStr
  const date = new Date(dateStr)
  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  const month = date.getMonth() + 1
  const day = date.getDate()
  const weekDay = weekDays[date.getDay()]
  return `${date.getFullYear()}年${month}月${day}日 ${weekDay}`
}

const toggleGroup = (dateKey: string) => {
  expandedGroups.value[dateKey] = !expandedGroups.value[dateKey]
}

const editDailyPlan = (plan: DailyPlan) => {
  editForm.value = {
    id: plan.id,
    planId: plan.planId || 0,
    planDate: plan.planDate,
    time: plan.time,
    location: plan.location,
    remark: plan.remark || '',
    tag: plan.tag || 0
  }
  editDialogVisible.value = true
}

const confirmEdit = async () => {
  try {
    await updateDailyPlan(editForm.value.id, {
      travelPlan: { id: editForm.value.planId},
      planDate: editForm.value.planDate,
      time: editForm.value.time,
      location: editForm.value.location,
      remark: editForm.value.remark,
      tag: editForm.value.tag
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
  // 扁平化分组后的数据
  const allPlans: DailyPlan[] = []
  Object.values(groupedDailyPlans.value).forEach(group => {
    allPlans.push(...group)
  })
  
  loading.value = true
  try {
    const sortOrderList = allPlans.map((plan, index) => ({
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

onMounted(() => {
  // 监听全局编辑事件
  window.addEventListener('edit-daily-plan', (e: any) => {
    const plan = e.detail
    editDailyPlan(plan)
  })

  // 监听全局删除事件
  window.addEventListener('delete-daily-plan', (e: any) => {
    const plan = e.detail
    deleteDailyPlan(plan)
  })
})

onUnmounted(() => {
  window.removeEventListener('edit-daily-plan', () => {})
  window.removeEventListener('delete-daily-plan', () => {})
})
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

.date-group {
  margin-bottom: 16px;
  position: relative;
}

.date-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  background: linear-gradient(90deg, #ecf5ff 0%, #f5f7fa 100%);
  border-radius: 8px 8px 0 0;
  cursor: pointer;
  user-select: none;
  border-left: 4px solid #409eff;
}

.toggle-icon {
  font-size: 12px;
  color: #606266;
  transition: transform 0.2s;
  width: 20px;
  text-align: center;
}

.date-title {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}

.date-count {
  font-size: 12px;
  color: #909399;
  margin-left: auto;
  padding: 2px 8px;
  background: #e6f7ff;
  border-radius: 10px;
}

.date-content {
  position: relative;
  background-color: #fafafa;
  padding: 8px 12px 8px 32px;
  border-radius: 0 0 8px 8px;
  border-left: 4px solid #409eff;
}

.timeline-line {
  position: absolute;
  left: 28px;
  top: 0;
  bottom: 0;
  width: 2px;
  background-color: #d9ecff;
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
  position: relative;
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

.daily-item.tag-1 { border-left-color: #409eff; }
.daily-item.tag-2 { border-left-color: #f57c00; }
.daily-item.tag-3 { border-left-color: #43a047; }
.daily-item.tag-4 { border-left-color: #757575; }
.daily-item.tag-5 { border-left-color: #e53935; }

.timeline-dot {
  position: absolute;
  left: -27px;
  top: 20px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background-color: #409eff;
  border: 2px solid #fff;
  z-index: 1;
}

.tag-dot-1 { background-color: #409eff; }
.tag-dot-2 { background-color: #f57c00; }
.tag-dot-3 { background-color: #43a047; }
.tag-dot-4 { background-color: #757575; }
.tag-dot-5 { background-color: #e53935; }

.daily-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.daily-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.daily-tag {
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
  background-color: #e8f0fe;
  color: #1976d2;
}

.daily-remark {
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
  padding-left: 60px;
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