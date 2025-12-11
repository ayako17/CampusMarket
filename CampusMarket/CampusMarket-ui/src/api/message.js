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
export const messageApi = {
    //发送邮件
    sendMessage(messageData) {
        return apiClient.post('/messages', messageData);
    },
    // 获取接受的所有邮件
    getSendMessage(senderId) {
        return apiClient.get(`/messages/sender/${senderId}`)
    },
    // 获取接受的所有邮件
    getReceiveMessage(receiverId) {
        return apiClient.get(`/messages/receiver/${receiverId}`)
    },
    //由用户名获取id
    getUserIdByUsername(username) {
        return apiClient.get('/auth/findByUsername', { params: { username } });
    },
    //标记已读
    markAsRead(messageId) {
        return apiClient.patch(`/messages/${messageId}/read`);
    },
    //获取通讯录
    getContacts(userId) {
        return apiClient.get(`/messages/contacts/${userId}`);
    },
    //获取来往邮件
    getConversation(userId, contactId) {
        return apiClient.get('/messages/conversation', { params: { userId, contactId } });
    },
    //删除邮件
    deleteMessage(messageId, userId, isSender) {
        return apiClient.delete(`/messages/${messageId}`, {
            params: { userId, isSender }
        });
    }
}