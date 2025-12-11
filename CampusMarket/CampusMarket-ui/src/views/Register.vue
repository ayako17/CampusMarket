<template>
    <div class="register-container">
      <div class="register-background"></div>
      <el-card class="register-card">
        <div class="register-header">
          <h2>注册账号</h2>
          <p class="subtitle">加入Campus Market校园知识流动系统</p>
        </div>
  
        <el-form :model="registerForm" :rules="rules" ref="registerFormRef" class="register-form">
          <el-form-item prop="username">
            <el-input 
              v-model="registerForm.username" 
              placeholder="用户名" 
              prefix-icon="User"
              class="custom-input"
            />
          </el-form-item>
  
          <el-form-item prop="password">
            <el-input 
              v-model="registerForm.password" 
              type="password" 
              placeholder="密码" 
              prefix-icon="Lock"
              show-password
              class="custom-input"
            />
          </el-form-item>
  
          <el-form-item>
            <el-button 
              type="primary" 
              :loading="loading" 
              @click="handleRegister" 
              class="register-button"
            >
              注册
            </el-button>
          </el-form-item>
  
          <el-form-item>
            <el-button 
              type="text" 
              @click="goToLogin" 
              class="login-link"
            >
              已有账号？点击登录
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </template>
  
  <script setup>
  import { ref, reactive } from 'vue'
  import { useRouter } from 'vue-router'
  import { ElMessage } from 'element-plus'
  import { authApi } from '../api/auth'
  
  const router = useRouter()
  const registerFormRef = ref(null)
  const loading = ref(false)
  
  const registerForm = reactive({
    username: '',
    password: ''
  })
  
  const rules = {
    username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
    password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
  }
  
  const handleRegister = async () => {
    if (!registerFormRef.value) return
  
    try {
      await registerFormRef.value.validate()
      loading.value = true
  
      const response = await authApi.register(registerForm.username, registerForm.password)
      const { message } = response.data
  
      ElMessage.success(message || '注册成功')
      router.push('/login') // 跳转到登录页面
    } catch (error) {
      console.error('注册失败:', error)
      ElMessage.error(error.response?.data?.message || '注册失败，请稍后再试')
    } finally {
      loading.value = false
    }
  }
  
  const goToLogin = () => {
    router.push('/login')
  }
  </script>
  
  <style scoped>
  .register-container {
    height: 100vh;
    display: flex;
    justify-content: center;
    align-items: center;
    position: relative;
    overflow: hidden;
  }
  
  .register-background {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: url('/src/assets/images/campus-bg.jpg') center/cover no-repeat;
    z-index: 0;
    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.4);
    }
  }
  
  .register-card {
    width: 400px;
    padding: 30px;
    border-radius: 15px;
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2);
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(10px);
    z-index: 1;
    animation: slideUp 0.5s ease-out;
  }
  
  .register-header {
    text-align: center;
    margin-bottom: 30px;
  }
  
  .register-header h2 {
    margin-bottom: 10px;
    color: #409EFF;
    font-size: 28px;
    font-weight: 600;
  }
  
  .subtitle {
    color: #666;
    font-size: 16px;
    margin-top: 8px;
  }
  
  .register-form {
    margin-top: 20px;
  }
  
  .custom-input :deep(.el-input__wrapper) {
    border-radius: 8px;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
    transition: all 0.3s ease;
  }
  
  .custom-input :deep(.el-input__wrapper:hover) {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
  
  .register-button {
    width: 100%;
    height: 44px;
    border-radius: 8px;
    font-size: 16px;
    font-weight: 500;
    transition: all 0.3s ease;
  }
  
  .register-button:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  }
  
  .login-link {
    width: 100%;
    text-align: center;
    color: #409EFF;
    font-size: 14px;
    transition: all 0.3s ease;
  }
  
  .login-link:hover {
    color: #66b1ff;
    transform: translateY(-1px);
  }
  
  @keyframes slideUp {
    from {
      opacity: 0;
      transform: translateY(20px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }
  </style>