import axios from 'axios'

// 创建axios实例，配置统一请求根路径
const request = axios.create({
  baseURL: 'http://localhost:8080', // 后端服务地址
  timeout: 5000 // 请求超时时间
})

// 旅行计划相关接口
export const createPlan = (data: { title: string; startDate: string; endDate: string }) => 
  request.post('/api/travelPlan', data)

export const getPlanList = () => request.get('/api/travelPlan')

// 每日计划相关接口
export const addDailyPlan = (data: { planId: number; time: string; location: string; planDate: string }) => 
  request.post('/api/dailyPlan', data)

export const getDailyPlanList = (planId: number) => request.get(`/api/dailyPlan/${planId}`)