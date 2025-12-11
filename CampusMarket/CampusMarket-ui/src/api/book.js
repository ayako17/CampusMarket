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

// 教材相关API
export const bookApi = {
  // 获取所有教材
  getAllBooks() {
    return apiClient.get('/books')
  },

  // 添加教材
  addBook(book) {
    return apiClient.post('/books', book)
  },

  // 搜索教材
  searchBooks(keyword) {
    return apiClient.get('/books/search', {
      params: { keyword }
    })
  },
    // 获取当前用户发布的教材
    getUserBooks(userId) {
      return apiClient.get(`/books/user/${userId}`)
    },
}