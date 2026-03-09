<!--
  MainLayout.vue
  这是应用的主布局组件，采用经典的 "侧边栏 + 顶部导航 + 内容区" 结构。
  所有需要显示侧边栏的页面（如仪表盘、用户管理等）都作为子路由嵌套在这里。
-->
<template>
  <div class="app-container">
    <el-container class="main-container">
      <!-- 侧边栏区域 (PC端) -->
      <el-aside v-if="!isMobile" :width="isCollapse ? '64px' : '200px'" class="aside-menu">
        <div class="logo">
          <span class="logo-text" v-if="!isCollapse">CargoVision</span>
          <span class="logo-text" v-else>CV</span>
        </div>
        <el-menu
          :default-active="activeMenu"
          class="el-menu-vertical-demo"
          :collapse="isCollapse"
          :unique-opened="false"
          :collapse-transition="false"
          text-color="var(--menu-text-color)"
          active-text-color="var(--menu-active-text)"
          background-color="var(--aside-bg-color)"
          :router="true"
        >
          <template v-for="menu in menuList" :key="menu.id">
            <el-sub-menu v-if="menu.children && menu.children.length > 0" :index="menu.path">
              <template #title>
                <el-icon>
                  <component :is="getIcon(menu)" />
                </el-icon>
                <span>{{ menu.name }}</span>
              </template>
              <el-menu-item v-for="child in menu.children" :key="child.id" :index="child.path">
                <template #title>
                  <span>{{ child.name }}</span>
                </template>
              </el-menu-item>
            </el-sub-menu>
            <el-menu-item v-else :index="menu.path">
              <el-icon>
                <component :is="getIcon(menu)" />
              </el-icon>
              <span>{{ menu.name }}</span>
            </el-menu-item>
          </template>
        </el-menu>
      </el-aside>

      <!-- 侧边栏区域 (移动端抽屉) -->
      <el-drawer
        v-if="isMobile"
        v-model="isMobileMenuOpen"
        direction="ltr"
        size="200px"
        :with-header="false"
        class="mobile-aside-drawer"
      >
        <div class="logo">
          <span class="logo-text">CargoVision</span>
        </div>
        <el-menu
          :default-active="activeMenu"
          class="el-menu-vertical-demo mobile-menu"
          :unique-opened="false"
          text-color="var(--menu-text-color)"
          active-text-color="var(--menu-active-text)"
          background-color="var(--aside-bg-color)"
          :router="true"
          @select="isMobileMenuOpen = false"
        >
          <template v-for="menu in menuList" :key="menu.id">
            <el-sub-menu v-if="menu.children && menu.children.length > 0" :index="menu.path">
              <template #title>
                <el-icon>
                  <component :is="getIcon(menu)" />
                </el-icon>
                <span>{{ menu.name }}</span>
              </template>
              <el-menu-item v-for="child in menu.children" :key="child.id" :index="child.path">
                <template #title>
                  <span>{{ child.name }}</span>
                </template>
              </el-menu-item>
            </el-sub-menu>
            <el-menu-item v-else :index="menu.path">
              <el-icon>
                <component :is="getIcon(menu)" />
              </el-icon>
              <span>{{ menu.name }}</span>
            </el-menu-item>
          </template>
        </el-menu>
      </el-drawer>

      <!-- 右侧主体区域 -->
      <el-container>
        <!-- 顶部导航栏 -->
        <el-header class="header">
          <div class="header-left">
            <el-icon class="icon-margin-right toggle-icon" @click="toggleCollapse">
              <component :is="isMobile ? 'Expand' : isCollapse ? 'Expand' : 'Fold'" />
            </el-icon>
            <span class="page-title">{{ currentPage }}</span>
          </div>
          <div class="header-right">
            <!-- 主题切换下拉菜单 - 无需权限 -->
            <el-dropdown @command="handleThemeCommand" class="theme-dropdown">
              <span class="theme-switch">
                <el-icon class="icon-margin-right">
                  <MagicStick />
                </el-icon>
                <span>主题</span>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="vue">Vue 风格</el-dropdown-item>
                  <el-dropdown-item command="bilibili">Bilibili 风格</el-dropdown-item>
                  <el-dropdown-item command="dark">夜间模式</el-dropdown-item>
                  <el-dropdown-item command="purple">韵味紫</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <!-- 用户信息及下拉菜单 - 无需权限 -->
            <el-dropdown>
              <span class="user-info">
                <el-avatar class="icon-margin-right" @click.stop="handleAvatarClick">
                  <img :src="avatarUrl" alt="User" />
                </el-avatar>

                <span class="username">{{ authStore.user?.username }}</span>
                <el-icon class="icon-margin-left">
                  <ArrowDown />
                </el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <!-- 更改头像 - 需要修改头像权限 -->
                  <el-dropdown-item
                    v-if="canUpdateAvatar"
                    @click="handleAvatarClick"
                    :icon="Camera"
                  >
                    更改头像
                  </el-dropdown-item>
                  <!-- 个人资料 - 需要查询资料权限 -->
                  <el-dropdown-item v-if="canViewProfile" :icon="User"> 个人资料 </el-dropdown-item>
                  <!-- 设置 - 需要设置权限 -->
                  <el-dropdown-item v-if="canSetting" :icon="Setting"> 设置 </el-dropdown-item>
                  <!-- 退出登录 - 无需权限 -->
                  <el-dropdown-item @click="handleLogout" :icon="SwitchButton">
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        <!-- 内容显示区域：子路由的组件会渲染在这里 -->
        <el-main class="main-content">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
    <!-- 更改头像抽屉 -->
    <el-drawer
      v-model="avatarDialogVisible"
      title="更改头像"
      :size="isMobile ? '100%' : '600px'"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <!-- 模式1：普通选择和上传 -->
      <div v-if="!showCropper" class="avatar-selection">
        <div class="section-title">选择预设头像</div>
        <div class="preset-avatars">
          <div
            v-for="(url, index) in presetAvatars"
            :key="index"
            class="avatar-item"
            :class="{ active: tempAvatarUrl === url }"
            @click="selectAvatar(url)"
          >
            <el-avatar :size="60" :src="url" />
            <div class="check-mark" v-if="tempAvatarUrl === url">
              <el-icon>
                <check />
              </el-icon>
            </div>
          </div>
        </div>
        <div class="section-title mt-20">上传自定义头像</div>
        <el-upload
          class="avatar-uploader"
          action="#"
          :show-file-list="false"
          :auto-upload="false"
          :on-change="handleFileChange"
        >
          <img
            v-if="tempAvatarUrl && !presetAvatars.includes(tempAvatarUrl)"
            :src="tempAvatarUrl"
            class="avatar"
          />
          <el-icon v-else class="avatar-uploader-icon">
            <Plus />
          </el-icon>
        </el-upload>
      </div>
      <!-- 模式2：图片裁剪器 (VueCropper) -->
      <div v-else class="avatar-cropper-container">
        <div class="cropper-wrapper">
          <vue-cropper
            ref="cropperRef"
            :img="cropperOptions.img"
            :output-size="cropperOptions.size"
            :output-type="cropperOptions.outputType"
            :info="true"
            :full="cropperOptions.full"
            :can-move="cropperOptions.canMove"
            :can-move-box="cropperOptions.canMoveBox"
            :fixed-box="cropperOptions.fixedBox"
            :original="cropperOptions.original"
            :auto-crop="cropperOptions.autoCrop"
            :auto-crop-width="cropperOptions.autoCropWidth"
            :auto-crop-height="cropperOptions.autoCropHeight"
            :center-box="cropperOptions.centerBox"
            :high="cropperOptions.high"
            :max-img-size="cropperOptions.maxImgSize"
          />
        </div>
        <div class="cropper-toolbar">
          <el-button-group>
            <!-- 向左旋转 - 纯前端操作 -->
            <el-button type="primary" :icon="RefreshLeft" @click="rotateLeft" />
            <!-- 向右旋转 - 纯前端操作 -->
            <el-button type="primary" :icon="RefreshRight" @click="rotateRight" />
            <!-- 放大 - 纯前端操作 -->
            <el-button type="primary" :icon="ZoomIn" @click="changeScale(1)" />
            <!-- 缩小 - 纯前端操作 -->
            <el-button type="primary" :icon="ZoomOut" @click="changeScale(-1)" />
          </el-button-group>
          <div class="action-btns">
            <!-- 取消裁剪 - 无需权限 -->
            <el-button @click="cancelCrop">取消</el-button>
            <!-- 确认裁剪 - 需要修改头像权限 -->
            <el-button v-if="canUpdateAvatar" type="success" @click="finishCrop"
              >确认裁剪</el-button
            >
          </div>
        </div>
      </div>
      <template #footer v-if="!showCropper">
        <div class="drawer-footer">
          <!-- 取消 - 无需权限 -->
          <el-button @click="avatarDialogVisible = false">取消</el-button>
          <!-- 确定 - 需要修改头像权限 -->
          <el-button v-if="canUpdateAvatar" type="primary" @click="confirmAvatar">确定</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, reactive, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useThemeStore } from '@/stores/theme'
