import { createRouter, createWebHistory } from 'vue-router'
import WelcomePage from '../pages/WelcomePage/WelcomePage.vue'

// 路由表：定义欢迎页、主工作区与设置页入口
const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'welcome',
      component: WelcomePage
    },
    {
      path: '/main',
      name: 'main',
      component: () => import('../pages/MainWorkspace.vue')
    },
    {
      path: '/settings',
      name: 'settings',
      component: () => import('../pages/SettingsPage/SettingsPage.vue')
    }
  ]
})

export default router
