import { createRouter, createWebHistory } from 'vue-router'
import { isLoggedIn, verifyToken } from '@/api/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '',
      component: () => import('@/views/index.vue'),
      meta: {
        name: '主页面',
        requiresAuth: true
      }
    },
    {
      path: '/login',
      component: () => import('@/views/user/Login.vue'),
      meta: {
        name: '登录'
      }
    },
    {
      path: '/register',
      component: () => import('@/views/user/Register.vue'),
      meta: {
        name: '注册'
      }
    },
    {
      path: '/chat',
      component: () => import('@/views/user/ChatModel.vue'),
      meta: {
        name: 'AIChat',
        requiresAuth: true
      }
    },
    {
      path: '/temp',
      component: () => import('@/views/user/ChatTemple.vue'),
      meta: {
        name: 'temp',
        requiresAuth: true
      }
    },
    {
      path: '/test',
      component: () => import('@/views/user/Test.vue'),
      meta:{
        name: 'test',
        requiresAuth: true
      }
    },
    // 404页面
    {
      path: '/:pathMatch(.*)*',
      redirect: '/login'
    }
  ],
})

// 路由守卫 - 处理自动登录和权限验证
router.beforeEach(async (to, from, next) => {
  // 如果访问登录或注册页面，直接放行
  if (to.path === '/login' || to.path === '/register') {
    // 如果已经登录，重定向到首页
    if (isLoggedIn()) {
      next('/');
    } else {
      next();
    }
    return;
  }

  // 检查是否需要认证
  if (to.meta.requiresAuth) {
    // 检查是否有token
    if (isLoggedIn()) {
      try {
        // 验证token是否有效
        await verifyToken();
        next();
      } catch (error) {
        // token无效或已过期，跳转到登录页面
        next('/login');
      }
    } else {
      // 没有token，跳转到登录页面
      next('/login');
    }
  } else {
    // 不需要认证的页面，直接放行
    next();
  }
})

export default router
