<template>
  <div class="app-container">
    <div class="user-profile-container">
      <!-- 个人信息卡片 -->
      <div class="profile-sidebar">
        <el-card class="box-card">
          <template #header>
            <div class="clearfix">
              <span>个人信息</span>
            </div>
          </template>
          <div class="profile-header text-center">
            <el-avatar :size="100" :src="userInfo.avatar || defaultAvatar" class="mb-4" />
            <div class="font-bold text-xl mb-2">{{ userInfo.name || userInfo.username }}</div>
            <div class="role-tags">
              <el-tag
                v-for="(role, index) in userRoleList"
                :key="index"
                effect="plain"
                class="mx-1"
              >
                {{ role }}
              </el-tag>
            </div>
          </div>
          <div class="profile-detail mt-6">
            <div class="detail-item">
              <el-icon><User /></el-icon>
              <span class="ml-2">用户名: {{ userInfo.username }}</span>
            </div>
            <div class="detail-item">
              <el-icon><Phone /></el-icon>
              <span class="ml-2">手机: {{ userInfo.phone || '未绑定' }}</span>
            </div>
            <div class="detail-item">
              <el-icon><Message /></el-icon>
              <span class="ml-2">邮箱: {{ userInfo.email || '未绑定' }}</span>
            </div>
            <div class="detail-item">
              <el-icon><Calendar /></el-icon>
              <span class="ml-2">注册时间: {{ userInfo.createdAt || '-' }}</span>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 基本资料与修改密码 -->
      <div class="profile-content">
        <el-card>
          <template #header>
            <div class="clearfix">
              <span>基本资料</span>
            </div>
          </template>
          <el-tabs v-model="activeTab">
            <el-tab-pane label="基本资料" name="info" height="400px">
              <el-form
                ref="userFormRef"
                :model="userForm"
                :rules="userRules"
                label-width="80px"
                height="400px"
              >
                <el-form-item label="用户名" prop="username">
                  <el-input v-model="userForm.username" maxlength="30" />
                </el-form-item>
                <el-form-item label="姓名" prop="name">
                  <el-input v-model="userForm.name" maxlength="30" />
                </el-form-item>

                <el-form-item label="手机号码" prop="phone">
                  <el-input v-model="userForm.phone" maxlength="11" />
                </el-form-item>

                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="userForm.email" maxlength="50" />
                </el-form-item>
                <el-form-item label="性别">
                  <el-radio-group v-model="userForm.gender">
                    <el-radio :label="1">男</el-radio>
                    <el-radio :label="2">女</el-radio>
                  </el-radio-group>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="loading" @click="submitUserInfo"
                    >保存</el-button
                  >
                  <el-button type="danger" @click="resetForm">重置</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <el-tab-pane label="修改密码" name="password">
              <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="80px">
                <el-form-item label="旧密码" prop="oldPassword">
                  <el-input
                    v-model="pwdForm.oldPassword"
                    type="password"
                    show-password
                    placeholder="请输入旧密码"
                  />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input
                    v-model="pwdForm.newPassword"
                    type="password"
                    show-password
                    placeholder="请输入新密码"
                  />
                </el-form-item>
                <el-form-item label="确认密码" prop="confirmPassword">
                  <el-input
                    v-model="pwdForm.confirmPassword"
                    type="password"
                    show-password
                    placeholder="请确认新密码"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="loading" @click="submitPassword"
                    >保存</el-button
                  >
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { updateUser } from '@/api/user' // Note: verify if resetPassword supports checking old password or if there is a specific endpoint
import { ElMessage } from 'element-plus'
import { Calendar, Message, Phone, User } from '@element-plus/icons-vue'

const authStore = useAuthStore()
const userInfo = computed(() => authStore.user || {})
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
const activeTab = ref('info')
const loading = ref(false)

const userRoleList = computed(() => {
  const roles = authStore.roles || []
  if (roles.length === 0) return ['普通用户']

  // 如果角色是对象，提取名称；如果是字符串，直接使用
  return roles.map((role) => {
    if (typeof role === 'string') return role
    return role.name || role.roleName || role.roleCode || '未知角色'
  })
})

// --- 基本资料 ---
const userFormRef = ref(null)
const userForm = reactive({
  username: '',
  name: '',
  phone: '',
  email: '',
  gender: 1,
})

const userRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] },
  ],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' },
  ],
}

// 初始化表单
const initForm = () => {
  if (userInfo.value) {
    userForm.username = userInfo.value.username || ''
    userForm.name = userInfo.value.name || ''
    userForm.phone = userInfo.value.phone || ''
    userForm.email = userInfo.value.email || ''
    userForm.gender = userInfo.value.gender || 1
  }
}

const resetForm = () => {
  initForm()
}

const submitUserInfo = async () => {
  if (!userFormRef.value) return

  await userFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await updateUser(userInfo.value.id, userForm)
        ElMessage.success('修改成功')
        // 更新 store 中的用户信息
        await authStore.getUserInfo()
      } catch (error) {
        console.error('修改失败', error)
      } finally {
        loading.value = false
      }
    }
  })
}

// --- 修改密码 ---
const pwdFormRef = ref(null)
const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const validatePass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else {
    if (pwdForm.confirmPassword !== '') {
      if (!pwdFormRef.value) return
      pwdFormRef.value.validateField('confirmPassword')
    }
    callback()
  }
}

const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' },
    { validator: validatePass, trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validatePass2, trigger: 'blur' },
  ],
}

const submitPassword = async () => {
  if (!pwdFormRef.value) return

  await pwdFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 注意：这里需要确认后端 API 是否支持修改密码，或者是否有专门的接口
        // 假设 updateUser 支持传 password 字段，或者有一个 updatePassword 接口
        // 这里暂时调用 updateUser 示例，实际上可能需要专门的 changePassword API
        // 如果后端 resetPassword 接口不需要旧密码，那可能是管理员重置接口
        // 如果没有找到 changePassword 接口，可能需要新增或确认

        // 尝试查找是否有 changePassword 接口，如果没有，暂时模拟或提示
        ElMessage.warning('密码修改功能暂未对接后端接口')
      } catch (error) {
        console.error('修改密码失败', error)
      } finally {
        loading.value = false
      }
    }
  })
}

onMounted(() => {
  initForm()
})
</script>

<style scoped>
.profile-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--el-border-color-light);
}

.profile-detail {
  margin-top: 20px;
}

.detail-item {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  color: var(--el-text-color-regular);
  font-size: 14px;
}

.text-center {
  text-align: center;
}

.mb-4 {
  margin-bottom: 1rem;
}

.mb-2 {
  margin-bottom: 0.5rem;
}

.mt-6 {
  margin-top: 1.5rem;
}

.ml-2 {
  margin-left: 0.5rem;
}

.user-profile-container {
  display: flex;
  gap: 20px;
}

.profile-sidebar {
  width: 300px;
  flex-shrink: 0;
}

.profile-content {
  flex: 1;
  min-width: 0; /* 防止内容溢出 */
  max-width: 800px;
}

@media screen and (max-width: 768px) {
  .user-profile-container {
    flex-direction: column;
  }

  .profile-sidebar {
    width: 100%;
  }
}
</style>
