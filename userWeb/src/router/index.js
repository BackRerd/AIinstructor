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
      component: () => import('@/views/user/Chat.vue'),
      meta: {
        name: 'AIChat'
      }
    }
  ],
})

export default router
