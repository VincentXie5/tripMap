<template>
  <el-card class="plan-list-card">
    <template #header>
      <div class="header-wrapper">
        <span>旅行计划列表</span>
        <el-button type="primary" size="small" @click="openCreateDialog">
          + 新增计划
        </el-button>
      </div>
    </template>
    <div v-if="plans.length === 0" class="empty-tip">
      暂无旅行计划，请点击上方"新增计划"按钮创建
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
          <el-button type="success" size="small" @click.stop="exportMarkdown(plan)">导出MD</el-button>
          <el-button type="primary" size="small" @click.stop="editPlan(plan)">编辑</el-button>
          <el-button type="danger" size="small" @click.stop="deletePlan(plan)">删除</el-button>
        </div>
      </div>
    </el-scrollbar>

    <!-- Markdown导出弹窗 -->
    <el-dialog v-model="exportDialogVisible" title="导出Markdown" width="600px">
      <el-input
        v-model="exportMarkdownContent"
        type="textarea"
        :rows="15"
        readonly
        style="margin-bottom: 16px; font-family: monospace;"
      />
      <template #footer>
        <el-button @click="exportDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="copyToClipboard">一键复制</el-button>
      </template>
    </el-dialog>

    <!-- 创建计划弹窗 -->
    <el-dialog v-model="createDialogVisible" title="创建旅行计划" width="400px">
      <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="80px">
        <el-form-item label="计划名称" prop="title">
          <el-input v-model="createForm.title" placeholder="请输入计划名称" />
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker v-model="createForm.startDate" type="date" placeholder="选择开始日期" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker 
            v-model="createForm.endDate" 
            type="date" 
            placeholder="选择结束日期" 
            value-format="YYYY-MM-DD" 
            style="width: 100%"
            :disabled-date="disabledCreateEndDate"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCreate" :loading="createLoading">确定创建</el-button>
      </template>
    </el-dialog>

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
          <el-date-picker 
            v-model="editForm.endDate" 
            type="date" 
            placeholder="选择结束日期" 
            value-format="YYYY-MM-DD" 
            style="width: 100%"
            :disabled-date="disabledEditEndDate"
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
import { defineProps, defineEmits, ref, defineExpose, reactive } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { createPlan, updatePlan, deletePlan as deletePlanApi } from '../api/travelApi'

interface Plan {
  id: number
  title: string
  startDate: string
  endDate: string
}
defineProps({
  plans: {
    type: Array as () => Plan[], // 类型断言为 Plan 数组
    default: () => [] // 默认空数组
  },
  selectedPlanId: {
    type: [Number, null],
    default: null
  }
});
const emit = defineEmits(['plan-selected', 'plan-updated', 'plan-deleted', 'plan-created', 'export-markdown'])

// 创建计划弹窗
const createDialogVisible = ref(false)
const createFormRef = ref()
const createLoading = ref(false)
const createForm = reactive({
  title: '',
  startDate: '',
  endDate: ''
})
const createRules = {
  title: [
    { required: true, message: '请输入计划名称', trigger: 'blur' }
  ],
  startDate: [
    { required: true, message: '请选择开始日期', trigger: 'change' }
  ],
  endDate: [
    { required: true, message: '请选择结束日期', trigger: 'change' }
  ]
}

// 编辑计划弹窗
const editDialogVisible = ref(false)
const editForm = ref({
  id: 0,
  title: '',
  startDate: '',
  endDate: ''
})

// 创建弹窗的结束日期禁用逻辑
const disabledCreateEndDate = (time: Date) => {
  if (!createForm.startDate) return false
  const startDateObj = new Date(createForm.startDate)
  return time.getTime() < startDateObj.getTime()
}

// 编辑弹窗的结束日期禁用逻辑
const disabledEditEndDate = (time: Date) => {
  if (!editForm.value.startDate) return false
  const startDateObj = new Date(editForm.value.startDate)
  return time.getTime() < startDateObj.getTime()
}

// 打开创建弹窗
const openCreateDialog = () => {
  createForm.title = ''
  createForm.startDate = ''
  createForm.endDate = ''
  createDialogVisible.value = true
}

// 提交创建
const submitCreate = async () => {
  if (!createFormRef.value) return
  await createFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      createLoading.value = true
      try {
        await createPlan({
          title: createForm.title,
          startDate: createForm.startDate,
          endDate: createForm.endDate
        })
        createDialogVisible.value = false
        emit('plan-created')
      } catch (error) {
        console.error(error)
      } finally {
        createLoading.value = false
      }
    }
  })
}

const exportDialogVisible = ref(false)
const exportMarkdownContent = ref('')

const selectPlan = (plan: Plan) => {
  emit('plan-selected', plan)
}

const exportMarkdown = (plan: Plan) => {
  emit('export-markdown', plan)
}

const copyToClipboard = async () => {
  try {
    await navigator.clipboard.writeText(exportMarkdownContent.value)
    ElMessage.success('已复制到剪贴板！')
  } catch (error) {
    ElMessage.error('复制失败，请手动复制')
  }
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
    editDialogVisible.value = false
    emit('plan-updated')
  } catch (error) {
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
    emit('plan-deleted')
  } catch (error) {
  }
}

// 暴露给父组件的方法和属性
defineExpose({
  exportDialogVisible,
  exportMarkdownContent
})
</script>

<style scoped>
.plan-list-card {
  margin-bottom: 20px;
}

.header-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
