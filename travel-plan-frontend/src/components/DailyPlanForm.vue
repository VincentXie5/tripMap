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
          :disabled-date="disabledDate"
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
        <div class="location-search-wrapper" ref="searchWrapperRef">
          <el-input
            v-model="form.location"
            placeholder="请输入地点"
            autocomplete="off"
            @input="handleLocationInput"
            @keydown="handleKeydown"
            @focus="handleFocus"
            ref="locationInputRef"
          />
          
          <!-- 搜索联想下拉框 -->
          <div 
            v-if="showDropdown && suggestionList.length > 0" 
            class="location-dropdown"
            @mousedown.prevent
          >
            <div 
              v-for="(item, index) in suggestionList" 
              :key="index"
              class="location-dropdown-item"
              :class="{ active: activeIndex === index }"
              @click="selectLocation(item)"
              @mouseenter="activeIndex = index"
            >
              <div class="location-name" v-html="highlightKeyword(item.name, searchKeyword)"></div>
              <div class="location-address" v-if="item.address">{{ item.address }}</div>
            </div>
          </div>
        </div>
      </el-form-item>
      <el-form-item label="标签">
        <el-radio-group v-model="form.tag" size="small">
          <el-radio :label="0">无标签</el-radio>
          <el-radio :label="1">🏛️ 景点</el-radio>
          <el-radio :label="2">🍜 美食</el-radio>
          <el-radio :label="3">🏨 住宿</el-radio>
          <el-radio :label="4">🚗 交通</el-radio>
          <el-radio :label="5">🛒 购物</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="备注">
        <el-input
          v-model="form.remark"
          type="textarea"
          :rows="3"
          placeholder="行程备注、注意事项、交通信息等（可选）"
          maxlength="1000"
          show-word-limit
        />
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
import { ref, reactive, defineProps, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { addDailyPlan, searchLocations } from '../api/travelApi'

const props = defineProps<{
  planId: number
  startDate?: string
  endDate?: string
}>()

// 计算日期选择器禁用日期
const disabledDate = (time: Date) => {
  const startStr = props.startDate
  const endStr = props.endDate
  if (!startStr || !endStr) return false
  try {
    // 使用本地日期，避免时区问题
    const year = time.getFullYear()
    const month = time.getMonth()
    const day = time.getDate()
    
    // 解析开始和结束日期的年月日
    const startParts = startStr.split('-')
    const startYear = parseInt(startParts[0])
    const startMonth = parseInt(startParts[1]) - 1
    const startDay = parseInt(startParts[2])
    
    const endParts = endStr.split('-')
    const endYear = parseInt(endParts[0])
    const endMonth = parseInt(endParts[1]) - 1
    const endDay = parseInt(endParts[2])
    
    // 比较年月日
    const dateNum = year * 10000 + month * 100 + day
    const startNum = startYear * 10000 + startMonth * 100 + startDay
    const endNum = endYear * 10000 + endMonth * 100 + endDay
    
    // 禁用范围外的日期
    return dateNum < startNum || dateNum > endNum
  } catch {
    return false
  }
}

const emit = defineEmits(['daily-added'])

const formRef = ref()
const loading = ref(false)
const locationInputRef = ref()
const searchWrapperRef = ref()

const form = reactive({
  planDate: '',
  time: '',
  location: '',
  remark: '',
  tag: 0
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

// 搜索联想相关状态
const suggestionList = ref<any[]>([])
const showDropdown = ref(false)
const activeIndex = ref(0)
const searchKeyword = ref('')
let debounceTimer: any = null
let searchCache = new Map<string, any>()
const CACHE_MAX_SIZE = 50
const CACHE_EXPIRE_MS = 24 * 60 * 60 * 1000 // 24小时

// 防抖搜索
const handleLocationInput = (value: string) => {
  searchKeyword.value = value
  
  clearTimeout(debounceTimer)
  
  // 输入小于3个字符不搜索
  if (!value || value.length < 2) {
    suggestionList.value = []
    showDropdown.value = false
    return
  }

  // 检查缓存
  const cached = searchCache.get(value)
  if (cached && Date.now() - cached.timestamp < CACHE_EXPIRE_MS) {
    suggestionList.value = cached.data
    activeIndex.value = 0
    showDropdown.value = true
    return
  }

  // 防抖延迟 800ms
  debounceTimer = setTimeout(() => {
    fetchLocationSuggestions(value)
  }, 800)
}

// 请求搜索接口
const fetchLocationSuggestions = async (keyword: string) => {
  try {
    const res = await searchLocations(keyword)
    if (res.data && Array.isArray(res.data)) {
      suggestionList.value = res.data
      activeIndex.value = 0
      showDropdown.value = true
      
      // 加入缓存
      if (searchCache.size >= CACHE_MAX_SIZE) {
        // 移除最早的缓存项
        const firstKey = searchCache.keys().next().value
        searchCache.delete(firstKey)
      }
      searchCache.set(keyword, {
        data: res.data,
        timestamp: Date.now()
      })
    }
  } catch (error) {
    // 接口失败静默处理，不影响用户输入
    console.debug('地点搜索接口请求失败，已降级', error)
    suggestionList.value = []
    showDropdown.value = false
  }
}

// 选择地点
const selectLocation = (item: any) => {
  form.location = item.name
  suggestionList.value = []
  showDropdown.value = false
  nextTick(() => {
    locationInputRef.value?.blur()
  })
}

// 键盘操作处理
const handleKeydown = (e: KeyboardEvent) => {
  if (!showDropdown.value || suggestionList.value.length === 0) return

  switch (e.key) {
    case 'ArrowDown':
      e.preventDefault()
      activeIndex.value = Math.min(activeIndex.value + 1, suggestionList.value.length - 1)
      break
    case 'ArrowUp':
      e.preventDefault()
      activeIndex.value = Math.max(activeIndex.value - 1, 0)
      break
    case 'Enter':
      e.preventDefault()
      if (activeIndex.value >= 0 && activeIndex.value < suggestionList.value.length) {
        selectLocation(suggestionList.value[activeIndex.value])
      }
      break
    case 'Escape':
      e.preventDefault()
      showDropdown.value = false
      break
  }
}

const handleFocus = () => {
  if (suggestionList.value.length > 0) {
    showDropdown.value = true
  }
}

// 关键词高亮
const highlightKeyword = (text: string, keyword: string) => {
  if (!keyword || !text) return text || ''
  const regex = new RegExp(`(${keyword})`, 'gi')
  return text.replace(regex, '<span class="highlight">$1</span>')
}

// 点击外部关闭下拉框
const handleClickOutside = (e: MouseEvent) => {
  if (searchWrapperRef.value && !searchWrapperRef.value.contains(e.target)) {
    showDropdown.value = false
  }
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
          const data: any = {
            travelPlan: { id: props.planId },
            planDate: form.planDate,
            time: form.time,
            location: form.location,
            remark: form.remark,
            tag: form.tag
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
  suggestionList.value = []
  showDropdown.value = false
  searchKeyword.value = ''
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
  clearTimeout(debounceTimer)
})
</script>

<style scoped>
.daily-form-card {
  margin-bottom: 20px;
}

.location-search-wrapper {
  position: relative;
  width: 100%;
}

.location-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  z-index: 1000;
  background: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
  max-height: 300px;
  overflow-y: auto;
  margin-top: 4px;
}

.location-dropdown-item {
  padding: 8px 12px;
  cursor: pointer;
  border-bottom: 1px solid #f5f7fa;
}

.location-dropdown-item:last-child {
  border-bottom: none;
}

.location-dropdown-item:hover,
.location-dropdown-item.active {
  background-color: #ecf5ff;
}

.location-name {
  font-size: 14px;
  color: #303133;
  line-height: 1.4;
}

.location-address {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
  line-height: 1.3;
}

.highlight {
  color: #409eff;
  font-weight: 600;
}
</style>