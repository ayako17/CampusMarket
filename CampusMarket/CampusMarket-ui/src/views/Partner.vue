<template>
  <div class="partner-container">
    <el-card class="partner-card">
      <template #header>
        <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
          <h3 class="title-text">找搭子</h3>
          <div style="display: flex; gap: 10px;">
            <el-button type="info" @click="showMessageModal = true">我的消息</el-button>
            <el-button type="success" @click="showInterestModal = true">我的收藏</el-button>
            <el-button type="primary" class="new-post-btn" @click="dialogVisible = true" round>发布新帖</el-button>
          </div>
        </div>

      </template>

      <!-- 筛选 -->
      <div class="filter-bar">
        <el-input v-model="searchSubject" placeholder="按主题搜索" prefix-icon="el-icon-search" class="filter-input"
          @input="handleSearch" clearable />
        <el-select v-model="statusFilter" placeholder="筛选状态" @change="handleStatusFilter" class="filter-select">
          <el-option label="全部" value="" />
          <el-option label="活跃" value="ACTIVE" />
          <el-option label="已匹配" value="MATCHED" />
        </el-select>
      </div>

      <!-- 帖子列表 -->
      <div class="post-list">
        <el-empty v-if="posts.length === 0 && !loading" description="暂无内容" />
        <el-skeleton v-if="loading" :rows="5" animated />

        <el-card v-for="post in posts" :key="post.id" class="post-item" shadow="hover">
          <div class="post-header">
            <div class="user-info">
              <el-avatar :src="post.avatar || defaultAvatar" :size="40" />
              <span class="username">{{ post.username }}</span>
            </div>
            <span class="post-time">{{ post.createTime }}</span>
          </div>

          <div class="post-content">
            <h4>{{ post.title }}</h4>
            <p>{{ post.content }}</p>
          </div>

          <div class="post-tag">
            <span class="tag-box">{{ post.tag }}</span>
          </div>

          <div class="post-footer">
            <el-button type="primary" text @click="contact(post)"
              :disabled="post.userId === currentUser?.id || post.status !== 'ACTIVE'">
              {{ post.userId === currentUser?.id ? '自己的请求' : '申请匹配' }}
            </el-button>
            <el-button type="info" text @click="viewDetails(post.id)">查看详情</el-button>
            <el-button type="danger" text @click="deletePartnerRequest(post.id)"
              :disabled="post.userId !== currentUser?.id">删除</el-button>
            <el-button type="warning" text @click="toggleInterest(post)">
              {{ post.interested ? '取消收藏' : '收藏' }}
            </el-button>
          </div>
          <!-- 添加匹配状态显示 -->
          <div class="post-match-status" v-if="post.matchedCount > 0 || post.userId === currentUser?.id">
            <el-tag type="info">
              已匹配: {{ post.matchedCount }} / {{ post.targetCount }}
            </el-tag>
          </div>
        </el-card>
      </div>

      <el-pagination v-if="totalPages > 1" :model-value="currentPage" :total="totalItems" :page-size="pageSize"
        layout="prev, pager, next" class="pagination" @update:model-value="handlePageChange" />
    </el-card>

    <!-- 我的消息 -->
    <el-dialog v-model="showMessageModal" title="我的消息" width="80vw" :close-on-click-modal="false">
      <el-tabs v-model="messageActiveTab">
        <el-tab-pane label="收到的请求" name="received">
          <div v-if="receivedRequests.length === 0" class="empty-message">
            <el-empty description="暂无收到的匹配请求" />
          </div>
          <el-card v-for="request in receivedRequests" :key="request.id" class="request-item">
            <div class="request-header">
              <div class="user-info">
                <!--el-avatar :src="request.matcher.avatar || defaultAvatar" :size="40" /-->
                <span class="username">{{ request.matcher.username }}</span>
              </div>
              <span class="request-time">{{ new Date(request.matchedAt).toLocaleString() }}</span>
            </div>
            <div class="request-content">
              <p>申请匹配您的帖子: <strong>{{ request.partner.title }}</strong></p>
            </div>
            <div class="request-footer" v-if="request.status === 'PENDING'">
              <el-button type="success" @click="processRequest(request.id, true)">同意</el-button>
              <el-button type="danger" @click="processRequest(request.id, false)">拒绝</el-button>
            </div>
            <div class="request-status" v-else>
              <el-tag :type="request.status === 'ACCEPTED' ? 'success' : 'danger'">
                {{ request.status === 'ACCEPTED' ? '已同意' : '已拒绝' }}
              </el-tag>
            </div>
          </el-card>
        </el-tab-pane>

        <el-tab-pane label="发出的请求" name="sent">
          <div v-if="sentRequests.length === 0" class="empty-message">
            <el-empty description="暂无发出的匹配请求" />
          </div>
          <el-card v-for="request in sentRequests" :key="request.id" class="request-item">
            <div class="request-header">
              <div class="user-info">
                <!--el-avatar :src="request.partner.user.avatar || defaultAvatar" :size="40" /-->
                <span class="username">{{ request.partner.user.username }}</span>
              </div>
              <span class="request-time">{{ new Date(request.matchedAt).toLocaleString() }}</span>
            </div>
            <div class="request-content">
              <p>您申请的帖子: <strong>{{ request.partner.title }}</strong></p>
            </div>
            <div class="request-status">
              <el-tag :type="request.status === 'ACCEPTED' ? 'success' :
                request.status === 'REJECTED' ? 'danger' : 'warning'">
                {{ request.status === 'ACCEPTED' ? '已通过' :
                  request.status === 'REJECTED' ? '已拒绝' : '待处理' }}
              </el-tag>
            </div>
          </el-card>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>


    <!-- 收藏列表 -->
    <el-dialog v-model="showInterestModal" title="我收藏的帖子" width="80vw" :close-on-click-modal="false"
      class="custom-dialog">
      <div style="max-height: 60vh; overflow-y: auto;">

        <el-card v-for="post in interestedPosts" :key="post.id" class="post-item" shadow="hover">
          <div class="post-header">
            <div class="user-info">
              <el-avatar :src="post.avatar || defaultAvatar" :size="40" />
              <span class="username">{{ post.username }}</span>
            </div>
            <span class="post-time">{{ post.createTime }}</span>
          </div>

          <div class="post-content">
            <h4>{{ post.title }}</h4>
            <p>{{ post.content }}</p>
          </div>

          <div class="post-tag">
            <span class="tag-box">{{ post.tag }}</span>
          </div>

          <div class="post-footer">
            <el-button type="info" text @click="viewDetails(post.id)">查看详情</el-button>
            <el-button type="primary" text @click="contact(post)"
              :disabled="post.userId === currentUser?.id || post.status !== 'ACTIVE'">
              {{ post.userId === currentUser?.id ? '自己的请求' : '申请匹配' }}
            </el-button>
            <el-button type="danger" text @click="handleToggleInterest(post)">取消收藏</el-button>
          </div>

          <!-- 添加匹配状态显示 -->
          <div class="post-match-status" v-if="post.matchedCount > 0 || post.userId === currentUser?.id">
            <el-tag type="info">
              已匹配: {{ post.matchedCount }} / {{ post.targetCount }}
            </el-tag>
          </div>
        </el-card>
      </div>
      <!-- 分页器 -->
      <div style="margin-top: 16px; text-align: right;width: 100%">
        <el-pagination background layout="prev, pager, next" :current-page="interestPage" :page-size="interestPageSize"
          :total="totalInterest" @current-change="handleInterestPageChange" />
      </div>
    </el-dialog>

    <!-- 发布新帖 -->
    <el-dialog v-model="dialogVisible" title="发布新帖" width="80vw" :close-on-click-modal="false" class="custom-dialog">
      <el-form :model="newPost" label-width="70px" class="post-form">
        <el-form-item label="标题">
          <el-input v-model="newPost.title" placeholder="请输入标题" class="input-field" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="newPost.description" type="textarea" :rows="4" placeholder="请输入描述" class="input-field" />
        </el-form-item>
        <el-form-item label="主题">
          <el-select v-model="newPost.subject" placeholder="请选择主题" class="input-field">
            <el-option label="学习" value="学习" />
            <el-option label="运动" value="运动" />
            <el-option label="娱乐" value="娱乐" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间">
          <el-input v-model="newPost.preferredTime" placeholder="例如：周六下午两点" class="input-field" />
        </el-form-item>
        <el-form-item label="联系方式">
          <el-input v-model="newPost.contactMethod" placeholder="请输入联系方式" class="input-field" />
        </el-form-item>
        <el-form-item label="目标人数">
          <el-input-number v-model="newPost.targetCount" :min="1" :max="20" placeholder="请输入需要匹配的人数" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPost">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useRouter } from 'vue-router';
