<template>
  <!--
    UserManagement.vue - 用户管理页面
    
    这是一个典型的 CRUD (增删改查) 页面，展示了表格数据的展示、搜索、分页以及表单的增删改操作。
    主要功能点：
    1. 表格展示：使用 el-table 展示用户列表，支持自定义列模板（如状态开关、角色标签）
    2. 搜索过滤：顶部搜索栏，支持多条件组合查询
    3. 分页功能：el-pagination 实现数据分页
    4. 弹窗表单：新增/编辑共用一个弹窗 (el-dialog)，复用表单逻辑
    5. 表单验证：使用 el-form 的 rules 属性进行前端校验
  -->
  <div class="management-container">
    <el-card class="management-card">
      <template #header>
        <div class="card-header-content">
          <span>{{ route.meta.title }}</span>
        </div>
      </template>
      <!-- 顶部搜索栏 -->
      <div class="search-bar-container">
        <!-- PC端搜索栏 -->
        <div v-if="!isMobile" class="search-bar">
          <el-button v-if="canAddUser" type="primary" :icon="Plus" @click="handleAddUser">
            新增用户
          </el-button>
          <el-input v-model="searchForm.username" placeholder="用户名" clearable class="w-200" />
          <el-input v-model="searchForm.name" placeholder="姓名" clearable class="w-200" />
          <el-select v-model="searchForm.roleId" placeholder="角色" clearable class="w-200">
            <el-option
              v-for="role in roleList"
              :key="role.id"
              :label="role.name"
              :value="role.id"
            />
          </el-select>
          <el-select v-model="searchForm.status" placeholder="状态" clearable class="w-200">
            <el-option
              v-for="item in userStatusList"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
          <el-button v-if="canQueryUser" type="primary" :icon="Search" @click="handleSearch">
            查询
          </el-button>
          <el-button v-if="canQueryUser" :icon="Refresh" @click="resetSearch"> 重置 </el-button>
        </div>

        <!-- 移动端搜索栏 -->
        <div v-else class="mobile-search-bar">
          <el-button v-if="canAddUser" type="primary" :icon="Plus" @click="handleAddUser">
            新增
          </el-button>
          <el-input
            v-model="searchForm.username"
            placeholder="搜索用户名"
            clearable
            class="mobile-main-search"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><search /></el-icon>
            </template>
          </el-input>
          <el-button v-if="canQueryUser" :icon="Filter" @click="filterDrawerVisible = true"
            >筛选</el-button
          >
        </div>
      </div>

      <!-- 用户数据列表 -->
      <!-- PC端表格 -->
      <el-table
        v-if="!isMobile"
        v-loading="loading"
        :data="userList"
        stripe
        class="user-table"
        height="100%"
      >
        <el-table-column type="selection" width="55" fixed="left" />
        <el-table-column prop="id" label="ID" width="80" fixed="left" />
        <el-table-column label="头像" width="80">
          <template #default="scope">
            <el-avatar
              :size="40"
              :src="
                scope.row.avatar ||
                'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
              "
            />
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" min-width="120" show-overflow-tooltip />
        <el-table-column prop="name" label="姓名" min-width="120" show-overflow-tooltip />
        <el-table-column prop="email" label="邮箱" min-width="200" show-overflow-tooltip />
        <el-table-column prop="phone" label="电话" min-width="150" show-overflow-tooltip />
        <el-table-column prop="idCard" label="身份证号" min-width="180" show-overflow-tooltip />
        <el-table-column prop="role" label="角色" min-width="200">
          <template #default="scope">
            <!-- 优先显示 roles 列表 -->
            <template v-if="scope.row.roles && scope.row.roles.length > 0">
              <el-tag
                v-for="(role, index) in scope.row.roles"
                :key="typeof role === 'object' ? role.id : index"
                :type="getRoleTagType(typeof role === 'object' ? role.id : role)"
                class="mr-5"
              >
                {{ getRoleName(role) }}
              </el-tag>
            </template>
            <!-- 兼容旧结构 -->
            <template v-else-if="scope.row.role || scope.row.roleId">
              <el-tag :type="getRoleTagType(scope.row.role?.id || scope.row.roleId)">
                {{ getRoleName(scope.row.role || scope.row.roleId) }}
              </el-tag>
            </template>
            <span v-else class="text-secondary">暂无角色</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="lastLoginTime"
          label="最后登录时间"
          min-width="180"
          show-overflow-tooltip
        />
        <el-table-column
          prop="lastLoginIp"
          label="最后登录IP"
          min-width="140"
          show-overflow-tooltip
        />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <!-- 状态开关，直接绑定行数据 -->
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              active-color="var(--el-color-success)"
              inactive-color="var(--el-color-danger)"
              @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" min-width="180" show-overflow-tooltip />
        <el-table-column prop="updatedAt" label="更新时间" min-width="180" show-overflow-tooltip />
        <el-table-column prop="operation" label="操作" width="280px" fixed="right">
          <template #default="scope">
            <el-button
              v-if="canEditUser"
              type="primary"
              size="small"
              :icon="Edit"
              @click="handleEditUser(scope.row)"
            >
              编辑
            </el-button>
            <el-button
              v-if="canResetPassword"
              type="warning"
              size="small"
              :icon="Lock"
              @click="handleResetPassword(scope.row)"
            >
              重置密码
            </el-button>
            <el-button
              v-if="canDeleteUser"
              type="danger"
              size="small"
              :icon="Delete"
              @click="handleDeleteUser(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 移动端卡片列表 -->
      <div v-else class="mobile-card-list" v-loading="loading">
        <el-empty v-if="userList.length === 0" description="暂无用户数据" />
        <div v-for="row in userList" :key="row.id" class="mobile-card-wrapper">
          <el-card class="mobile-card-item" shadow="hover">
            <div class="card-top">
              <div class="user-info">
                <el-avatar
                  :size="40"
                  :src="
                    row.avatar ||
                    'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
                  "
                />
                <div class="user-name-group">
                  <span class="username">{{ row.username }}</span>
                </div>
              </div>
              <el-switch
                v-model="row.status"
                :active-value="1"
                :inactive-value="0"
                size="small"
                @change="handleStatusChange(row)"
              />
            </div>
            <div class="card-content">
              <div class="content-item">
                <span class="label">姓名:</span>
                <span class="value">{{ row.name }}</span>
              </div>
              <div class="content-item">
                <span class="label">角色:</span>
                <div class="value">
                  <template v-if="row.roles && row.roles.length > 0">
                    <el-tag
                      v-for="(role, index) in row.roles"
                      :key="typeof role === 'object' ? role.id : index"
                      :type="getRoleTagType(typeof role === 'object' ? role.id : role)"
                      size="small"
                      class="mr-2"
                    >
                      {{ getRoleName(role) }}
                    </el-tag>
                  </template>
                  <template v-else-if="row.role || row.roleId">
                    <el-tag :type="getRoleTagType(row.role?.id || row.roleId)" size="small">
                      {{ getRoleName(row.role || row.roleId) }}
                    </el-tag>
                  </template>
                  <span v-else class="text-secondary">暂无角色</span>
                </div>
              </div>
              <div class="content-item">
                <span class="label">身份证号:</span>
                <span class="value">{{ row.idCard || '-' }}</span>
              </div>
              <div class="content-item">
                <span class="label">邮箱:</span>
                <span class="value">{{ row.email }}</span>
              </div>
              <div class="content-item">
                <span class="label">电话:</span>
                <span class="value">{{ row.phone }}</span>
              </div>
              <div class="content-item">
                <span class="label">最后登录:</span>
                <span class="value">{{ row.lastLoginTime || '从未登录' }}</span>
              </div>
              <div class="content-item">
                <span class="label">登录IP:</span>
                <span class="value">{{ row.lastLoginIp || '-' }}</span>
              </div>
              <div class="content-item">
                <span class="label">创建时间:</span>
                <span class="value">{{ row.createdAt }}</span>
              </div>
            </div>
            <div class="card-actions">
              <el-button
                v-if="canEditUser"
                type="primary"
                size="small"
                :icon="Edit"
                @click="handleEditUser(row)"
              >
                编辑
              </el-button>
              <el-button
                v-if="canResetPassword"
                type="warning"
                size="small"
                :icon="Lock"
                @click="handleResetPassword(row)"
              >
                重置
              </el-button>
              <el-button
                v-if="canDeleteUser"
                type="danger"
                size="small"
                :icon="Delete"
                @click="handleDeleteUser(row)"
              >
                删除
              </el-button>
            </div>
          </el-card>
        </div>
      </div>

      <!-- 移动端筛选抽屉 -->
      <el-drawer
        v-model="filterDrawerVisible"
        title="筛选条件"
        direction="rtl"
        size="100%"
        class="mobile-filter-drawer"
      >
        <el-form :model="searchForm" label-position="top">
          <el-form-item label="姓名">
            <el-input v-model="searchForm.name" placeholder="请输入姓名" clearable />
          </el-form-item>
          <el-form-item label="角色">
            <el-select v-model="searchForm.roleId" placeholder="全部角色" clearable class="w-full">
              <el-option
                v-for="role in roleList"
                :key="role.id"
                :label="role.name"
                :value="role.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="全部状态" clearable class="w-full">
              <el-option
                v-for="item in userStatusList"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="drawer-footer">
            <el-button @click="resetSearch">重置</el-button>
            <el-button type="primary" @click="handleFilterConfirm">确定</el-button>
          </div>
        </template>
      </el-drawer>

      <!-- 分页组件 -->
      <div class="pagination">
        <div v-if="isMobile" class="mobile-pagination">
          <el-button
            size="small"
            :disabled="pagination.currentPage === 1"
            @click="handleCurrentChange(1)"
          >
            首页
          </el-button>
          <el-pagination
            v-model:current-page="pagination.currentPage"
            v-model:page-size="pagination.pageSize"
            :total="pagination.total"
            layout="prev, pager, next"
            :small="true"
            @current-change="handleCurrentChange"
          />
          <el-button
            size="small"
            :disabled="pagination.currentPage >= Math.ceil(pagination.total / pagination.pageSize)"
            @click="handleCurrentChange(Math.ceil(pagination.total / pagination.pageSize))"
          >
            末页
          </el-button>
        </div>
        <el-pagination
          v-else
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>

  <!-- 新增/编辑用户抽屉 -->
  <el-drawer
    v-model="userDialogVisible"
    title="用户信息"
    :size="isMobile ? '100%' : '900px'"
    :close-on-click-modal="false"
    destroy-on-close
    class="user-drawer"
  >
    <div v-if="isMobile" class="mobile-user-form">
      <!-- 移动端不使用 splitpanes，直接平铺 -->
      <el-scrollbar>
        <div class="user-form-container">
          <el-form
            :model="userForm"
            :rules="userRules"
            ref="userFormRef"
            label-position="top"
            class="user-form"
          >
            <!-- 基本信息 -->
            <div class="form-section">
              <h3 class="section-title">基本信息</h3>
              <el-form-item label="用户名" prop="username">
                <el-input v-model="userForm.username" placeholder="请输入用户名" clearable />
              </el-form-item>
              <el-form-item label="头像" prop="avatar">
                <el-upload
                  class="avatar-uploader"
                  action="#"
                  :show-file-list="false"
                  :auto-upload="false"
                  :on-change="handleAvatarChange"
                  accept=".jpg,.jpeg,.png,.gif,.bmp,.webp"
                >
                  <img v-if="userForm.avatar" :src="userForm.avatar" class="avatar" />
                  <el-icon v-else class="avatar-uploader-icon">
                    <Plus />
                  </el-icon>
                </el-upload>
              </el-form-item>
              <el-form-item label="姓名" prop="name">
                <el-input v-model="userForm.name" placeholder="请输入姓名" clearable />
              </el-form-item>
              <el-form-item label="身份证号" prop="idCard">
                <el-input v-model="userForm.idCard" placeholder="请输入身份证号" clearable />
              </el-form-item>
            </div>

            <!-- 联系方式 -->
            <div class="form-section">
              <el-divider />
              <h3 class="section-title">联系方式</h3>
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="userForm.email" placeholder="请输入邮箱" clearable />
              </el-form-item>
              <el-form-item label="联系电话" prop="phone">
                <el-input v-model="userForm.phone" placeholder="请输入联系电话" clearable />
              </el-form-item>
            </div>

            <!-- 地址信息 -->
            <div class="form-section">
              <el-divider />
              <h3 class="section-title">地址信息</h3>
              <el-form-item label="教研地址">
                <el-cascader
                  v-model="linkageAddress"
                  :options="regionData"
                  placeholder="请选择省/市/区"
                  class="w-full"
                  clearable
                />
              </el-form-item>
              <el-form-item label="详细地址">
                <el-input
                  v-model="detailedAddress"
                  type="textarea"
                  placeholder="请输入详细地址"
                  clearable
                />
              </el-form-item>
            </div>

            <!-- 账户与角色 -->
            <div class="form-section">
              <el-divider />
              <h3 class="section-title">账户与角色</h3>
              <el-form-item label="角色" prop="roleIds">
                <el-select
                  v-model="userForm.roleIds"
                  placeholder="请选择角色"
                  multiple
                  class="w-full"
                >
                  <el-option
                    v-for="role in roleList"
                    :key="role.id"
                    :label="role.name"
                    :value="role.id"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="状态" prop="status">
                <el-switch
                  v-model="userForm.status"
                  :active-value="1"
                  :inactive-value="0"
                  active-color="var(--el-color-success)"
                  inactive-color="var(--el-color-danger)"
                />
              </el-form-item>
              <el-form-item label="密码" prop="password" v-if="!userForm.id">
                <el-input
                  v-model="userForm.password"
                  type="password"
                  placeholder="请输入密码"
                  show-password
                  clearable
                />
              </el-form-item>
            </div>
          </el-form>
        </div>
      </el-scrollbar>
    </div>
    <splitpanes v-else class="default-theme user-drawer-splitpanes">
      <pane size="22" min-size="15" class="user-menu-pane">
        <div class="user-menu-wrapper">
          <el-menu
            default-active="1"
            class="el-menu-vertical-demo user-menu"
            @select="handleMenuSelect"
          >
            <el-menu-item index="1">
              <el-icon>
                <User />
              </el-icon>
              <span>基本信息</span>
            </el-menu-item>
            <el-menu-item index="2">
              <el-icon>
                <Postcard />
              </el-icon>
              <span>联系方式</span>
            </el-menu-item>
            <el-menu-item index="3">
              <el-icon>
                <Location />
              </el-icon>
              <span>地址信息</span>
            </el-menu-item>
            <el-menu-item index="4">
              <el-icon>
                <Setting />
              </el-icon>
              <span>账户与角色</span>
            </el-menu-item>
          </el-menu>
        </div>
      </pane>
      <pane class="user-form-pane">
        <div class="user-form-container">
          <el-scrollbar>
            <el-form
              :model="userForm"
              :rules="userRules"
              ref="userFormRef"
              label-width="100px"
              class="user-form"
            >
              <!-- 基本信息 -->
              <div id="section-1" class="form-section">
                <h3 class="section-title">基本信息</h3>
                <el-form-item label="用户名" prop="username">
                  <el-autocomplete
                    v-model="userForm.username"
                    :fetch-suggestions="querySearchUsername"
                    placeholder="请输入用户名"
                    clearable
                    class="w-full"
                  />
                </el-form-item>
                <el-form-item label="头像" prop="avatar">
                  <el-upload
                    class="avatar-uploader"
                    action="#"
                    :show-file-list="false"
                    :auto-upload="false"
                    :on-change="handleAvatarChange"
                    accept=".jpg,.jpeg,.png,.gif,.bmp,.webp"
                  >
                    <img v-if="userForm.avatar" :src="userForm.avatar" class="avatar" />
                    <el-icon v-else class="avatar-uploader-icon">
                      <Plus />
                    </el-icon>
                  </el-upload>
                </el-form-item>
                <el-form-item label="姓名" prop="name">
                  <el-autocomplete
                    v-model="userForm.name"
                    :fetch-suggestions="querySearchName"
                    placeholder="请输入姓名"
                    clearable
                    class="w-full"
                  />
                </el-form-item>
                <el-form-item label="身份证号" prop="idCard">
                  <el-input v-model="userForm.idCard" placeholder="请输入身份证号" clearable />
                </el-form-item>
              </div>

              <!-- 联系方式 -->
              <div id="section-2" class="form-section">
                <el-divider />
                <h3 class="section-title">联系方式</h3>
                <el-form-item label="邮箱" prop="email">
                  <el-autocomplete
                    v-model="userForm.email"
                    :fetch-suggestions="querySearchEmail"
                    placeholder="请输入邮箱"
                    clearable
                    class="w-full"
                  />
                </el-form-item>
                <el-form-item label="联系电话" prop="phone">
                  <el-input v-model="userForm.phone" placeholder="请输入联系电话" clearable />
                </el-form-item>
              </div>

              <!-- 地址信息 -->
              <div id="section-3" class="form-section">
                <el-divider />
                <h3 class="section-title">地址信息</h3>
                <el-form-item label="教研地址">
                  <el-cascader
                    v-model="linkageAddress"
                    :options="regionData"
                    placeholder="请选择省/市/区"
                    class="w-full"
                    clearable
                  />
                </el-form-item>
                <el-form-item label="详细地址">
                  <el-input
                    v-model="detailedAddress"
                    type="textarea"
                    placeholder="请输入详细地址"
                    clearable
                  />
                </el-form-item>
              </div>

              <!-- 账户与角色 -->
              <div id="section-4" class="form-section">
                <el-divider />
                <h3 class="section-title">账户与角色</h3>
                <el-form-item label="角色" prop="roleIds">
                  <el-select v-model="userForm.roleIds" placeholder="请选择角色" multiple>
                    <el-option
                      v-for="role in roleList"
                      :key="role.id"
                      :label="role.name"
                      :value="role.id"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item label="状态" prop="status">
                  <el-switch
                    v-model="userForm.status"
                    :active-value="1"
                    :inactive-value="0"
                    active-color="var(--el-color-success)"
                    inactive-color="var(--el-color-danger)"
                  />
                </el-form-item>
                <!-- 只有新增时才需要输入初始密码 -->
                <el-form-item label="密码" prop="password" v-if="!userForm.id">
                  <el-input
                    v-model="userForm.password"
                    type="password"
                    placeholder="请输入密码"
                    show-password
                    clearable
                  />
                </el-form-item>
              </div>
            </el-form>
          </el-scrollbar>
        </div>
      </pane>
    </splitpanes>
    <template #footer>
      <div class="drawer-footer">
        <el-button @click="userDialogVisible = false">取消</el-button>
        <el-button type="primary" :icon="Check" :loading="submitting" @click="handleSaveUser"
          >保存</el-button
        >
      </div>
    </template>
  </el-drawer>

  <!-- 裁剪头像抽屉 -->
  <el-drawer
    v-model="cropperDialogVisible"
    title="图片裁剪"
    size="600px"
    append-to-body
    destroy-on-close
  >
    <div class="cropper-container">
      <vue-cropper
        ref="cropperRef"
        :img="cropperOptions.img"
        :output-size="1"
        :output-type="cropperOptions.outputType"
        :info="true"
        :auto-crop="cropperOptions.autoCrop"
        :auto-crop-width="cropperOptions.autoCropWidth"
        :auto-crop-height="cropperOptions.autoCropHeight"
        :fixed-box="cropperOptions.fixedBox"
        :center-box="cropperOptions.centerBox"
      />
    </div>
    <template #footer>
      <div class="flex-auto">
        <el-button :icon="Refresh" @click="cropperDialogVisible = false">取消</el-button>
        <el-button type="primary" :icon="Upload" @click="handleCropConfirm" :loading="cropping"
          >确认并上传</el-button
        >
      </div>
    </template>
  </el-drawer>
