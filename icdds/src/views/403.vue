<template>
  <div class="forbidden-container">
    <div class="content">
      <h1 class="error-code">403</h1>
      <h2 class="error-title">访问受限</h2>
      <p class="error-desc">抱歉，您没有权限访问该页面。</p>
      <!-- 返回首页 - 无需权限 -->
      <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
    </div>
  </div>
</template>

<style scoped>
.forbidden-container {
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
  color: var(--el-color-danger);
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