import { authApi } from '../api/auth.js';
import { profileApi } from '@/api/profile.js';
import {
  getPartners, createPartner, deletePartner, togglePartnerInterest,
  getPartnerInterests
} from '../api/partner.js';
import {
  getMatchRequests,
  processMatchRequest,
  createMatch,
  checkIfMatched
} from '../api/matches.js';
import { useAuthStore } from '../stores/authStore'

const authStore = useAuthStore()

// 状态和数据
const posts = ref([]);
const currentUser = ref(null);
const loading = ref(false);
const searchSubject = ref('');
const statusFilter = ref('');
const currentPage = ref(1);
const pageSize = ref(10);
const totalItems = ref(0);
const totalPages = ref(0);
const dialogVisible = ref(false);
const defaultAvatar = '/images/icons_mine1.svg';
const router = useRouter();
const activePartnerRequests = ref([]);

const showInterestModal = ref(false);
const interestedPosts = ref([]);
const interestPage = ref(1);
const interestPageSize = ref(6);
const totalInterest = ref(0);
//匹配部分的信息
const showMessageModal = ref(false);
const messageActiveTab = ref('received');
const receivedRequests = ref([]);
const sentRequests = ref([]);
const processingRequestId = ref(null);


const newPost = ref({
  title: '',
  description: '',
  subject: '',
  preferredTime: '',
  contactMethod: '',
  targetCount: 1 // 默认值为1
});

