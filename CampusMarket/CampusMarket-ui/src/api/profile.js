import axios from 'axios'
import { API_BASE_URL, API_ENDPOINTS } from './config'

// 创建axios实例
const apiClient = axios.create({
    baseURL: API_BASE_URL,
    timeout: 5000,
    headers: {
      'Content-Type': 'application/json'
    }
  })
  

export const profileApi = {
  getProfile(userId) {
    return apiClient.get(`/profile/${userId}`) // 获取用户的个人资料
  },
  updateProfile(profile) {
    return apiClient.post('/profile/update', profile) // 更新或创建用户的个人资料
  },
  getUserBooks(userId){
    return apiClient.get(`/books/user/${userId}`) // 获取用户发布的教材
  },
  deleteBook(bookId) {
    return apiClient.delete(`/books/${bookId}`) // 删除教材
  },
  updateBook(book) {
    return apiClient.post(`/books`, book) // 更新教材
  },
  getUsernameById(userId) {
    return apiClient.get(`/auth/getUsernameById`, { params: { userId } }); // 通过用户 ID 获取用户名
  },
}