</template>

<script setup>
import { ref, reactive, onMounted, computed, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

import { storeToRefs } from 'pinia'
import { hasPermission } from '@/utils/permission'
import { PERMISSIONS } from '@/constants/permissionConstants'
import { ElMessage, ElNotification, ElMessageBox } from 'element-plus'
import {
  Plus,
  Search,
  Refresh,
  Location,
  Setting,
  User,
  Postcard,
  Edit,
  Lock,
  Delete,
  Check,
  Upload,
  Filter,
} from '@element-plus/icons-vue'
import {
  getUsers,
  createUser,
  updateUser,
  updateUserStatus,
  resetPassword,
  assignRoles,
  deleteUser,
} from '@/api/user'
import { getRoles } from '@/api/role'
import { getUserStatusDict } from '@/api/common'
import http from '@/utils/request'
import { regionData } from '@/utils/regionData'
import { VueCropper } from 'vue-cropper'
import 'vue-cropper/dist/index.css'
import { Splitpanes, Pane } from 'splitpanes'

const route = useRoute()

// 权限控制
const authStore = useAuthStore()
const { permissions: perms } = storeToRefs(authStore)

const canAddUser = computed(
  () =>
    hasPermission(perms.value, PERMISSIONS.USER_ADD) ||
    hasPermission(perms.value, PERMISSIONS.USER_ADD_LEGACY),
)
const canEditUser = computed(
  () =>
    hasPermission(perms.value, PERMISSIONS.USER_EDIT) ||
    hasPermission(perms.value, PERMISSIONS.USER_EDIT_LEGACY),
)
const canDeleteUser = computed(
  () =>
    hasPermission(perms.value, PERMISSIONS.USER_DELETE) ||
    hasPermission(perms.value, PERMISSIONS.USER_DELETE_LEGACY),
)
const canResetPassword = computed(
  () =>
    hasPermission(perms.value, PERMISSIONS.USER_RESET) ||
    hasPermission(perms.value, PERMISSIONS.USER_EDIT),
)
const canQueryUser = computed(
  () =>
    hasPermission(perms.value, PERMISSIONS.USER_QUERY) ||
    hasPermission(perms.value, PERMISSIONS.USER_QUERY_LEGACY),
)

// --- 头像裁剪相关 ---
const cropperDialogVisible = ref(false)
const cropperRef = ref(null)
const cropping = ref(false)
const cropperOptions = reactive({
  img: '',
  autoCrop: true,
  autoCropWidth: 200,
  autoCropHeight: 200,
  fixedBox: true,
  outputType: 'png',
  centerBox: true,
})

const handleAvatarChange = (uploadFile) => {
  const file = uploadFile.raw
  if (!file) return

  // 允许的图片类型: jpg, png, gif, bmp, webp
  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/bmp', 'image/webp']
  const isAllowedType = allowedTypes.includes(file.type)
  const isLt2M = file.size / 1024 / 1024 < 14

  if (!isAllowedType) {
    ElMessage.error('头像必须是 JPG, PNG, GIF, BMP 或 WEBP 格式!')
    return
  }
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过 14MB!')
    return
  }

  // 打开裁剪弹窗
  cropperOptions.img = URL.createObjectURL(file)
  cropperDialogVisible.value = true
}
//这是确认裁剪并上传头像的函数
const handleCropConfirm = () => {
  // 检查是否有裁剪器实例
  if (!cropperRef.value) return
  // 禁用确认按钮防止重复点击
  cropping.value = true
  // 调用裁剪器获取裁剪后的 Blob 数据
  cropperRef.value.getCropBlob(async (blob) => {
    try {
      // 创建 FormData 对象，用于上传文件
      const file = new File([blob], 'avatar.png', { type: 'image/png' })
      // 上传文件到后端
      const res = await http.upload('/common/upload', file)
      // 兼容返回结构
      const url = res.url || res
      // 检查是否成功获取到 URL
      if (url) {
        // 成功获取到 URL 后，更新用户表单的 avatar 字段
        userForm.avatar = url
        ElMessage.success('头像上传成功')
        // 上传成功后，刷新用户信息
        await authStore.getUserInfo()
        // 刷新成功后，关闭裁剪弹窗
        cropperDialogVisible.value = false
      } else {
        ElMessage.error('上传返回值异常')
      }
    } catch (error) {
      console.error('上传失败', error)
      ElMessage.error('头像上传失败')
    } finally {
      cropping.value = false
    }
  })
}