// 获取当前用户信息
const fetchCurrentUser = async () => {
  const token = authStore.token;
  if (!token) {
    await router.push('/login');
    return;
  }
  try {
    const res = await authApi.getUserInfo('/api/auth/user-info', {
      headers: {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });
    currentUser.value = res.data; // 确保返回格式为 { id, username, role }
  } catch (error) {
    if (error.response?.status === 401) {
      ElMessage.error('登录已过期，请重新登录');
      authStore.clearToken();
      await router.push('/login');
    } else {
      console.error('获取用户信息失败:', error.response || error);
      ElMessage.error(error.response?.data?.message || '获取用户信息失败');
    }
  }
};

const fetchUserAvatar = async (userId) => {
  try {
    const response = await profileApi.getProfile(userId)
    return response.data.avatarUrl
  } catch (error) {
    console.error(`获取用户头像失败 (userId: ${userId})`, error)
    return '/images/icons_mine1.svg'
  }
}

// 获取帖子列表（使用封装的 getPartners）
const fetchPosts = async () => {
  loading.value = true;
  try {
    const res = await getPartners({
      subject: searchSubject.value,
      status: statusFilter.value,
      page: currentPage.value - 1,
      size: pageSize.value
    });

    console.log('API响应数据:', res); // 添加调试日志

    // 确保响应数据结构正确
    if (!res || !Array.isArray(res.content)) {
      throw new Error('无效的响应格式');
    }

    const postsWithAvatar = await Promise.all(
      res.content.map(async post => {
        const avatar = post.user?.id
          ? await fetchUserAvatar(post.user.id)
          : defaultAvatar;
        return {
          id: post.id,
          userId: post.user?.id,
          username: post.user?.username || '未知用户',
          title: post.title,
          content: post.description,
          tag: post.subject,
          createTime: post.createdAt ? new Date(post.createdAt).toLocaleString() : '未知时间',
          status: post.status,
          matchedCount: post.matchedCount || 0,
          targetCount: post.targetCount || 1,
          avatar
        };
      })
    );
    posts.value = postsWithAvatar;

    totalItems.value = res.totalElements || 0;
    totalPages.value = res.totalPages || 1;
  } catch (error) {
    console.error('获取帖子失败详情:', {
      error: error.message,
      stack: error.stack,
      response: error.response?.data
    });
    ElMessage.error(error.message || '获取帖子失败');
  } finally {
    loading.value = false;
  }
};

// 提交新帖子（使用封装的 createPartner）
const submitPost = async () => {
  if (!currentUser.value?.id) {
    return ElMessage.error('请先登录');
  }

  const {
    title = '',
    description = '',
    subject = '',
    preferredTime = '',
    contactMethod = '' // 添加默认值
  } = newPost.value;

  if (!title || !description || !subject || !preferredTime || !contactMethod) {
    return ElMessage.error('请填写完整信息');
  }

  try {
    await createPartner({
      ...newPost.value
      //userId: currentUser.value.id
    });
    ElMessage.success('发布成功');
    dialogVisible.value = false;
    await fetchPosts();
    newPost.value = { title: '', description: '', subject: '', preferredTime: '', contactMethod: '' };
  } catch (error) {
    const message = error.response?.data?.message || '发布失败，请检查网络或数据格式';
    ElMessage.error(message); // 显示后端错误详情
    console.error('提交失败详情:', error);
  }
};

const fetchMatchRequests = async () => {

  try {
    const [resReceived, resSent] = await Promise.all([
      getMatchRequests('received', 0, 10),
      getMatchRequests('sent', 0, 10)
    ]);
    console.log('收到的请求:', resReceived); // 调试日志
    console.log('发出的请求:', resSent);    // 调试日志

    receivedRequests.value = resReceived.content || [];
    sentRequests.value = resSent.content || [];
  } catch (error) {
    ElMessage.error(error.message || '获取匹配请求失败');
  }
};
//   // 使用封装好的API方法
//   const resReceived = await getPartnerMatches('received');
//   receivedRequests.value = resReceived.content || [];

//   const resSent = await getPartnerMatches('sent');
//   sentRequests.value = resSent.content || [];
// } catch (error) {
//   ElMessage.error(error.message || '获取匹配请求失败');
// }


// 修改 processRequest 方法
const processRequest = async (requestId, accept) => {
  processingRequestId.value = requestId;

  try {
    const request = receivedRequests.value.find(r => r.id === requestId);
    if (!request || request.status !== 'PENDING') {
      return; // 防止重复处理
    }
    console.log('处理请求参数:', {
      partnerId: request.partner.id,
      matchId: requestId,
      accept
    });

    const response = await processMatchRequest(
      request.partner.id,
      requestId,
      accept
    );

    console.log('处理请求响应:', response);

    // 更新本地数据
    const index = receivedRequests.value.findIndex(r => r.id === requestId);
    if (index !== -1) {
      receivedRequests.value[index] = response.data.updatedMatch;
    }

    // 更新帖子列表中的匹配状态
    const postIndex = posts.value.findIndex(p => p.id === request.partner.id);
    if (postIndex !== -1) {
      posts.value[postIndex].matchedCount = response.data.updatedPartner.matchedCount;
    }

    ElMessage.success(response.data.message);
  } catch (error) {
    console.error('处理请求失败:', {
      error: error.response?.data || error.message,
      stack: error.stack
    });
    ElMessage.error(error.response?.data?.message || '处理请求失败');
  } finally {
    processingRequestId.value = null;
  }
  fetchMatchRequests();
};

// 修改联系方法为申请匹配
const contact = async (post) => {

  if (post.status !== 'ACTIVE') {
    return ElMessage.warning('该帖子已关闭，无法匹配');
  }

  if (post.userId === currentUser.value?.id) {
    return ElMessage.warning('不能匹配自己的请求');
  }

  try {
    // 检查是否已申请
    const isMatched = (await checkIfMatched(post.id)) === true;
    if (isMatched) {
      return ElMessage.warning('您已经申请过该请求');
    }

    // 确认申请
    const confirm = await ElMessageBox.confirm(
      '确定要申请匹配此请求吗？',
      '匹配确认',
      { type: 'warning' }
    );

    if (confirm) {
      await createMatch(post.id);
      ElMessage.success('申请已发送，等待对方确认');
      await fetchPosts(); // 刷新列表
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || error.message || '申请失败');
    }
  }
};

