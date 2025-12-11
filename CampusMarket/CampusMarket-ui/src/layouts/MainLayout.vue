<template>
  <div class="layout-container">
    <el-container>
      <!-- 顶部标题栏 -->
      <el-header>
        <div class="header-content">
          <h2>Hex校园知识流动系统</h2>
          <!-- 头像区域 -->
          <el-dropdown trigger="hover" @command="handleCommand">
            <span class="avatar-wrapper">
            <el-avatar :src="userInfo.avatarUrl" :size="40" style="cursor: pointer" />
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人主页</el-dropdown-item>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-container>
        <!-- 左侧导航菜单 -->
        <el-aside width="200px">
          <el-menu :default-active="activeMenu" class="el-menu-vertical" :collapse="isCollapse" @select="handleSelect"
            router>
            <el-menu-item index="/home">
              <el-icon>
                <house />
              </el-icon>
              <span>主页</span>
            </el-menu-item>
            <el-menu-item index="/partner">
              <el-icon>
                <ChatDotRound />
              </el-icon>
              <span>找搭子</span>
            </el-menu-item>
            <el-sub-menu index="resources">
              <template #title>
                <el-icon>
                  <folder />
                </el-icon>
                <span>找资料</span>
              </template>
              <el-menu-item index="/resources">
                <el-icon>
                  <document />
                </el-icon>
                <span>资料库</span>
              </el-menu-item>
              <el-menu-item index="/my-resources">
                <el-icon>
                  <files />
                </el-icon>
                <span>我的资料</span>
              </el-menu-item>
            </el-sub-menu>
            <el-menu-item index="/book">
              <el-icon><shopping-cart /></el-icon>
              <span>易二手</span>
            </el-menu-item>
            <el-menu-item index="/message">
              <el-icon>
                <message />
              </el-icon>
              <span>发邮件</span>
            </el-menu-item>
            <el-menu-item index="/profile">
              <el-icon>
                <user />
              </el-icon>
              <span>个人中心</span>
            </el-menu-item>
          </el-menu>
        </el-aside>

        <!-- 主内容区 -->
        <el-main>
          <router-view></router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '@/api/auth'
import { profileApi } from '@/api/profile'
import { useAuthStore } from '../stores/authStore'
import {
  House,
  UserFilled,
  Folder,
  ShoppingCart,
  Message,
  User,
  Document,
  Files,
  ChatDotRound
} from '@element-plus/icons-vue'

const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()
const isCollapse = ref(false)

const token = ref(authStore.token|| '')
const userId = ref(authStore.userId||null)
const userInfo = reactive({
  avatarUrl: '',
})

// 获取用户的个人资料
const fetchProfile = async () => {
  const response = await profileApi.getProfile(userId.value)
  Object.assign(userInfo, response.data)
}

// 根据当前路由路径设置活动菜单项
const activeMenu = computed(() => route.path)

const handleCommand = (command) => {
  if (command === 'logout') {
    authStore.clearToken()
    ElMessage.success('已退出登录')
    router.push('/login')
  } else if (command === 'profile') {
    router.push('/profile')
  }
}

const handleSelect = (path) => {
  router.push(path)
}

onMounted(() => {
  fetchProfile()
})
</script>

<style scoped>
.layout-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  display: flex;
  flex-direction: column;
}

/* 顶部标题栏 */
.el-header {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 60px;
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  z-index:1000;
}

.header-content {
  height: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 左侧导航栏 */
.el-aside {
  position: fixed;
  top: 60px; /* 留出顶部标题栏的高度 */
  left: 0;
  width: 200px;
  height: calc(100vh - 60px); /* 减去顶部标题栏的高度 */
  background-color: #ffffff;
  border-right: 1px solid #e6e6e6;
  z-index: 1000;
}

.el-menu-vertical {
  height: 100%;
  border-right: none;
}

/* 主内容区 */
.el-main {
  margin-left: 180px; /* 留出左侧导航栏的宽度 */
  margin-top: 40px; /* 留出顶部标题栏的高度 */
  padding: 20px;
  background-color: #f5f5f5;
  min-height: calc(100vh - 60px); /* 减去顶部标题栏的高度 */
}
</style>