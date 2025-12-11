// src/api/matches.js
import axios from 'axios';
import { useAuthStore } from '../stores/authStore'

const authStore = useAuthStore()

// 获取 token
const getToken = () => authStore.token

// --------------------------
// 匹配请求相关接口
// --------------------------

/**
 * 获取用户的匹配请求（收到的/发出的）
 * @param {string} type - 'received' 或 'sent'
 * @param {number} page
 * @param {number} size
 */
export const getMatchRequests = async (type, page = 0, size = 10) => {
    try {
        const response = await axios.get(`/matches/requests`, {
            params: {
                type,
                page,
                size
            },
            headers: { Authorization: `Bearer ${getToken()}` }
        });
        return response.data;
    } catch (error) {
        throw new Error(error.response?.data?.message || '获取匹配请求失败');
    }
};

/**
 * 处理匹配请求（同意/拒绝）
 * @param {number} partnerId - 帖子ID
 * @param {number} matchId - 匹配记录ID
 * @param {boolean} accept - 是否接受
 */

// 修改 processMatchRequest 的路径，与后端匹配
export const processMatchRequest = async (partnerId, matchId, accept) => {
    try {
        console.log('发起处理请求:', { partnerId, matchId, accept });
        const response = await axios.post(
            `/matches/${partnerId}/requests/${matchId}`,
            {}, // 空对象，因为accept是通过URL参数传递
            {
                params: { accept }, // 将accept作为查询参数
                headers: { Authorization: `Bearer ${getToken()}` }
            }
        );
        return response;
    } catch (error) {
        console.error('处理请求API错误:', error.response?.data || error.message);
        throw error;
    }
};

/**
 * 检查是否已匹配某个帖子
 * @param {number} partnerId - 帖子ID
 */
export const checkIfMatched = async (partnerId) => {
    try {
        const response = await axios.get(`/matches/check`, {
            params: { partnerId },
            headers: { Authorization: `Bearer ${getToken()}` }
        });
        return response.data;
    } catch (error) {
        throw new Error(error.response?.data?.message || '检查匹配状态失败');
    }
};

/**
 * 申请匹配帖子
 * @param {number} postId - 帖子ID
 */
export const createMatch = async (postId) => {
    try {
        const response = await axios.post(
            `/matches/${postId}/apply`,
            null,
            { headers: { Authorization: `Bearer ${getToken()}` } }
        );
        return response.data;
    } catch (error) {
        throw new Error(error.response?.data?.message || '申请匹配失败');
    }
};