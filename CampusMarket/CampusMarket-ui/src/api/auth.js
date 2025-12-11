import axios from 'axios'
import { API_BASE_URL, API_ENDPOINTS } from './config'
import { useAuthStore } from '../stores/authStore'

// 创建 axios 实例
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 5000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器 - 添加 token 到请求头
apiClient.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore()
    if (authStore.token) {
      config.headers['Authorization'] = `Bearer ${authStore.token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器 - 处理常见错误
apiClient.interceptors.response.use(
  (response) => {
    return response
  },
  (error) => {
    if (error.response) {
      const authStore = useAuthStore()
      if (error.response.status === 401) {
        authStore.clearToken() // 清除 Pinia 中的 token
        window.location.href = '/login' // 重定向到登录页面
      } else {
        console.error('请求失败', error.response.data || error.message)
      }
    }
    return Promise.reject(error)
  }
)

// 认证相关 API
export const authApi = {
  // 用户登录
  login(username, password, rememberMe = false) {
    return apiClient.post(API_ENDPOINTS.LOGIN, {
      username,
      password,
      rememberMe,
    }).then((response) => {
      const token = response.data.token
      const userId = response.data.id
      if (token&&userId) {
        const authStore = useAuthStore()
        authStore.setToken(token) // 将 token 存入 Pinia
        authStore.setUserId(userId)
      }
      return response
    })
  },

  // 获取用户信息
  getUserInfo() {
    return apiClient.get(API_ENDPOINTS.USER_INFO)
  },

  // 用户登出
  logout() {
    const authStore = useAuthStore()
    authStore.clearToken() // 清除 Pinia 中的 token
    window.location.href = '/login' // 重定向到登录页面
  },

  // 用户注册
  register(username, password) {
    return apiClient.post(API_ENDPOINTS.REGISTER, {
      username,
      password,
    })
  },
}