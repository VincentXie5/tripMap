<template>
  <el-card class="plan-form-card">
    <template #header>
      <span>创建旅行计划</span>
    </template>
    <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
      <el-form-item label="计划名称" prop="title">
        <el-input v-model="form.title" placeholder="请输入计划名称" />
      </el-form-item>
      <el-form-item label="开始日期" prop="startDate">
        <el-date-picker
          v-model="form.startDate"
          type="date"
          placeholder="选择开始日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="结束日期" prop="endDate">
        <el-date-picker
          v-model="form.endDate"
          type="date"
          placeholder="选择结束日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="submitForm" :loading="loading">
          创建计划
        </el-button>
        <el-button @click="resetForm">重置</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { createPlan } from '../api/travelApi'

const emit = defineEmits(['plan-created'])

const formRef = ref()
const loading = ref(false)

const form = reactive({
  title: '',
  startDate: '',
  endDate: ''
})

const rules = {
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

const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      loading.value = true
      try {
        await createPlan(form)
        ElMessage.success('旅行计划创建成功！')
        resetForm()
        emit('plan-created')
      } catch (error) {
        ElMessage.error('创建失败，请重试')
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
.plan-form-card {
  margin-bottom: 20px;
}
</style>