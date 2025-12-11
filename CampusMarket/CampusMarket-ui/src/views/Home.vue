<template>
  <div class="home-container">
    <!-- 主内容区 -->
    <div class="content-wrapper">
      <!-- 系统名称 -->
      <div class="title-section">
        <h1 class="main-title">Campus Market校园知识流动系统</h1>
        <p class="subtitle">
          一站式校园资源共享平台，助力学习生活更轻松。
          在这里，你可以找到志同道合的学习伙伴，共享优质学习资料，
          还能让闲置物品流转起来，让知识和价值充分流动。
        </p>
      </div>

      <!-- 功能按钮区 -->
      <div class="features-section">
        <el-row :gutter="30" justify="center">
          <el-col :span="8">
            <el-card class="feature-card" @click="router.push('/partner')">
              <el-icon class="feature-icon"><ChatDotRound /></el-icon>
              <h2>找搭子</h2>
              <p>寻找志同道合的学习伙伴</p>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card class="feature-card" @click="router.push('/resources')">
              <el-icon class="feature-icon"><folder /></el-icon>
              <h2>找资料</h2>
              <p>共享优质学习资源</p>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card class="feature-card" @click="router.push('/book')">
              <el-icon class="feature-icon"><shopping-cart /></el-icon>
              <h2>易二手</h2>
              <p>闲置物品自由交易</p>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { UserFilled, Folder, ShoppingCart } from '@element-plus/icons-vue'

const router = useRouter()
</script>

<style scoped>
.home-container {
  min-height: calc(100vh - 60px);
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  background-color: #f5f7fa;
}

.content-wrapper {
  max-width: 1200px;
  width: 100%;
  text-align: center;
}

.title-section {
  margin-bottom: 60px;
}

.main-title {
  font-size: 48px;
  color: #409EFF;
  margin-bottom: 20px;
  font-weight: bold;
  background: linear-gradient(120deg, #409EFF, #67C23A);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.subtitle {
  font-size: 16px;
  color: #606266;
  line-height: 1.8;
  max-width: 800px;
  margin: 0 auto;
}

.features-section {
  margin-top: 40px;
}

.feature-card {
  height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
  background-color: white;
}

.feature-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.feature-icon {
  font-size: 48px;
  margin-bottom: 16px;
  color: #409EFF;
}

.feature-card h2 {
  font-size: 24px;
  color: #303133;
  margin: 0 0 8px 0;
}

.feature-card p {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-title {
    font-size: 36px;
  }

  .subtitle {
    font-size: 14px;
    padding: 0 20px;
  }

  .feature-card {
    margin-bottom: 20px;
  }
}
</style>