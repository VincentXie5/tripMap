import axios from 'axios'

const request = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 5000
})

// 请求拦截器 - 添加Token
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    return response.data
  },
  (error) => {
    // 处理业务错误，显示错误消息
    if (error.response?.data?.message) {
      // 错误消息已经在 error.response.data.message 中
      // 调用方可以根据需要显示
    }
    return Promise.reject(error)
  }
)

export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  email: string
  nickname: string
  password: string
  verifyCode: string
}

export interface UserInfo {
  id: number
  email: string
  nickname: string
  avatarType: string
  isActive: boolean
}

export const sendCode = (email: string) => {
  return request.post('/api/auth/send-code', { email })
}

export const register = (data: RegisterRequest) => {
  return request.post('/api/auth/register', data)
}

export const login = (data: LoginRequest) => {
  return request.post<{ data: string }>('/api/auth/login', data)
}

export const getCurrentUser = () => {
  return request.get<{ data: UserInfo }>('/api/auth/me')
}

export default request
