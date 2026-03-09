<template>
  <div class="not-found-container">
    <div class="content">
      <h1 class="error-code">404</h1>
      <h2 class="error-title">页面未找到</h2>
      <p class="error-desc">抱歉，您访问的页面不存在或已被移动。</p>
      <!-- 返回首页 - 无需权限 -->
      <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
    </div>
  </div>
</template>

<style scoped>
.not-found-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: var(--bg-color);
}

.content {
  text-align: center;
}

.error-code {
  font-size: 120px;
  font-weight: bold;
  color: var(--el-color-primary);
  margin: 0;
  line-height: 1.2;
}

.error-title {
  font-size: 30px;
  color: var(--text-color-primary);
  margin: 20px 0;
}

.error-desc {
  font-size: 16px;
  color: var(--text-color-regular);
  margin-bottom: 30px;
}
</style>
