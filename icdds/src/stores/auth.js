/**
 * 用户认证状态管理 (stores/auth.js)
 *
 * 作用：
 * 1. 使用 Pinia 管理全局的用户认证状态。
 * 2. 存储核心数据：Token、用户信息、角色列表、权限列表。
 * 3. 提供登录、登出、获取用户信息等业务 Action。
 * 4. 实现数据的持久化（localStorage），防止刷新丢失。
 */

import { defineStore } from 'pinia'
import { loginApi, getUserInfoApi, logoutApi } from '@/api/auth'

// Token 在 localStorage 中的存储键名
const TOKEN_KEY = 'Authorization'

export const useAuthStore = defineStore('auth', {
  // 定义 State (状态)
  state: () => ({
    // Token: 优先从本地缓存读取，实现自动登录
    token: localStorage.getItem(TOKEN_KEY) || '',
    
    // 用户基本信息对象 (id, username, avatar 等)
    user: JSON.parse(localStorage.getItem('user_info') || 'null'),
    
    // 用户角色列表 (如 ['admin', 'operator'])
    roles: JSON.parse(localStorage.getItem('user_roles') || '[]'),
    
    // 用户权限标识列表 (如 ['system:user:list', 'cargo:detect:add'])
    permissions: JSON.parse(localStorage.getItem('user_permissions') || '[]'),
  }),

  // 定义 Getters (计算属性)
  getters: {
    // 判断用户是否已登录
    // 逻辑：只要 Token 字符串非空，暂且视为已登录（具体有效性由后端接口验证）
    isLoggedIn: (state) => !!state.token,
  },

  // 定义 Actions (业务方法)
  actions: {
    /**
     * 用户登录 Action
     * 
     * @param {Object} loginForm 登录表单数据 { username, password }
     * @return {Promise} 登录结果
     */
    async login(loginForm) {
      try {
        // 1. 调用后端登录接口
        const res = await loginApi(loginForm)
        
        // 2. 获取 Token (假设后端返回结构包含 token 字段)
        const token = res.token
        
        // 3. 保存 Token 到状态和本地存储
        this.setToken(token)
        
        // 4. 立即获取用户信息（确保存储了最新的权限数据）
        await this.getUserInfo()
        
        return res
      } catch (error) {
        return Promise.reject(error)
      }
    },

    /**
     * 获取用户信息 Action
     * 
     * 作用：拉取当前用户的详细资料、角色和权限列表。
     * 通常在登录成功后，或页面刷新（Token存在但User为空）时调用。
     */
    async getUserInfo() {
      try {
        const res = await getUserInfoApi()
        
        // 解析后端返回的数据结构
        // 兼容性处理：后端可能返回 user 对象，也可能直接在根对象返回属性
        const user = res.user || res.userInfo || (res.username || res.id ? res : {})
        const roles = res.roles || []
        const permissions = Array.isArray(res.permissions) ? res.permissions : []

        // 更新状态并持久化
        this.setUserInfo(user, roles, permissions)

        return { roles: this.roles, permissions: this.permissions }
      } catch (error) {
        return Promise.reject(error)
      }
    },

    /**
     * 退出登录 Action
     * 
     * 作用：清理所有登录状态，安全退出系统。
     */
    async logout() {
      // 1. 尝试通知后端注销 (可选，取决于后端是否需要使 Token 失效)
      try {
        await logoutApi().catch(() => {})
      } finally {
        // 2. 无论后端接口成功与否，前端必须执行清理操作
        this.resetToken()
        
        // 3. 强制刷新并跳转到登录页
        // 使用 location.href 而不是 router.push，是为了彻底清除内存中的路由实例和 Store 状态
        // 防止上一个用户的动态路由残留在内存中
        window.location.href = '/login'
      }
    },

    /**
     * 辅助方法：设置并保存 Token
     */
    setToken(token) {
      this.token = token
      localStorage.setItem(TOKEN_KEY, token)
    },

    /**
     * 辅助方法：设置并保存用户信息
     */
    setUserInfo(user, roles, permissions) {
      this.user = user || {}
      this.roles = roles || []
      this.permissions = permissions || []

      // 备份到 localStorage，用于某些非 Vue 组件场景的读取
      localStorage.setItem('user_info', JSON.stringify(this.user))
      localStorage.setItem('user_roles', JSON.stringify(this.roles))
      localStorage.setItem('user_permissions', JSON.stringify(this.permissions))
    },

    /**
     * 辅助方法：重置所有状态 (登出时调用)
     */
    resetToken() {
      this.token = ''
      this.user = null
      this.roles = []
      this.permissions = []
      
      // 清除 localStorage 中的所有相关项
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem('user_info')
      localStorage.removeItem('user_roles')
      localStorage.removeItem('user_permissions')
    },
  },
})