// // 获取当前用户的活动伙伴请求
// const fetchActivePartnerRequests = async () => {
//   try {
//     const res = await getMyPartners({
//       userId: currentUser.value.id,
//       page: currentPage.value - 1,
//       size: pageSize.value
//     });
//     activePartnerRequests.value = res.content;
//   } catch (error) {
//     ElMessage.error(error.message);
//   }
// };

// 删除帖子（使用封装的 deletePartner）
const deletePartnerRequest = async (partnerId) => {
  try {
    await deletePartner(partnerId);
    ElMessage.success('删除成功');
    await fetchPosts(); // 刷新列表
  } catch (error) {
    console.error('删除失败:', error);
    ElMessage.error(error.message || '删除失败');
  }
};

// 页面导航方法
const viewDetails = (id) => {
  router.push({ name: 'PostDetail', params: { id } })
}

// 筛选处理方法
const handleSearch = () => {
  currentPage.value = 1
  fetchPosts()
}

const handleStatusFilter = () => {
  currentPage.value = 1
  fetchPosts()
}

const handlePageChange = (page) => {
  currentPage.value = page
  fetchPosts()
}

// 生命周期钩子
onMounted(() => {
  fetchCurrentUser();
  fetchPosts();
  fetchMatchRequests();
})


// 点击收藏或取消收藏
const toggleInterest = async (post) => {
  try {
    await togglePartnerInterest(post.id);
    post.interested = !post.interested; // 切换状态

    // 如果是在收藏界面操作，同步更新收藏列表
    if (showInterestModal.value) {
      if (post.interested) {
        // 如果是收藏操作，将帖子添加到收藏列表
        await loadInterestedPosts(); // 重新加载确保数据最新
      } else {
        // 如果是取消收藏，从当前显示的收藏列表移除
        interestedPosts.value = interestedPosts.value.filter(p => p.id !== post.id);
      }
    }

    ElMessage.success(post.interested ? '已收藏' : '已取消收藏');
  } catch (err) {
    console.error('操作失败', err);
    ElMessage.error(post.interested ? '收藏失败' : '取消收藏失败');
  }

};

