/**
 * 路由配置 (router/index.js)
 *
 * 作用：
 * 1. 定义应用的页面结构和 URL 映射规则。
 * 2. 配置路由模式（使用 HTML5 History 模式）。
 * 3. 管理基础路由（如登录页、404 页）和动态加载机制。
 */

import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '@/components/MainLayout.vue'

// 路由懒加载 (Lazy Loading)
// 作用：只有当用户访问特定页面时，才加载对应的 JS 代码块。
// 优点：显著减小首屏加载体积 (bundle size)，提高应用启动速度。
const Login = () => import('@/views/Login.vue')
const Dashboard = () => import('@/views/Dashboard.vue')
const Forbidden = () => import('@/views/403.vue')
const NotFound = () => import('@/views/NotFound.vue')

// 静态/常量路由配置
// 这些路由对所有用户可见，不需要通过后端权限接口动态下发。
export const routes = [
  {
    path: '/login',
    name: 'login',
    component: Login,
    // meta.public: true 标记该路由为公开路由，路由守卫会直接放行
    meta: { title: '用户登录', public: true },
  },
  {
    path: '/',
    name: 'Root',
    // MainLayout 是应用的骨架（包含侧边栏、顶部导航），所有业务页面都作为它的子路由渲染
    component: MainLayout,
    children: [], // 子路由将由 guard.js 根据用户权限动态添加
  },
  {
    path: '/403',
    name: '403',
    component: Forbidden,
    meta: { title: '权限不足', public: true },
  },
  {
    // 通配符路由：匹配所有未定义的路径
    // 必须放在路由列表的最后
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: NotFound,
    meta: { title: '页面未找到', public: true },
  },
]

// 创建 Router 实例
const router = createRouter({
  // 使用 HTML5 History 模式
  // 特点：URL 中不带 '#' 号（如 http://localhost/login），更加美观，但需要后端服务器配置重定向支持
  history: createWebHistory(),
  routes,
})

export default router