// 搜索表单
const searchForm = reactive({
  username: '',
  name: '',
  roleId: '',
  status: '',
})

// 用户列表数据
const userList = ref([])
const loading = ref(false)
const submitting = ref(false)

// 角色列表数据 (用于下拉选择)
const roleList = ref([])

// 获取角色列表
const fetchRoleList = async () => {
  try {
    // 获取所有角色用于下拉选择和列表显示，假设1000足够覆盖所有角色
    const res = await getRoles({ page: 1, size: 1000 })
    // 兼容后端返回结构：可能是数组，也可能是 { list: [...] }
    const roles = Array.isArray(res) ? res : res.list || res.records || res.data || []
    roleList.value = roles
  } catch (error) {
    console.error('获取角色列表失败:', error)
  }
}

// 分页信息
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0,
})

// 获取用户列表
const fetchUserList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.currentPage,
      size: pagination.pageSize,
      username: searchForm.username || undefined,
      name: searchForm.name || undefined,
      roleId: searchForm.roleId || undefined,
      // 修复 status 为 0 (禁用) 时被视为 false 的问题
      status:
        searchForm.status === '' || searchForm.status === undefined ? undefined : searchForm.status,
    }
    const res = await getUsers(params)
    // 处理列表数据
    const rawList = Array.isArray(res) ? res : res.list || res.records || res.data || []

    // 处理总数
    pagination.total = typeof res.total !== 'undefined' ? Number(res.total) : rawList.length

    userList.value = rawList.map((item) => {
      // 调试日志：查看单条用户数据结构
      // console.log('用户数据项:', item)

      // 严格按照后端返回处理角色数据
      // 假设后端可能返回:
      // 1. roleIds: [1, 2] (最常见)
      // 2. roles: [{id: 1, name: 'admin'}, ...] (完整对象)
      // 3. roleId: 1 (单角色)

      let roles = []

      // 优先使用 roles (如果是数组且包含对象)
      if (Array.isArray(item.roles) && item.roles.length > 0) {
        roles = item.roles
      }
      // 其次处理 roleIds (ID数组) - 转换为对象以便前端统一处理
      else if (Array.isArray(item.roleIds) && item.roleIds.length > 0) {
        roles = item.roleIds.map((id) => ({ id }))
      }
      // 处理单角色 ID
      else if (item.roleId) {
        roles = [{ id: item.roleId }]
      }
      // 处理 roleList 字段 (部分后端可能用这个名字)
      else if (Array.isArray(item.roleList) && item.roleList.length > 0) {
        roles = item.roleList
      }
      // 处理单角色对象
      else if (item.role) {
        roles = [item.role]
      }
      // 尝试从其他字段恢复 (如 userRoles)
      else if (Array.isArray(item.userRoles) && item.userRoles.length > 0) {
        roles = item.userRoles
      }

      return {
        ...item,
        roles: roles,
        status: item.status === undefined || item.status === null ? 1 : Number(item.status),
      }
    })
  } catch (error) {
    console.error('获取用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 用户状态字典
const userStatusList = ref([])

// 获取字典数据
const fetchDicts = async () => {
  try {
    const res = await getUserStatusDict()
    if (res && Array.isArray(res)) {
      userStatusList.value = res
    }
  } catch (error) {
    console.error('获取字典失败:', error)
  }
}

// 响应式处理
const isMobile = ref(window.innerWidth < 768)
const filterDrawerVisible = ref(false)

const handleResize = () => {
  isMobile.value = window.innerWidth < 768
}

const handleFilterConfirm = () => {
  filterDrawerVisible.value = false
  handleSearch()
}

onMounted(() => {
  fetchRoleList()
  fetchUserList()
  fetchDicts()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})

// 用户对话框控制
const userDialogVisible = ref(false)
const userFormRef = ref(null) // 获取表单实例，用于调用 validate 方法

// 菜单选择处理 - 滚动到对应区域
const handleMenuSelect = (index) => {
  const sectionId = `section-${index}`
  // 由于是在 Drawer 中，且可能未完全渲染，稍微延迟一下或直接获取
  // 注意：getElementById 获取的是全局 ID，确保 ID 唯一性
  // 考虑到 Drawer 是 destroy-on-close，每次打开都是新的，ID 冲突可能性小（除非同时打开多个，但 Drawer 通常是模态）
  const element = document.getElementById(sectionId)
  if (element) {
    element.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

// 表单数据模型
const userForm = reactive({
  id: '',
  username: '',
  name: '',
  email: '',
  phone: '',
  idCard: '',
  avatar: '',
  roleIds: [],
  status: 1,
  password: '',
})

// 地址相关状态
const linkageAddress = ref([])
const detailedAddress = ref('')

// 自动联想 - 用户名
const querySearchUsername = (queryString, cb) => {
  const users = userList.value
  const results = queryString
    ? users.filter(
        (item) => item.username && item.username.toLowerCase().includes(queryString.toLowerCase()),
      )
    : users
  const unique = [...new Set(results.map((item) => item.username))]
  cb(unique.map((val) => ({ value: val })))
}

// 自动联想 - 姓名
const querySearchName = (queryString, cb) => {
  const users = userList.value
  const results = queryString
    ? users.filter(
        (item) => item.name && item.name.toLowerCase().includes(queryString.toLowerCase()),
      )
    : users
  const unique = [...new Set(results.map((item) => item.name))]
  cb(unique.map((val) => ({ value: val })))
}

// 自动联想 - 邮箱
const querySearchEmail = (queryString, cb) => {
  const suffixes = [
    '@gmail.com',
    '@163.com',
    '@qq.com',
    '@outlook.com',
    '@hotmail.com',
    '@sina.com',
  ]
  if (!queryString) {
    cb([])
    return
  }
  // 如果已经包含 @，则不联想，或者可以优化为联想后缀
  if (queryString.indexOf('@') > -1) {
    cb([])
  } else {
    cb(suffixes.map((suffix) => ({ value: queryString + suffix })))
  }
}

// 用户表单验证规则
const userRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' },
  ],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
  ],
  phone: [
    { required: true, message: '请输入电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' },
  ],
  roleIds: [{ required: true, message: '请至少选择一个角色', trigger: 'change', type: 'array' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' },
  ],
}

// 获取角色名称
const getRoleName = (roleOrId) => {
  if (!roleOrId) return '未知角色'

  // 如果是对象且有名称，直接返回
  if (typeof roleOrId === 'object') {
    if (roleOrId.name) return roleOrId.name
    if (roleOrId.roleName) return roleOrId.roleName
    // 如果只有 ID，尝试查找
    if (roleOrId.id) {
      const found = roleList.value.find((r) => r.id === roleOrId.id)
      return found ? found.name || found.roleName : `ID:${roleOrId.id}`
    }
  }

  // 如果是 ID (数字或字符串)，在 roleList 中查找
  const id = roleOrId
  // 使用非严格相等以兼容数字/字符串 ID
  const found = roleList.value.find((r) => r.id == id)
  if (found) {
    return found.name || found.roleName
  }

  return `ID:${id}`
}

// 获取角色标签类型 (UI 辅助函数)
const getRoleTagType = (roleId) => {
  if (!roleId) return 'default'
  // 使用简单的哈希算法将 ID 映射到几种颜色
  const types = ['primary', 'success', 'warning', 'danger', 'info']
  const index = Number(roleId) % types.length
  return types[index]
}

// 处理状态变更
const handleStatusChange = async (row) => {
  try {
    console.log('更新用户状态:', row.id, '为', row.status)
    if (!row.id) {
      throw new Error('缺少用户 ID')
    }
    await updateUserStatus(row.id, row.status)
    ElMessage.success(`用户状态已更新为${row.status === 1 ? '启用' : '禁用'}`)
  } catch (error) {
    console.error('更新用户状态失败:', error)
    ElMessage.error('状态更新失败: ' + (error.message || '未知错误'))
    // 如果失败则恢复状态
    row.status = row.status === 1 ? 0 : 1
  }
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.currentPage = 1
  fetchUserList()
}

const handleCurrentChange = (page) => {
  pagination.currentPage = page
  fetchUserList()
}

// 搜索和重置
const handleSearch = () => {
  pagination.currentPage = 1
  fetchUserList()
}

const resetSearch = () => {
  // 重置所有搜索字段
  Object.keys(searchForm).forEach((key) => {
    searchForm[key] = ''
  })
  pagination.currentPage = 1
  fetchUserList()
}

// 点击新增用户
const handleAddUser = () => {
  // 重置表单数据
  Object.keys(userForm).forEach((key) => {
    if (key === 'roleIds') userForm[key] = []
    else userForm[key] = ''
  })
  userForm.status = 1 // 默认启用
  // 显式清除密码字段以防万一
  userForm.password = ''
  delete userForm.id

  // 重置地址
  linkageAddress.value = []
  detailedAddress.value = ''

  userDialogVisible.value = true
}

// 点击编辑用户
const handleEditUser = (row) => {
  // 将行数据复制到表单对象中
  Object.assign(userForm, row)

  // 处理地址回显
  // 简单处理：将原有地址放入详细地址中，省市区不回显（除非能解析）
  detailedAddress.value = row.address || ''
  linkageAddress.value = []

  // 处理角色回显: 将 row.roles (对象数组) 转换为 roleIds (ID数组)
  if (row.roles && Array.isArray(row.roles)) {
    userForm.roleIds = row.roles.map((r) => r.id)
  } else if (row.role || row.roleId) {
    // 兼容旧数据
    userForm.roleIds = [row.role?.id || row.roleId].filter(Boolean)
  } else {
    userForm.roleIds = []
  }

  // 确保清除密码，因为我们通常不在这里编辑它，如果不更改则留空
  userForm.password = ''
  userDialogVisible.value = true
}

// 保存用户 (新增或编辑)
const handleSaveUser = () => {
  // 合并地址
  // 注意：这里简单地将省市区代码(或名称，取决于配置)和详细地址拼接
  // 实际项目中可能需要根据 regionData 查找对应的 Label
  const regionStr = linkageAddress.value.join(' ')
  userForm.address = (regionStr + ' ' + detailedAddress.value).trim()

  // 调用表单校验
  userFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        let userId = userForm.id

        // roleIds 将在后续通过 assignRoles 单独处理
        // roles, permissions 等字段可能是从行数据复制过来的，需要排除以免造成后端反序列化错误
        const { roleIds, roles, permissions, ...payload } = userForm

        // 新增数据脱敏处理逻辑
        // 如果手机号包含 * 号，说明未修改，不提交该字段
        if (payload.phone && payload.phone.includes('*')) {
          delete payload.phone
        }
        // 如果身份证号包含 * 号，说明未修改，不提交该字段
        if (payload.idCard && payload.idCard.includes('*')) {
          delete payload.idCard
        }

        if (userId) {
          // 编辑
          await updateUser(userId, payload)
          ElMessage.success('用户信息已更新')
        } else {
          // 创建
          const res = await createUser(payload)

          // 后端现在返回完整的用户对象，直接获取 ID
          userId = res.id

          if (!userId) {
            console.warn('错误: 无法获取新用户的 ID，角色分配将跳过。响应数据:', res)
            ElMessage.warning('用户创建成功，但无法自动分配角色(未获取到新用户ID)')
          } else {
            ElMessage.success('用户已创建')
          }
        }

        // 分配角色 (只要 userId 存在，就尝试更新角色，允许清空)
        // 注意：如果是新增用户且没有选择角色，roleIds 为空数组，此时也应该调用以确保状态一致（或者后端有默认角色）
        // 这里我们放宽条件：只要是表单提交，且 roleIds 存在（数组），就调用接口
        if (userId && Array.isArray(userForm.roleIds)) {
          try {
            await assignRoles(userId, userForm.roleIds)
            // 如果是新增用户，分配角色成功后无需额外提示
          } catch (roleError) {
            console.error('分配角色失败', roleError)
            ElMessage.warning('用户基本信息已保存，但角色分配失败，请重试')
          }
        }

        userDialogVisible.value = false
        fetchUserList()
      } catch (error) {
        console.error('保存用户失败', error)
        // 错误通常由 request.js 拦截器提示，这里不再重复
      } finally {
        submitting.value = false
      }
    }
  })
}