import { useAuthStore } from '@/stores/auth'
import { usePermissionStore } from '@/stores/permission'
import http from '@/utils/request'
import { updateUser } from '@/api/user'
import { storeToRefs } from 'pinia'
import { hasPermission } from '@/utils/permission'
import { PERMISSIONS } from '@/constants/permissionConstants'
// 导入图标
import {
  User,
  ArrowDown,
  SwitchButton,
  Setting,
  MagicStick,
  Check,
  Plus,
  ZoomIn,
  ZoomOut,
  RefreshLeft,
  RefreshRight,
  Camera,
  Expand,
  Fold,
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import 'vue-cropper/dist/index.css'
import { VueCropper } from 'vue-cropper'

// --- 头像管理逻辑 ---
const router = useRouter()
//  当前路由的参数，包含在URL中
const route = useRoute()
//这是当前路由的参数，包含在URL中
const themeStore = useThemeStore()
// 从 authStore 获取用户信息
const authStore = useAuthStore()
const permissionStore = usePermissionStore()
const { permissions: perms } = storeToRefs(authStore)

const canUpdateAvatar = computed(() => hasPermission(perms.value, PERMISSIONS.USER_AVATAR_UPDATE))
const canViewProfile = computed(() => hasPermission(perms.value, PERMISSIONS.USER_PROFILE_QUERY))
const canSetting = computed(() => hasPermission(perms.value, PERMISSIONS.USER_SETTING))

// 侧边栏折叠状态
const isCollapse = ref(false)
const isMobile = ref(window.innerWidth < 768)
const isMobileMenuOpen = ref(false)

const toggleCollapse = () => {
  if (isMobile.value) {
    isMobileMenuOpen.value = !isMobileMenuOpen.value
  } else {
    isCollapse.value = !isCollapse.value
  }
}

// 监听窗口大小变化，自动折叠侧边栏
const handleResize = () => {
  isMobile.value = window.innerWidth < 768
  if (isMobile.value) {
    isCollapse.value = false // 移动端不需要折叠状态，直接隐藏
  }
}

onMounted(() => {
  permissionStore.fetchMenus()
  window.addEventListener('resize', handleResize)
})

// 获取图标
const getIcon = (menu) => {
  // 1. 优先使用后端返回的图标 (存储在 meta.icon 中)
  if (menu.meta && menu.meta.icon) {
    return menu.meta.icon
  }

  // 2. 兼容旧逻辑：尝试匹配 icon 字段 (如果未在 meta 中)
  if (menu.icon) {
    return menu.icon
  }

  // 3. 默认图标
  return 'Menu'
}

const menuList = computed(() => permissionStore.menus)

const avatarUrl = ref(authStore.user?.avatar || 'https://randomuser.me/api/portraits/men/1.jpg')
const avatarDialogVisible = ref(false)
const tempAvatarUrl = ref('')

// 监听用户信息变化，更新头像
watch(
  () => authStore.user?.avatar,
  (newVal) => {
    if (newVal) {
      avatarUrl.value = newVal
    }
  },
)

// --- 裁剪器状态配置 ---
const showCropper = ref(false)
const cropperRef = ref(null)
const cropperOptions = reactive({
  img: '', // 裁剪图片的地址
  size: 1, // 裁剪生成图片的质量
  full: false, // 是否输出原图比例的截图
  outputType: 'png', // 裁剪生成图片的格式
  canMove: true, // 图片是否可以移动
  fixedBox: true, // 固定截图框大小
  original: false, // 上传图片按照原始比例渲染
  canMoveBox: true, // 截图框能否拖动
  autoCrop: true, // 是否默认生成截图框
  autoCropWidth: 200,
  autoCropHeight: 200,
  centerBox: true, // 截图框是否被限制在图片里面
  high: true, // 是否根据dpr生成适合屏幕的高清图片
  maxImgSize: 2000, // 限制图片最大宽度和高度
})

// 预设头像列表
const presetAvatars = [
  'https://randomuser.me/api/portraits/men/1.jpg',
  'https://randomuser.me/api/portraits/women/1.jpg',
]

// 打开头像选择对话框
const handleAvatarClick = () => {
  tempAvatarUrl.value = avatarUrl.value
  avatarDialogVisible.value = true
  showCropper.value = false
}

// 选择预设头像
const selectAvatar = (url) => {
  tempAvatarUrl.value = url
}

// 确认更改头像
const confirmAvatar = async () => {
  let newAvatarUrl = tempAvatarUrl.value
  if (!newAvatarUrl) return

  // 如果是 base64 (来自裁剪)，先上传
  if (newAvatarUrl.startsWith('data:image')) {
    try {
      // Base64 转 File
      const arr = newAvatarUrl.split(',')
      const mime = arr[0].match(/:(.*?);/)[1]
      const bstr = atob(arr[1])
      let n = bstr.length
      const u8arr = new Uint8Array(n)
      while (n--) {
        u8arr[n] = bstr.charCodeAt(n)
      }
      const file = new File([u8arr], 'avatar.png', { type: mime })

      // 上传
      const res = await http.upload('/common/upload', file)
      newAvatarUrl = res.url || res
    } catch (e) {
      console.error('上传头像失败', e)
      ElMessage.error('头像上传失败')
      return
    }
  }

  // 更新用户信息
  if (authStore.user && authStore.user.id) {
    try {
      // 构造更新数据，必须包含 username 等必填字段，因为后端校验了
      const updateData = {
        ...authStore.user,
        avatar: newAvatarUrl,
      }
      await updateUser(authStore.user.id, updateData)
      await authStore.getUserInfo() // 刷新本地存储的用户信息
      ElMessage.success('头像修改成功')
      avatarDialogVisible.value = false
    } catch (e) {
      console.error('保存用户信息失败', e)
      ElMessage.error('保存用户信息失败')
    }
  } else {
    ElMessage.error('未找到用户信息')
  }
}

// 处理文件选择（用于上传自定义头像）
const handleFileChange = (uploadFile) => {
  const file = uploadFile.raw
  if (!file) return
  // 验证格式和大小
  const isJPGOrPNG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 14
  // 仅允许 JPG/PNG 格式
  if (!isJPGOrPNG) {
    ElMessage.error('头像仅支持 JPG/PNG 格式')
    return
  }
  if (!isLt2M) {
    // 限制大小为 14MB
    ElMessage.error('头像大小不能超过 14MB')
    return
  }
  // 转换为 Blob URL 供裁剪器使用
  cropperOptions.img = URL.createObjectURL(file)
  showCropper.value = true // 切换到裁剪模式
}

// --- 裁剪器操作 ---
const rotateLeft = () => {
  cropperRef.value.rotateLeft()
}

const rotateRight = () => {
  cropperRef.value.rotateRight()
}

const changeScale = (num) => {
  cropperRef.value.changeScale(num)
}

const cancelCrop = () => {
  showCropper.value = false
  cropperOptions.img = ''
}

const finishCrop = () => {
  // 获取裁剪后的数据
  cropperRef.value.getCropData((data) => {
    tempAvatarUrl.value = data
    showCropper.value = false
  })
}

// 主题切换处理
const handleThemeCommand = (command) => {
  themeStore.setTheme(command)
}

/**
 * 退出登录处理
 * 1. 调用 store 的 logout 清除状态
 * 2. 跳转回登录页
 * 3. 显示提示消息
 */
const handleLogout = async () => {
  await authStore.logout()
  router.push('/login')
  ElMessage.success('退出登录成功')
}

// 当前菜单高亮根据路由路径
const activeMenu = computed(() => {
  const { meta, path } = route
  // 如果 meta 中配置了 activeMenu，则使用它（用于详情页高亮父级菜单）
  if (meta.activeMenu) {
    return meta.activeMenu
  }
  return path
})
// 顶部标题取自当前路由的 meta.title
const currentPage = computed(() => route.meta?.title || '首页')
</script>

<style scoped>
.app-container {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
}

.main-container {
  height: 100%;
}

.aside-menu {
  background-color: var(--aside-bg-color);
  transition: width 0.3s;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* 移动端抽屉样式 */
:deep(.mobile-aside-drawer) {
  background-color: var(--aside-bg-color) !important;
}

:deep(.mobile-aside-drawer .el-drawer__body) {
  padding: 0;
  background-color: var(--aside-bg-color);
  display: flex;
  flex-direction: column;
}

.mobile-menu {
  border-right: none;
  flex: 1;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--aside-bg-color);
  color: white;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo-text {
  font-size: 20px;
  font-weight: bold;
  white-space: nowrap;
}

.el-menu-vertical-demo {
  border-right: none;
  flex: 1;
}

.header {
  background-color: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
}

.page-title {
  font-size: 16px;
  font-weight: 500;
  margin-left: 10px;
}

.toggle-icon {
  font-size: 20px;
  cursor: pointer;
  transition: color 0.3s;
}

.toggle-icon:hover {
  color: var(--el-color-primary);
}

.header-right {
  display: flex;
  align-items: center;
}

.theme-switch {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 0 10px;
  height: 60px;
  transition: background-color 0.3s;
}

.theme-switch:hover {
  background-color: #f6f6f6;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 0 10px;
  height: 60px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f6f6f6;
}

.main-content {
  background-color: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}

@media (max-width: 768px) {
  .header {
    padding: 0 10px;
  }
  .page-title {
    font-size: 14px;
    max-width: 120px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  .theme-dropdown {
    margin-right: 10px;
  }
  :deep(.theme-switch span) {
    display: none;
  }
  .user-info {
    padding: 0 5px;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  .user-info .el-avatar {
    margin-right: 0;
    width: 32px;
    height: 32px;
  }
  .user-info .username {
    display: none;
  }
  .user-info .el-icon {
    display: none;
  }
  .main-content {
    padding: 10px;
  }
}

.icon-margin-right {
  margin-right: 8px;
}

.icon-margin-left {
  margin-left: 8px;
}

.mt-20 {
  margin-top: 20px;
}

/* 头像选择器样式 */
.avatar-selection {
  padding: 20px;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 15px;
  color: #333;
}

.preset-avatars {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
  gap: 15px;
}

.avatar-item {
  position: relative;
  cursor: pointer;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 5px;
  border: 2px solid transparent;
  border-radius: 8px;
  transition: all 0.3s;
}

.avatar-item:hover {
  background-color: #f5f7fa;
}

.avatar-item.active {
  border-color: var(--el-color-primary);
  background-color: #ecf5ff;
}

.check-mark {
  position: absolute;
  bottom: -5px;
  right: -5px;
  background-color: var(--el-color-primary);
  color: white;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 12px;
  border: 2px solid white;
}

.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 100px;
  height: 100px;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: border-color 0.3s;
}

.avatar-uploader:hover {
  border-color: var(--el-color-primary);
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}

.avatar {
  width: 100px;
  height: 100px;
  display: block;
  object-fit: cover;
}

.avatar-cropper-container {
  height: 400px;
  display: flex;
  flex-direction: column;
}

.cropper-wrapper {
  flex: 1;
  width: 100%;
}

.cropper-toolbar {
  margin-top: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.action-btns {
  display: flex;
  gap: 10px;
}

.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
