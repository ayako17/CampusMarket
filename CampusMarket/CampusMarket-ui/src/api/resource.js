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

const uploadClient = axios.create({
    baseURL: API_BASE_URL,
    timeout: 30000, // 上传文件需要更长超时时间
    headers: {
        'Content-Type': 'multipart/form-data'
    }
})

export const resourceApi = {
    getAllNotes() {
        return apiClient.get('/notes')
    },

    addNotes(note) {
        return apiClient.post('/notes', note)
    },

    deleteNote(noteId) {
        return apiClient.delete(`/notes/${noteId}`)
    },

    updateNote(noteId, note) {
        return apiClient.put(`/notes/${noteId}`, note)
    },

    searchNotes(keyword) {
        return apiClient.get('/notes/search', {
            params: { keyword }
        })
    },

    getUserNotes(userId) {
        return apiClient.get(`/notes/user/${userId}`)
    },

    uploadFile(file) {
        const formData = new FormData()
        formData.append('file', file)
        return uploadClient.post('files/upload', formData)
    }
}