// 重置密码
const handleResetPassword = (row) => {
  ElMessageBox.confirm(`确定要重置用户 ${row.username} 的密码吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      try {
        await resetPassword(row.id)
        ElNotification({
          title: '提示',
          message: `用户 ${row.username} 的密码已重置为默认密码`,
          type: 'success',
          duration: 3000,
        })
      } catch (error) {
        // 已处理
      }
    })
    .catch(() => {})
}

// 删除用户
const handleDeleteUser = (row) => {
  ElMessageBox.confirm(`确定要删除用户 ${row.username} 吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      try {
        await deleteUser(row.id)
        ElMessage.success('用户已删除')
        // 如果当前页只有一条数据且不是第一页，则跳转到上一页
        if (userList.value.length === 1 && pagination.currentPage > 1) {
          pagination.currentPage--
        }
        fetchUserList()
      } catch (error) {
        console.error('删除用户失败', error)
      }
    })
    .catch(() => {})
}
</script>

<style scoped>
/* 
 * 布局样式已提取到 @/assets/styles/management-layout.css 
 * 包含 .management-container, .management-card, .search-bar-container, .pagination 等
 * 移动端卡片样式已提取到 @/assets/styles/mobile-card.css
 */

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-name-group {
  display: flex;
  flex-direction: column;
}

