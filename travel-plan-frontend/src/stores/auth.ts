import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as apiLogin, register as apiRegister, getCurrentUser, type UserInfo } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const userInfo = ref<UserInfo | null>(JSON.parse(localStorage.getItem('user') || 'null'))

  const isLoggedIn = computed(() => !!token.value)

  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const setUserInfo = (info: UserInfo) => {
    userInfo.value = info
    localStorage.setItem('user', JSON.stringify(info))
  }

  const login = async (email: string, password: string) => {
    const response = await apiLogin({ email, password })
    setToken(response.data)
    await fetchUserInfo()
  }

  const register = async (email: string, nickname: string, password: string, verifyCode: string) => {
    await apiRegister({ email, nickname, password, verifyCode })
  }

  const fetchUserInfo = async () => {
    try {
      const response = await getCurrentUser()
      setUserInfo(response.data)
    } catch {
      logout()
    }
  }

  const logout = () => {
    token.value = null
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  // 初始化时检查登录状态
  if (token.value) {
    fetchUserInfo()
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    login,
    register,
    logout,
    fetchUserInfo
  }
})