// 监听弹窗开启时获取收藏列表
watch(showInterestModal, async (val) => {
  if (val) {
    try {
      const data = await getPartnerInterests({ page: 0, size: 20 });
      interestedPosts.value = data.content || []; // 如果分页返回的是 content
    } catch (err) {
      console.error('获取收藏失败', err);
    }
  }
});

// 加载收藏数据
const loadInterestedPosts = async () => {
  try {
    const data = await getPartnerInterests({
      page: interestPage.value - 1,
      size: interestPageSize.value,
    });
    interestedPosts.value = await Promise.all(
      (data.content || []).map(async post => {
        const avatar = post.user?.id
          ? await fetchUserAvatar(post.user.id)
          : defaultAvatar;
        return {
          id: post.id,
          userId: post.user?.id,
          username: post.user?.username || '未知用户',
          title: post.title,
          content: post.description,
          tag: post.subject,
          createTime: post.createdAt ? new Date(post.createdAt).toLocaleString() : '未知时间',
          status: post.status,
          matchedCount: post.matchedCount || 0,
          targetCount: post.targetCount || 1,
          avatar
        };
      })
    );
    // 同步更新主界面的收藏状态
    posts.value.forEach(p => {
      const isInterested = interestedPosts.value.some(ip => ip.id === p.id);
      p.interested = isInterested;
    });
    totalInterest.value = data.totalElements;
  } catch (err) {
    console.error('加载收藏失败', err);
  }
};


