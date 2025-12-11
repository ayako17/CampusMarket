import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '../layouts/MainLayout.vue'
import Login from '../views/Login.vue'
import Home from '../views/Home.vue'
import Register from '../views/Register.vue'
import BookModule from '../views/BookModule.vue'
import Profile from '../views/Profile.vue'
import Message from '../views/Message.vue'
import { authApi } from '../api/auth'
import PostDetail from '../views/PostDetail.vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/authStore'


const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/',
    component: MainLayout,
    children: [
      {
        path: '',
        redirect: '/home'
      },
      {
        path: '/home',
        name: 'Home',
        component: Home,
        meta: { requiresAuth: true }
      },
      {
        path: '/partner',
        name: 'Partner',
        component: () => import('../views/Partner.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: '/resources',
        name: 'Resources',
        component: () => import('../views/Resources.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: '/my-resources',
        name: 'MyResources',
        component: () => import('../views/MyResources.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: '/book',
        name: 'BookModule',
        component: BookModule ,
        meta: { requiresAuth: true }
      },
      {
        path: '/profile',
        name: 'Profile',
        component: () => import('../views/Profile.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: '/post/:id',
        name: 'PostDetail',
        component: () => import('@/views/PostDetail.vue'),  // 动态导入推荐
        meta: { requiresAuth: true }
      },
      {
        path:'/message',
        name:'Message',
        component:Message,
        meta: { requiresAuth: true }
      },
      {
        path: '/other-profile/:userId',
        name: 'OtherProfile',
        component: () => import('@/views/OtherProfile.vue'),
        props: true, // 将路由参数作为组件的 props
      }
    ]
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()

  // 如果用户已登录且尝试访问登录页面，重定向到主页
  if (to.path === '/login' && authStore.token) {
    next('/home')
    return
  }

  // 如果路由需要认证且用户未登录，重定向到登录页面
  if (to.meta.requiresAuth && !authStore.token) {
    ElMessage('请登录')
    next('/login')
    return
  }

  // 如果有 token，进行验证
  if (authStore.token) {
    try {
      await authApi.getUserInfo(authStore.token) // 调用后端接口验证 token
    } catch (error) {
      // 如果验证失败，清除 token 并重定向到登录页面
      console.error('Token 验证失败:', error)
      authStore.clearToken()
      next({ path: '/login', query: { redirect: to.fullPath } })
      return
    }
  }

  next()
})

export default router
