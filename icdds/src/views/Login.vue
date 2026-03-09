<!-- 
  Login.vue 登录页面
  功能：用户输入用户名和密码进行身份验证。
-->
<template>
  <div class="login-container">
    <div class="login-box">
      <div class="logo">
        <h1 class="app-title">CargoVision</h1>
        <p class="app-subtitle">货物运输破损检测系统</p>
      </div>
      <el-card>
        <!-- 登录表单 -->
        <el-form
          :model="loginForm"
          :rules="rules"
          ref="loginFormRef"
          :label-width="isMobile ? '0' : '80px'"
          :label-position="isMobile ? 'top' : 'right'"
        >
          <el-form-item :label="isMobile ? '' : '用户名'" prop="username">
            <el-input
              v-model.trim="loginForm.username"
              placeholder="请输入用户名"
              :prefix-icon="User"
              clearable
            />
          </el-form-item>
          <el-form-item :label="isMobile ? '' : '密码'" prop="password">
            <el-input
              v-model="loginForm.password"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              show-password
              clearable
            />
          </el-form-item>
          <el-form-item>
            <!-- 登录按钮 - 无需权限 -->
            <el-button
              type="primary"
              :icon="Right"
              @click="handleLogin"
              class="login-button"
              :loading="loading"
              >登录</el-button
            >
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Lock, Right } from '@element-plus/icons-vue'
import router from '@/router/index'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

// 响应式布局
const isMobile = ref(window.innerWidth < 768)
const handleResize = () => {
  isMobile.value = window.innerWidth < 768
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})

// 获取表单实例引用，用于调用 validate 方法
const loginFormRef = ref(null)
const loading = ref(false)

// 响应式数据：登录表单数据
const loginForm = ref({
  username: 'admin',
  password: '123456',
})

// 表单验证规则
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

// 处理登录点击事件
const handleLogin = () => {
  // 调用 Element Plus 表单的验证方法
  loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await authStore.login(loginForm.value)
        ElMessage.success('登录成功')
        router.push('/')
      } catch (error) {
        console.error('登录失败:', error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: var(--bg-color);
}

.login-box {
  width: 100%;
  max-width: 400px;
  padding: 20px;
}

.logo {
  margin-bottom: 32px;
  text-align: center;
}

.app-title {
  font-size: 30px;
  font-weight: 700;
  color: var(--el-color-primary);
  margin: 0;
}

.app-subtitle {
  color: var(--el-text-color-secondary);
  margin-top: 8px;
  margin-bottom: 0;
}

.login-button {
  width: 100%;
}
</style>
