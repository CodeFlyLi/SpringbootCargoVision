/**
 * 主题状态管理 (stores/theme.js)
 *
 * 使用 Pinia 来管理应用的全局主题状态。
 * 这允许我们在任何组件中切换主题（如：Vue风格、Bilibili风格、暗黑模式）。
 */

import { ref } from 'vue'
import { defineStore } from 'pinia'

// defineStore 定义一个 Store，第一个参数是唯一的 ID 'theme'
export const useThemeStore = defineStore('theme', () => {
  // 状态：当前主题，优先从 localStorage 获取，默认为 'vue'
  const currentTheme = ref(localStorage.getItem('theme') || 'vue')

  // 动作：切换主题
  const setTheme = (theme) => {
    currentTheme.value = theme
    // 保存到 localStorage
    localStorage.setItem('theme', theme)
    
    // 设置 HTML 根元素的 data-theme 属性，CSS 会根据这个属性应用不同的变量
    document.documentElement.setAttribute('data-theme', theme)

    // 特殊处理：如果是暗黑模式，还需要配合 Element Plus 的暗黑类名
    if (theme === 'dark') {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
  }

  // 初始化：应用当前主题
  setTheme(currentTheme.value)

  // 返回状态和方法，供组件使用
  return { currentTheme, setTheme }
})