.username {
  font-weight: bold;
  font-size: 14px;
}

.name {
  font-size: 12px;
  color: #909399;
}

.mobile-user-form {
  height: 100%;
}

.section-title {
  margin-top: 0;
  margin-bottom: 15px;
  font-size: 16px;
  color: var(--el-color-primary);
}

.form-section {
  padding-bottom: 10px;
}

/* 头像上传样式 */
.avatar-uploader {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
  width: 100px;
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-uploader:hover {
  border-color: var(--el-color-primary);
}

.avatar-uploader-icon {
  font-size: 28px;
  color: var(--el-text-color-secondary);
  width: 100px;
  height: 100px;
  text-align: center;
  display: flex;
  justify-content: center;
  align-items: center;
}

.avatar {
  width: 100px;
  height: 100px;
  display: block;
  object-fit: cover;
}

/* Refactored Styles */

.user-table {
  width: 100%;
  flex: 1;
}

.mr-5 {
  margin-right: 5px;
}

.text-secondary {
  color: var(--el-text-color-secondary);
}

.text-danger {
  color: var(--el-color-danger);
}

.user-drawer-splitpanes {
  height: 100%;
  border: 1px solid var(--el-border-color-light);
}

.user-menu-pane {
  background-color: transparent;
}

.user-menu-wrapper {
  padding: 10px;
  height: 100%;
}

.user-menu {
  border-right: none;
  background-color: transparent;
}

.user-form-pane {
  overflow: hidden;
  background-color: transparent;
}

.user-form-container {
  padding: 20px;
  height: 100%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}

.user-form {
  padding-right: 20px;
}

.flex-auto {
  flex: auto;
}

.cropper-container {
  height: 400px;
}
</style>
