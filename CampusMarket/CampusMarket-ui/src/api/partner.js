import axios from 'axios';
import { useAuthStore } from '../stores/authStore'

const authStore = useAuthStore()

// 获取 token
const getToken = () => authStore.token

// 获取帖子列表
export const getPartners = async (params) => {
  try {
    // 构建符合DTO结构的查询参数
    const searchParams = {
      page: params.page,
      size: params.size,
      subject: params.subject,
      status: params.status
    };

    const response = await axios.get('/partners', {
      params: searchParams, // 直接传递DTO对象
      headers: {
        Authorization: `Bearer ${getToken()}`
      }
    });

    return response.data;
  } catch (error) {
    console.error('获取帖子列表失败:', {
      url: '/api/partners',
      params,
      status: error.response?.status,
      data: error.response?.data,
      message: error.message
    });

    // 传递更详细的错误信息
    const errorMsg = error.response?.data?.message
      || error.response?.data?.error
      || error.message
      || '获取帖子列表失败，请稍后重试';

    throw new Error(errorMsg);
  }
};

// 创建帖子
export const createPartner = async (postData) => {
  try {
    const response = await axios.post('/partners/create', postData, {
      headers: {
        Authorization: `Bearer ${getToken()}`,
        'Content-Type': 'application/json'
      },
      transformRequest: [
        (data) => JSON.stringify(data), // 手动序列化，避免 Axios 自动修改 Content-Type
      ],
    });
    return response.data;
  } catch (error) {
    console.error('创建帖子失败:', error);
    throw new Error('创建帖子失败，请稍后重试');
  }
};

// 删除帖子
// partner.js
export const deletePartner = async (partnerId) => {
  try {
    const response = await axios.delete(`/partners/${partnerId}`, {
      headers: {
        Authorization: `Bearer ${getToken()}`
      }
    });
    return response.data;
  } catch (error) {
    throw new Error(error.response?.data?.message || '删除帖子失败');
  }
};


// 收藏 / 取消收藏帖子
export const togglePartnerInterest = async (partnerId) => {
  try {
    await axios.post(`/partners/${partnerId}/partnerInterest`, null, {
      headers: {
        Authorization: `Bearer ${getToken()}`,
      },
    });
  } catch (error) {
    throw new Error('操作收藏失败');
  }
};

// 获取用户收藏的帖子
export const getPartnerInterests = async (params) => {
  const response = await axios.get('/partners/partnerInterests', {
    params: { page: 0, size: 20 }, // 分页参数
    headers: { Authorization: `Bearer ${getToken()}` }
  });
  return response.data; // 直接返回，结构与主页面一致
};


