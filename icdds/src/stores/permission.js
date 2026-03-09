/**
 * 权限与动态路由管理 Store (stores/permission.js)
 * 
 * 核心功能：
 * 1. 从后端获取用户可访问的菜单列表。
 * 2. 将后端菜单数据转换为 Vue Router 可识别的路由配置。
 * 3. 实现了基于角色的动态路由控制 (RBAC)。
 */

import { defineStore } from 'pinia'
import { getRouters } from '@/api/auth'
import MainLayout from '@/components/MainLayout.vue'

// 使用 Vite 的 glob 导入功能，批量获取 src/views 下所有的 .vue 文件
// 作用：将后端返回的字符串路径（如 "system/user/index"）映射到实际的组件文件
const modules = import.meta.glob('/src/views/**/*.vue')

export const usePermissionStore = defineStore('permission', {
  state: () => ({
    // 存储格式化后的菜单列表，用于侧边栏渲染
    menus: [],
    // 权限映射表 (code -> menu object)，用于快速查找
    permissionMap: new Map(),
  }),

  actions: {
    /**
     * Action 1: 获取并处理菜单数据
     * 
     * 流程：
     * 1. 调用后端接口 getRouters() 获取当前用户的菜单树。
     * 2. 对原始数据进行格式化（适配前端字段名）。
     * 3. 存储到 state.menus 中。
     */
    async fetchMenus() {
      try {
        const res = await getRouters()
        
        // 兼容处理：后端可能直接返回数组，也可能包裹在 data 字段中
        const menus = Array.isArray(res) ? res : res.data || []

        // 递归格式化菜单函数
        const formatMenu = (menuList) => {
          return menuList.map((item) => {
            return {
              path: item.path,           // 路由路径
              name: item.name,           // 路由名称 (唯一)
              component: item.component, // 组件路径字符串 (后续需解析)
              meta: {
                title: item.name,        // 菜单标题
                icon: item.icon,         // 菜单图标
                permission: item.permission // 权限标识
              },
              // 递归处理子菜单
              children: item.children ? formatMenu(item.children) : [],
            }
          })
        }

        const formattedMenus = formatMenu(menus)
        this.menus = formattedMenus
        return formattedMenus
      } catch (error) {
        console.error('获取菜单失败:', error)
        return []
      }
    },

    /**
     * Action 2: 生成动态路由配置
     * 
     * 作用：
     * 将 menus 树形结构扁平化，并解析 component 字段为实际的 Vue 组件对象，
     * 生成可供 router.addRoute 使用的路由配置数组。
     */
    async generateRoutes() {
      const routes = []

      // 递归遍历菜单树，提取所有叶子节点（页面）
      const flattenRoutes = (items) => {
        items.forEach((item) => {
          // 如果有子节点，先递归处理子节点
          if (item.children?.length) {
            flattenRoutes(item.children)
          }

          // 只有配置了 component 且有 path 的节点才生成路由
          // (目录节点通常没有 component，或者 component 为 Layout)
          if (item.component && item.path) {
            
            // 解析组件文件
            const component = this.resolveComponent(item.component)
            
            if (!component) {
              console.warn(`未找到组件文件: ${item.component}`)
              return
            }

            // 构建路由对象
            routes.push({
              path: item.path, // 完整路径
              name: item.path, // 使用路径作为 name，确保唯一性
              component: component,
              meta: {
                title: item.meta?.title || item.name,
                permission: item.meta?.permission,
                icon: item.meta?.icon,
              },
            })
          }
        })
      }

      flattenRoutes(this.menus)
      return routes
    },

    /**
     * 辅助方法：解析组件路径
     * 
     * @param {string} compStr 后端返回的组件路径字符串
     * @returns {Component} Vue 组件对象
     */
    resolveComponent(compStr) {
      if (!compStr) return null

      // 情况 1: Layout 组件 (通常是顶级菜单或包含子菜单的目录)
      if (compStr === 'MainLayout' || compStr === 'Layout') {
        return MainLayout
      }

      // 情况 2: 业务组件
      // 目标路径格式: /src/views/xxx/xxx.vue
      let path = compStr

      // 自动补全 views/ 前缀
      if (!path.startsWith('views/')) {
        path = `views/${path}`
      }

      // 自动补全 .vue 后缀
      if (!path.endsWith('.vue')) {
        path = `${path}.vue`
      }

      const fullPath = `/src/${path}`
      
      // 从 modules 集合中查找对应的组件
      return modules[fullPath]
    },

    /**
     * 辅助方法：重置 Store 状态 (通常在登出时调用)
     */
    reset() {
      this.menus = []
      this.permissionMap.clear()
    },
  },
})
