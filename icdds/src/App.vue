<!-- 
  App.vue 是项目的根组件
  
  【执行逻辑与作用】：
  1. 它是整个 Vue 应用的顶层容器。
  2. 它负责初始化全局状态（如主题配置）。
  3. 它包含 <RouterView />，这是路由渲染的出口。
  
  【执行顺序】：
  应用挂载(main.js) -> App.vue setup() -> 初始化 themeStore -> 渲染 Template -> RouterView 显示对应路由组件
-->
<script setup>
import { useThemeStore } from '@/stores/theme'

// 初始化主题 Store
// 【作用】：
// 即使这里没有直接使用 themeStore 的数据，调用它也能确保 store 被初始化。
// 在 theme.js 的 defineStore 内部逻辑中，通常会读取 localStorage 中的主题配置并应用到 body 上。
// 这样可以保证用户刷新页面后，主题设置（如暗黑模式）依然生效。
const themeStore = useThemeStore()
</script>

<template>
  <!-- 路由视图占位符：Vue Router 会根据当前 URL，动态把对应的页面组件显示在这里 -->
  <!-- 例如：URL 是 /login 时，这里显示 Login.vue；URL 是 /dashboard 时，这里显示 Dashboard.vue -->
  <RouterView />
</template>

<style scoped></style>
