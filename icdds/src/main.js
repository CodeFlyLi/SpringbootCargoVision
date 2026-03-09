/**
 * 项目入口文件 (main.js)
 *
 * 【执行逻辑与作用】：
 * 这里的代码是整个 Vue 应用的启动点。
 * 1. 导入 Vue 核心库和必要的插件（Pinia, Router, ElementPlus）。
 * 2. 导入根组件 App.vue，它是所有页面的容器。
 * 3. 创建 Vue 应用实例。
 * 4. 注册全局插件：
 *    - Pinia: 状态管理（用户登录状态、主题等）。
 *    - Router: 路由管理（页面跳转）。
 *    - ElementPlus: UI 组件库。
 * 5. 挂载应用到 index.html 的 #app 节点，开始渲染页面。
 *
 * 【执行顺序】：
 * import -> createApp -> app.use(plugins) -> app.mount
 */

import { createApp } from 'vue' // 导入 Vue 的创建应用函数
import { createPinia } from 'pinia' // 导入 Pinia 状态管理库
import ElementPlus from 'element-plus' // 导入 Element Plus UI 组件库
import 'element-plus/dist/index.css' // 导入 Element Plus 的基础样式
import 'element-plus/theme-chalk/dark/css-vars.css' // 导入暗黑模式的变量定义
import '@/assets/styles/theme.css' // 导入我们需要自定义的全局主题样式
import '@/assets/styles/mobile-card.css' // 导入移动端卡片通用样式
import '@/assets/styles/management-layout.css' // 导入PC端管理页面通用布局样式

import App from './App.vue' // 导入根组件 App.vue
import router from './router/index' // 导入配置好的路由实例
import './router/guard' // 导入路由守卫 (权限控制核心逻辑)
import { useAuthStore } from './stores/auth' // 导入认证状态管理 store

// 1. 创建 Vue 应用实例
// 这一步初始化了 Vue 的核心对象
import * as ElementPlusIconsVue from '@element-plus/icons-vue' // 导入所有图标

const app = createApp(App)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 2. 安装插件
// 按顺序注册全局功能：
// - createPinia(): 初始化全局状态管理，让组件可以共享数据
app.use(createPinia())
// - router: 初始化路由系统，监听 URL 变化并渲染对应组件
app.use(router)
// - ElementPlus: 注册 UI 组件库，使得全局可以使用 <el-button> 等组件
app.use(ElementPlus)

// 3. 挂载应用
// 将构建好的 Vue 应用实例挂载到 DOM 树中 ID 为 app 的节点上
// 这标志着 Vue 应用正式接管页面渲染
app.mount('#app')
