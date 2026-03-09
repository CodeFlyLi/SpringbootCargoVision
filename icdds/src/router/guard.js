/**
 * 全局路由守卫 (router/guard.js)
 *
 * 核心功能：权限控制 (Permission Control)
 *
 * 流程逻辑：
 * 1. 拦截每一次路由跳转。
 * 2. 检查用户是否已登录 (是否有 Token)。
 * 3. 检查用户是否已获取详细信息 (角色、权限)。
 * 4. 如果是首次加载，从后端拉取菜单配置，并动态生成路由表。
 * 5. 检查用户是否有权限访问目标页面。
 */

import NProgress from 'nprogress'
import 'nprogress/nprogress.css' // 引入进度条样式
import router from './index'
import { useAuthStore } from '@/stores/auth'
import { usePermissionStore } from '@/stores/permission'

// 白名单路由：无需登录即可直接访问的页面路径
const whiteList = ['/login', '/403']

// 标记位：记录动态路由是否已经添加过
// 防止每次路由跳转都重复请求后端接口
let injected = false

/**
 * 全局前置守卫 (beforeEach)
 *
 * @param to   即将进入的目标路由对象
 * @param from 当前导航正要离开的路由
 * @param next 必须调用的方法，用于 resolve 钩子
 */
router.beforeEach(async (to, from, next) => {
  // 1. 开启顶部加载进度条
  NProgress.start()

  // 获取 Auth Store 实例
  const authStore = useAuthStore()

  // 2. 判断目标路径是否在白名单中
  if (whiteList.includes(to.path)) {
    // 如果是白名单页面，直接放行
    next()
    return
  }

  // 3. 检查登录状态 (判断 Token 是否存在)
  if (!authStore.isLoggedIn) {
    // 未登录状态
    // 如果已经在登录页，则不处理（防止死循环）
    // 但前面已经判断了白名单，所以这里处理的是非白名单的受保护页面

    // 跳转到登录页，并携带 redirect 参数，以便登录后能自动跳回当前尝试访问的页面
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }

  // 4. 已登录状态，检查是否需要初始化用户信息和动态路由
  // 触发条件：store 中没有用户信息，或者动态路由尚未注入 (injected = false)
  // 场景：用户刚登录成功，或者按 F5 刷新了页面
  if (!authStore.user || !injected) {
    try {
      // 4.1 重新获取最新的用户信息（角色、权限点）
      await authStore.getUserInfo()

      // 4.2 初始化权限 Store
      const permissionStore = usePermissionStore()

      // 4.3 从后端获取菜单数据
      await permissionStore.fetchMenus()

      // 4.4 根据菜单数据和组件映射，生成 Vue Router 可识别的路由配置数组
      const accessRoutes = await permissionStore.generateRoutes()

      // 4.5 将生成的动态路由添加到 'Root' 父路由下
      // 这样这些页面就会显示在 MainLayout 的 <router-view> 中
      accessRoutes.forEach((route) => {
        router.addRoute('Root', route)
      })

      // 4.6 标记注入完成
      injected = true

      // 4.7 重新触发跳转
      // 关键点：使用 replace: true 替换当前历史记录
      // 必须重新调用 next({...to})，因为此时路由配置刚刚更新，直接 next() 可能会导致 404
      next({ path: to.path, query: to.query, hash: to.hash, replace: true })
      return
    } catch (e) {
      // 异常处理：通常是 Token 过期或网络错误
      console.error('路由守卫初始化失败:', e)

      // 强制登出清理状态
      await authStore.logout()

      // 跳转回登录页
      next('/login')
      return
    }
  }

  // 5. 细粒度权限校验 (Meta Permission Check)
  // 检查目标路由是否配置了 meta.permission 字段
  if (to.meta.permission) {
    const { permissions } = authStore

    // 判断用户拥有的权限列表中是否包含目标页面所需的权限
    // 支持通配符 '*' 或 '*:*:*' (超级管理员)
    const hasPerm =
      Array.isArray(permissions) &&
      (permissions.includes('*') ||
        permissions.includes('*:*:*') ||
        permissions.includes(to.meta.permission))

    if (!hasPerm) {
      // 记录无权访问的日志（仅开发环境可见）
      try {
        console.warn('访问拦截(权限不足):', {
          path: to.path,
          requiredPermission: to.meta.permission,
          currentPermissions: permissions,
        })
      } catch {}

      // 跳转到 403 禁止访问页面
      next('/403')
      return
    }
  }

  // 6. 设置浏览器标题
  // 格式：页面标题 - 系统名称
  document.title = to.meta?.title
    ? `${to.meta.title} - 智能货物破损检测系统`
    : '智能货物破损检测系统'

  // 7. 所有检查通过，放行
  next()
})

/**
 * 全局后置钩子 (afterEach)
 *
 * 作用：路由跳转完成后执行。
 * 主要用于结束进度条动画。
 */
router.afterEach(() => {
  NProgress.done()
})
