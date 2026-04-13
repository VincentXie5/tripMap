import axios from 'axios'
import type { AxiosInstance, AxiosResponse, AxiosError } from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiResponse } from '../types/api'

// 创建axios实例
const request: AxiosInstance = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 5000
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 可在此处添加Token等通用请求头
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器 - 统一处理ApiResult
request.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const res = response.data
    // 成功时直接返回data，便于业务代码直接使用
    return res.data
  },
  (error: AxiosError<ApiResponse>) => {
    // HTTP层面错误处理
    if (error.response) {
      const status = error.response.status
      const data = error.response.data
      
      // 优先使用后端返回的错误信息
      if (data?.message) {
        ElMessage.error(data.message)
      } else {
        switch (status) {
          case 400: ElMessage.error('请求参数错误'); break
          case 401: ElMessage.error('未授权，请重新登录'); break
          case 403: ElMessage.error('禁止访问'); break
          case 404: ElMessage.error('资源不存在'); break
          case 500: ElMessage.error('服务器内部错误'); break
          default: ElMessage.error('请求失败')
        }
      }
    } else {
      ElMessage.error('网络连接失败，请检查网络')
    }
    
    return Promise.reject(error)
  }
)

// ============ API接口定义 ============

// 旅行计划相关接口
export const createPlan = (data: { title: string; startDate: string; endDate: string }) => 
  request.post('/api/travelPlan', data)

export const getPlanList = () => request.get('/api/travelPlan')

export const updatePlan = (id: number, data: { title: string; startDate: string; endDate: string }) => 
  request.put(`/api/travelPlan/${id}`, data)

export const deletePlan = (id: number) => request.delete(`/api/travelPlan/${id}`)

// 每日计划相关接口
export const addDailyPlan = (data: { travelPlan: { id: number }; time: string; location: string; planDate: string; remark?: string; tag?: number }) => 
  request.post('/api/dailyPlan', data)

export const getDailyPlanList = (planId: number) => request.get(`/api/dailyPlan/${planId}`)

export const updateDailyPlan = (id: number, data: { travelPlan: { id: number }; time: string; location: string; planDate: string; remark?: string; tag?: number }) => 
  request.put(`/api/dailyPlan/${id}`, data)

export const deleteDailyPlan = (id: number) => request.delete(`/api/dailyPlan/${id}`)

export const updateDailyPlanSort = (planId: number, sortOrderList: { id: number; sortOrder: number }[]) => 
  request.put(`/api/dailyPlan/sort/${planId}`, sortOrderList)

// 地理编码相关接口
export const searchLocations = (keyword: string) => 
  request.get(`/api/geocode/search?keyword=${encodeURIComponent(keyword)}`)
