<template>
  <el-card class="daily-form-card">
    <template #header>
      <span>添加当日行程</span>
    </template>
    <el-form :model="form" :rules="rules" ref="formRef" label-width="60px">
      <el-form-item label="日期" prop="planDate">
        <el-date-picker
          v-model="form.planDate"
          type="date"
          placeholder="选择日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          style="width: 100%"
          :teleported="false"
        />
      </el-form-item>
      <el-form-item label="时间" prop="time">
        <el-time-picker
          v-model="form.time"
          placeholder="选择时间"
          format="HH:mm"
          value-format="HH:mm:ss"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="地点" prop="location">
        <el-input v-model="form.location" placeholder="请输入地点" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="submitForm" :loading="loading">
          添加行程
        </el-button>
        <el-button @click="resetForm">重置</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup lang="ts">
import { ref, reactive, defineProps } from 'vue'
import { ElMessage } from 'element-plus'
import { addDailyPlan } from '../api/travelApi'

const props = defineProps<{
  planId: number | null
}>()

const emit = defineEmits(['daily-added'])

const formRef = ref()
const loading = ref(false)

const form = reactive({
  planDate: '',
  time: '',
  location: ''
})

const rules = {
  planDate: [
    { required: true, message: '请选择日期', trigger: 'change' }
  ],
  time: [
    { required: true, message: '请选择时间', trigger: 'change' }
  ],
  location: [
    { required: true, message: '请输入地点', trigger: 'blur' }
  ]
}

const submitForm = async () => {
  if (!formRef.value) return
  
  if (!props.planId) {
    ElMessage.warning('请先选择一个旅行计划')
    return
  }
  
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      loading.value = true
      try {
        const data = {
          travelPlan: { id: props.planId },
          planDate: form.planDate,
          time: form.time,
          location: form.location
        }
        await addDailyPlan(data)
        ElMessage.success('行程添加成功！')
        resetForm()
        emit('daily-added')
      } catch (error) {
        ElMessage.error('添加失败，请重试')
        console.error(error)
      } finally {
        loading.value = false
      }
    }
  })
}

const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
}
</script>

<style scoped>
.daily-form-card {
  margin-bottom: 20px;
}
</style>