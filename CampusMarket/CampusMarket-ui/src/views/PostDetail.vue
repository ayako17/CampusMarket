<template>
  <div class="post-detail-container">
    <!-- 返回按钮 -->
    <el-button type="primary" icon="el-icon-arrow-left" @click="goBack" class="back-button">
      返回
    </el-button>

    <!-- 加载骨架 -->
    <el-skeleton :loading="isLoading" animated>
      <template #template>
        <el-card>
          <h3>加载中...</h3>
        </el-card>
      </template>

      <template #default>
        <!-- 错误提示 -->
        <el-alert v-if="error" :title="error" type="error" show-icon center style="margin-bottom: 20px;" />

        <!-- 帖子详情卡片 -->
        <el-card v-if="post" class="post-card">
          <div class="header">
            <el-avatar :size="64" :src="userAvatar" icon="el-icon-user-solid" class="avatar" @click="handleImageClick" />
            <div class="meta">
              <h2 class="title">{{ post.title }}</h2>
              <div class="user">{{ post.user?.username || '匿名用户' }}</div>
              <div class="time">发布时间：{{ formatTimestamp(post.createdAt) || '未知时间' }}</div>
            </div>
          </div>

          <el-divider />

          <div class="post-content">
            <p class="description">{{ post.description }}</p>

            <el-descriptions title="详细信息" :column="1" border>
              <el-descriptions-item label="主题">{{ post.subject }}</el-descriptions-item>
              <el-descriptions-item label="时间">{{ post.preferredTime }}</el-descriptions-item>
              <el-descriptions-item label="联系方式">{{ post.contactMethod }}</el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag type="success" v-if="post.status === 'MATCHED'">已匹配</el-tag>
                <el-tag type="info" v-else>活跃</el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>
      </template>
    </el-skeleton>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { profileApi } from '@/api/profile'

const route = useRoute()
const router = useRouter()
const postId = route.params.id

const post = ref(null)
const isLoading = ref(true)
const error = ref(null)

const fetchPostDetail = async () => {
  try {
    const response = await axios.get(`/partners/${postId}`)
    post.value = response.data
  } catch (err) {
    error.value = '获取帖子详情失败，请稍后重试。'
    console.error('获取帖子详情失败:', err)
  } finally {
    isLoading.value = false
  }
}

const userAvatar = ref(null)
const fetchUserAvatar = async (userId) => {
  try {
    const response = await profileApi.getProfile(userId)
    return response.data.avatarUrl
  } catch (error) {
    console.error(`获取用户头像失败 (userId: ${userId})`, error)
    return '/images/icons_mine1.svg'
  }
}

const handleImageClick = () => {
  viewOtherProfile(post.value.user.id)
}

const viewOtherProfile = (userId) => {
  router.push(`/other-profile/${userId}`)
}

// 格式化时间方法
const formatTimestamp = (timestamp) => {
  const date = new Date(timestamp);
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  }).format(date).replace(/\//g, '/');
};

onMounted(async () => {
  if (!postId) {
    error.value = '无效的帖子ID'
    isLoading.value = false
  } else {
    await fetchPostDetail()
    userAvatar.value =  await fetchUserAvatar(post.value.user.id)
  }
})

const goBack = () => {
  router.back()
}
</script>

<style scoped>
.post-detail-container {
  max-width: 900px;
  margin: 40px auto;
  padding: 20px;
}

.back-button {
  margin-bottom: 20px;
}

.post-card {
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.06);
}

.header {
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar {
  background-color: #409eff;
}

.meta {
  flex: 1;
}

.title {
  margin: 0;
  font-size: 24px;
  font-weight: bold;
}

.user {
  color: #666;
  margin-top: 4px;
}

.time {
  font-size: 13px;
  color: #999;
}

.post-content {
  margin-top: 20px;
}

.description {
  font-size: 16px;
  line-height: 1.8;
  color: #444;
  margin-bottom: 20px;
}
</style>