watch(showInterestModal, async (val) => {
  if (val) {
    interestPage.value = 1;
    await loadInterestedPosts();
  }
});

// 页码改变时
const handleInterestPageChange = async (newPage) => {
  interestPage.value = newPage;
  await loadInterestedPosts();
};

const handleToggleInterest = async (post) => {
  try {
    await togglePartnerInterest(post.id);

    // 1. 从收藏列表移除
    interestedPosts.value = interestedPosts.value.filter(p => p.id !== post.id);

    // 2. 同步更新主界面对应帖子的状态
    const index = posts.value.findIndex(p => p.id === post.id);
    if (index !== -1) {
      posts.value[index].interested = false; // 标记为未收藏
    }

    ElMessage.success('已取消收藏');
  } catch (err) {
    console.error('取消收藏失败', err);
    ElMessage.error('取消收藏失败');
  }
};

// 监听消息弹窗关闭
watch(showMessageModal, async (val) => {
  if (!val) {
    // 弹窗关闭时刷新数据
    await fetchMatchRequests();
  }
});




</script>

<style scoped>
.partner-container {
  padding: 30px;
  background-color: #f6f8fb;
}

.partner-card {
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  border: none;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title-text {
  font-size: 26px;
  font-weight: 600;
  margin: 0;
}

.new-post-btn {
  padding: 6px 18px;
  font-size: 14px;
}

.filter-bar {
  display: flex;
  gap: 20px;
  margin: 20px 0;
}

.filter-input,
.filter-select {
  flex: 1;
}

.post-list {
  margin-top: 20px;
}

.post-item {
  margin-bottom: 24px;
  border-radius: 12px;
  transition: all 0.2s ease;
  border: 1px solid #dbeafe;
  background-color: #fff;
}

.post-item:hover {
  transform: scale(1.01);
  box-shadow: 0 4px 12px rgba(30, 64, 175, 0.12);
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.username {
  font-weight: 600;
  font-size: 16px;
}

.post-time {
  color: #999;
  font-size: 13px;
}

.post-content h4 {
  font-size: 18px;
  margin-bottom: 6px;
  color: #333;
}

.post-content p {
  color: #666;
  line-height: 1.6;
}

.post-tag {
  margin-top: 10px;
}

.tag-box {
  display: inline-block;
  background-color: #fff;
  color: #000;
  border: 1px solid #000;
  /* 黑色边框 */
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 6px;
  min-width: 40px;
  text-align: center;
}

.post-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 10px;
}

.pagination {
  margin-top: 30px;
  text-align: center;
}

:deep(.el-dialog) {
  border-radius: 14px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
}

/*弹窗宽度*/
.el-dialog__wrapper .el-dialog {
  max-width: 1200px;
}

/* 表单区域 */
.post-form {
  background-color: #e3f2fd;
  padding: 20px 24px;
  border-radius: 12px;
}

/* 表单项间距 */
.post-form .el-form-item {
  margin-bottom: 20px;
}

/* 输入框本体*/
.post-form .el-input .el-input__inner,
.post-form .el-textarea__inner,
.post-form .el-select .el-input__inner {
  border-radius: 8px;
}

/* 按钮组美化 */
:deep(.el-dialog__footer) {
  text-align: right;
  padding: 16px 20px;
  background-color: #f3f4f6;
  border-top: 1px solid #e5e7eb;
}

.request-item {
  margin-bottom: 10px;
}

.request-header {
  display: flex;
  justify-content: space-between;
}

.request-footer {
  display: flex;
  justify-content: flex-end;
}

/* 在style部分添加 */
.request-item {
  margin-bottom: 16px;
  transition: all 0.3s;
}

.request-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.request-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.request-time {
  color: #999;
  font-size: 13px;
}

.request-content {
  margin-bottom: 12px;
}

.request-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.request-status {
  display: flex;
  justify-content: flex-end;
}

.post-match-status {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
}

.empty-message {
  padding: 20px 0;
}
</style>