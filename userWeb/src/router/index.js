import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '',
      component: () => import('@/views/index.vue'),
      meta: {
        name: '主页面'
      }
    },
    {
      path: '/chat',
      component: () => import('@/views/user/ChatModel.vue'),
      meta: {
        name: 'AIChat'
      }
    },
    {
      path: '/temp',
      component: () => import('@/views/user/ChatTemple.vue'),
      meta: {
        name: 'temp'
      }
    },
    {
      path: '/test',
      component: () => import('@/views/user/Test.vue'),
      meta:{
        name: 'test'
      }
    }
  ],
})

export default router
