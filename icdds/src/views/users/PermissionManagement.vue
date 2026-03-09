<template>
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
          <!-- 新增权限按钮 - 需要新增权限权限 -->
          <el-button v-if="canAddPermission" type="primary" @click="handleAdd" :icon="Plus">
            新增权限
          </el-button>
          <el-input
            v-model="searchForm.name"
            placeholder="权限名称"
            clearable
            class="w-200"
            @keyup.enter="handleSearch"
          />
          <el-input
            v-model="searchForm.code"
            placeholder="权限标识"
            clearable
            class="w-200"
            @keyup.enter="handleSearch"
          />
          <el-select
            v-model="searchForm.type"
            placeholder="类型"
            clearable
            class="w-200"
            @change="handleSearch"
          >
            <el-option label="菜单" :value="1" />
            <el-option label="按钮" :value="2" />
          </el-select>
          <!-- 查询按钮 - 需要查询权限权限 -->
          <el-button v-if="canQueryPermission" type="primary" @click="handleSearch" :icon="Search">
            查询
          </el-button>
          <!-- 重置按钮 - 需要查询权限权限 -->
          <el-button v-if="canQueryPermission" @click="resetSearch" :icon="Refresh">
            重置
          </el-button>
        </div>

        <!-- 移动端搜索栏 -->
        <div v-else class="mobile-search-bar">
          <!-- 新增按钮 - 需要新增权限权限 -->
          <el-button v-if="canAddPermission" type="primary" :icon="Plus" @click="handleAdd">
            新增
          </el-button>
          <el-input
            v-model="searchForm.name"
            placeholder="搜索权限名称"
            clearable
            class="mobile-main-search"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <!-- 筛选按钮 - 需要查询权限权限 -->
          <el-button v-if="canQueryPermission" :icon="Filter" @click="filterDrawerVisible = true">
            筛选
          </el-button>
        </div>
      </div>

      <!-- 移动端筛选抽屉 -->
      <el-drawer
        v-if="isMobile"
        v-model="filterDrawerVisible"
        title="搜索筛选"
        size="100%"
        direction="rtl"
        class="mobile-filter-drawer"
      >
        <el-form :model="searchForm" label-position="top" class="mobile-filter-form">
          <el-form-item label="权限名称">
            <el-input v-model="searchForm.name" placeholder="请输入名称" clearable />
          </el-form-item>
          <el-form-item label="权限标识">
            <el-input v-model="searchForm.code" placeholder="请输入标识" clearable />
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="searchForm.type" placeholder="请选择类型" clearable class="w-full">
              <el-option label="菜单" :value="1" />
              <el-option label="按钮" :value="2" />
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

      <!-- 权限列表表格 -->
      <el-table
        v-if="!isMobile"
        v-loading="loading"
        :data="permissionList"
        class="permission-table"
        height="100%"
        row-key="id"
        stripe
        default-expand-all
        highlight-current-row
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column
          prop="name"
          label="权限名称"
          fixed="left"
          min-width="180"
          show-overflow-tooltip
        />
        <el-table-column prop="code" label="权限标识" min-width="200" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.type === 'menu' || scope.row.type == 1 ? 'success' : 'info'">
              {{ scope.row.type === 'menu' || scope.row.type == 1 ? '菜单' : '按钮' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路由路径" min-width="180" show-overflow-tooltip />
        <el-table-column prop="component" label="组件路径" min-width="260" show-overflow-tooltip />
        <el-table-column prop="icon" label="图标" min-width="120" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        <el-table-column label="操作" width="280px" fixed="right">
          <template #default="scope">
            <!-- 添加下级按钮 - 需要新增权限权限 -->
            <el-button
              v-if="canAddChild"
              size="small"
              type="primary"
              :icon="Plus"
              @click="handleAddSub(scope.row)"
            >
              添加下级
            </el-button>
            <!-- 编辑按钮 - 需要编辑权限权限 -->
            <el-button
              v-if="canEditPermission"
              size="small"
              type="primary"
              :icon="Edit"
              @click="handleEdit(scope.row)"
            >
              编辑
            </el-button>
            <!-- 删除按钮 - 需要删除权限权限 -->
            <el-button
              v-if="canDeletePermission"
              size="small"
              type="danger"
              :icon="Delete"
              @click="handleDelete(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 移动端卡片列表 -->
      <div v-else class="mobile-card-list" v-loading="loading">
        <el-empty v-if="permissionList.length === 0" description="暂无权限数据" />
        <div v-for="item in flattenedPermissions" :key="item.id" class="permission-card-wrapper">
          <el-card class="mobile-card-item" shadow="hover">
            <div class="card-top">
              <div class="name-section">
                <span v-for="n in item.level" :key="n" class="indent-dot"></span>
                <el-icon v-if="item.children && item.children.length > 0" class="folder-icon"
                  ><Folder
                /></el-icon>
                <span class="permission-name">{{ item.name }}</span>
              </div>
              <el-tag
                :type="item.type === 'menu' || item.type == 1 ? 'success' : 'info'"
                size="small"
              >
                {{ item.type === 'menu' || item.type == 1 ? '菜单' : '按钮' }}
              </el-tag>
            </div>
            <div class="card-content">
              <div class="content-item">
                <span class="label">ID:</span>
                <span class="value">{{ item.id }}</span>
              </div>
              <div class="content-item">
                <span class="label">标识:</span>
                <span class="value code">{{ item.code }}</span>
              </div>
              <div v-if="item.path" class="content-item">
                <span class="label">路径:</span>
                <span class="value">{{ item.path }}</span>
              </div>
              <div v-if="item.component" class="content-item">
                <span class="label">组件:</span>
                <span class="value">{{ item.component }}</span>
              </div>
              <div v-if="item.icon" class="content-item">
                <span class="label">图标:</span>
                <span class="value">{{ item.icon }}</span>
              </div>
              <div class="content-item">
                <span class="label">排序:</span>
                <span class="value">{{ item.sortOrder }}</span>
              </div>
              <div v-if="item.createTime" class="content-item">
                <span class="label">创建:</span>
                <span class="value">{{ item.createTime }}</span>
              </div>
              <div v-if="item.updateTime" class="content-item">
                <span class="label">更新:</span>
                <span class="value">{{ item.updateTime }}</span>
              </div>
            </div>
            <div class="card-actions">
              <!-- 下级按钮 - 需要新增权限权限 -->
              <el-button
                v-if="canAddChild"
                size="small"
                type="primary"
                :icon="Plus"
                @click="handleAddSub(item)"
              >
                下级
              </el-button>
              <!-- 编辑按钮 - 需要编辑权限权限 -->
              <el-button
                v-if="canEditPermission"
                size="small"
                type="primary"
                :icon="Edit"
                @click="handleEdit(item)"
              >
                编辑
              </el-button>
              <!-- 删除按钮 - 需要删除权限权限 -->
              <el-button
                v-if="canDeletePermission"
                size="small"
                type="danger"
                :icon="Delete"
                @click="handleDelete(item)"
              >
                删除
              </el-button>
            </div>
          </el-card>
        </div>
      </div>
    </el-card>

    <!-- 权限表单抽屉 -->
    <el-drawer
      v-model="dialogVisible"
      :title="dialogTitle"
      :size="isMobile ? '100%' : '900px'"
      :close-on-click-modal="false"
      destroy-on-close
      @close="resetForm"
    >
      <div v-if="isMobile" class="mobile-form-container">
        <el-form
          ref="permissionFormRef"
          :model="permissionForm"
          :rules="rules"
          label-position="top"
          class="permission-form"
        >
          <div class="form-section">
            <h3 class="section-title">基础信息</h3>
            <el-form-item label="上级权限" prop="parentId">
              <el-tree-select
                v-model="permissionForm.parentId"
                :data="permissionTreeData"
                :props="{ label: 'name', value: 'id', children: 'children' }"
                check-strictly
                placeholder="请选择上级权限"
                clearable
                class="w-full"
              />
            </el-form-item>
            <el-form-item label="权限名称" prop="name">
              <el-input v-model="permissionForm.name" placeholder="请输入权限名称" clearable />
            </el-form-item>
            <el-form-item label="权限标识" prop="code">
              <el-input v-model="permissionForm.code" placeholder="例如: user:add" clearable />
            </el-form-item>
            <el-form-item label="类型" prop="type">
              <el-radio-group v-model="permissionForm.type">
                <el-radio :value="1">菜单</el-radio>
                <el-radio :value="2">按钮</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="排序" prop="sortOrder">
              <el-slider v-model="permissionForm.sortOrder" :min="0" :max="20" class="w-full" />
            </el-form-item>
          </div>

          <div v-if="permissionForm.type == 1" class="form-section">
            <el-divider />
            <h3 class="section-title">菜单配置</h3>
            <el-form-item label="路由路径" prop="path">
              <el-input v-model="permissionForm.path" placeholder="例如: /users" clearable />
            </el-form-item>
            <el-form-item label="图标" prop="icon">
              <el-input v-model="permissionForm.icon" placeholder="例如: User" clearable />
            </el-form-item>
            <el-form-item label="组件路径" prop="component">
              <el-input v-model="permissionForm.component" placeholder="组件路径" clearable />
            </el-form-item>
          </div>
        </el-form>
      </div>
      <splitpanes v-else class="default-theme splitpanes-container">
        <pane size="22" min-size="15" class="left-pane">
          <div class="menu-pane-content">
            <el-menu
              default-active="1"
              class="el-menu-vertical-demo transparent-menu"
              @select="handleMenuSelect"
            >
              <el-menu-item index="1">
                <el-icon>
                  <Document />
                </el-icon>
                <span>基础信息</span>
              </el-menu-item>
              <el-menu-item index="2" :disabled="permissionForm.type != 1">
                <el-icon>
                  <Setting />
                </el-icon>
                <span>菜单配置</span>
              </el-menu-item>
            </el-menu>
          </div>
        </pane>
        <pane class="right-pane">
          <div class="form-pane-content">
            <el-scrollbar ref="formScrollbarRef">
              <el-form
                ref="permissionFormRef"
                :model="permissionForm"
                :rules="rules"
                label-width="100px"
                class="permission-form"
              >
                <div id="section-1" class="form-section">
                  <h3 class="section-title">基础信息</h3>
                  <el-form-item label="上级权限" prop="parentId">
                    <el-tree-select
                      v-model="permissionForm.parentId"
                      :data="permissionTreeData"
                      :props="{ label: 'name', value: 'id', children: 'children' }"
                      check-strictly
                      placeholder="请选择上级权限 (为空则为顶级)"
                      clearable
                      class="w-full"
                    />
                  </el-form-item>
                  <el-row :gutter="20">
                    <el-col :span="12">
                      <el-form-item label="权限名称" prop="name">
                        <el-input
                          v-model="permissionForm.name"
                          placeholder="请输入权限名称"
                          clearable
                        />
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="权限标识" prop="code">
                        <el-input
                          v-model="permissionForm.code"
                          placeholder="例如: user:add"
                          clearable
                        />
                      </el-form-item>
                    </el-col>
                  </el-row>
                  <el-row :gutter="20">
                    <el-col :span="12">
                      <el-form-item label="类型" prop="type">
                        <el-radio-group v-model="permissionForm.type">
                          <el-radio :value="1">菜单</el-radio>
                          <el-radio :value="2">按钮</el-radio>
                        </el-radio-group>
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="排序" prop="sortOrder">
                        <el-slider
                          v-model="permissionForm.sortOrder"
                          :min="0"
                          :max="20"
                          show-input
                          class="w-full"
                        />
                      </el-form-item>
                    </el-col>
                  </el-row>
                </div>

                <div id="section-2" class="form-section" v-if="permissionForm.type == 1">
                  <el-divider />
                  <h3 class="section-title">菜单配置</h3>
                  <el-row :gutter="20">
                    <el-col :span="12">
                      <el-form-item label="路由路径" prop="path">
                        <el-input
                          v-model="permissionForm.path"
                          placeholder="例如: /users"
                          clearable
                        />
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="图标" prop="icon">
                        <el-input
                          v-model="permissionForm.icon"
                          placeholder="例如: User"
                          clearable
                        />
                      </el-form-item>
                    </el-col>
                  </el-row>
                  <el-form-item label="组件路径" prop="component">
                    <el-input
                      v-model="permissionForm.component"
                      placeholder="例如: users/UserManagement.vue"
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
        <div class="flex-auto">
          <el-button :icon="Refresh" @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :icon="Check" @click="submitForm" :loading="submitting">
            确定
          </el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { useRoute } from 'vue-router'
import { ref, reactive, onMounted, computed, nextTick, watch, onUnmounted } from 'vue'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
import { storeToRefs } from 'pinia'
import { hasPermission } from '@/utils/permission'
import { PERMISSIONS } from '@/constants/permissionConstants'
import { ElMessage, ElMessageBox } from 'element-plus'

// 响应式布局
const isMobile = ref(window.innerWidth < 768)
const filterDrawerVisible = ref(false)
const handleResize = () => {
  isMobile.value = window.innerWidth < 768
}

const handleFilterConfirm = () => {
  filterDrawerVisible.value = false
  handleSearch()
}

// 扁平化权限列表，用于移动端卡片展示
const flattenedPermissions = computed(() => {
  const result = []
  const flatten = (items, level = 0) => {
    items.forEach((item) => {
      result.push({ ...item, level })
      if (item.children && item.children.length > 0) {
        flatten(item.children, level + 1)
      }
    })
  }
  flatten(permissionList.value)
  return result
})

import {
  Document,
  Setting,
  Plus,
  Edit,
  Delete,
  Search,
  Refresh,
  Check,
  Filter,
  Folder,
} from '@element-plus/icons-vue'
import { Splitpanes, Pane } from 'splitpanes'
import 'splitpanes/dist/splitpanes.css'
import {
  getPermissions,
  getPermissionTree,
  createPermission,
  updatePermission,
  deletePermission,
} from '@/api/permission'

// 权限列表加载状态
const loading = ref(false)
// 权限列表数据
const permissionList = ref([])
// 搜索表单
const searchForm = reactive({
  name: '',
  code: '',
  type: undefined,
})

// 搜索函数 (调用后端接口)
const handleSearch = () => {
  fetchPermissions()
}

// 重置搜索
const resetSearch = () => {
  searchForm.name = ''
  searchForm.code = ''
  searchForm.type = undefined
  handleSearch()
}

// 对话框显示状态
const dialogVisible = ref(false)
// 提交状态
const submitting = ref(false)
// 是否编辑模式
const isEdit = ref(false)
// 表单引用
const permissionFormRef = ref(null)
// 滚动条引用
const formScrollbarRef = ref(null)

// 菜单选择处理
const handleMenuSelect = (index) => {
  const sectionId = `section-${index}`
  const element = document.getElementById(sectionId)
  if (element) {
    element.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

// 表单数据模型
const permissionForm = ref({
  // 权限ID
  id: undefined,
  // 上级权限ID
  parentId: null,
  // 权限名称
  name: '',
  // 权限标识
  code: '',
  // 类型：menu 或 button
  type: 1,
  // 路由路径（菜单类型时必填）
  path: '',
  // 组件路径
  component: '',
  // 图标
  icon: '',
  // 排序
  sortOrder: 0,
})

// 表单验证规则
const rules = {
  // 权限名称必填
  name: [{ required: true, message: '请输入权限名称', trigger: 'blur' }],
  // 权限标识必填
  code: [{ required: true, message: '请输入权限标识', trigger: 'blur' }],
  // 类型必填
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
}

// 对话框标题根据操作类型动态切换
const dialogTitle = computed(() => (isEdit.value ? '编辑权限' : '新增权限'))
// 从 authStore 获取权限列表
const authStore = useAuthStore()
// 从 authStore 获取当前用户的所有权限
const { permissions: perms } = storeToRefs(authStore)
// 检查是否有添加权限的权限
const canAddPermission = computed(
  () =>
    hasPermission(perms.value, PERMISSIONS.PERMISSION_ADD_LEGACY) ||
    hasPermission(perms.value, PERMISSIONS.PERMISSION_ADD),
)
// 检查是否有编辑权限的权限
const canEditPermission = computed(
  () =>
    hasPermission(perms.value, PERMISSIONS.PERMISSION_EDIT_LEGACY) ||
    hasPermission(perms.value, PERMISSIONS.PERMISSION_EDIT),
)
// 检查是否有删除权限的权限
const canDeletePermission = computed(
  () =>
    hasPermission(perms.value, PERMISSIONS.PERMISSION_DELETE_LEGACY) ||
    hasPermission(perms.value, PERMISSIONS.PERMISSION_DELETE),
)
const canQueryPermission = computed(
  () =>
    hasPermission(perms.value, PERMISSIONS.PERMISSION_QUERY) ||
    hasPermission(perms.value, PERMISSIONS.PERMISSION_QUERY_LEGACY),
)
const canAddChild = computed(
  () =>
    hasPermission(perms.value, PERMISSIONS.PERMISSION_ADD_CHILD) ||
    hasPermission(perms.value, PERMISSIONS.PERMISSION_ADD_CHILD_LEGACY) ||
    canAddPermission.value, // Fallback to add permission if addchild is missing but add exists
)

// 用于树形选择器的数据 (包含一个根节点选项，或者直接使用 permissionList)
const permissionTreeData = computed(() => {
  // 可以添加一个虚拟根节点，或者直接用列表
  return permissionList.value
})

// 监听菜单类型变化，如果是按钮类型则清除菜单相关配置
watch(
  () => permissionForm.value.type,
  (newVal) => {
    if (newVal === 2) {
      permissionForm.value.path = ''
      permissionForm.value.component = ''
      permissionForm.value.icon = ''
    }
  },
)

const fetchPermissions = async () => {
  // 显示加载状态
  loading.value = true
  try {
    // 构造查询参数
    const params = {}
    if (searchForm.name) params.name = searchForm.name
    if (searchForm.code) params.code = searchForm.code
    if (searchForm.type) params.type = searchForm.type

    // 根据用户指示，使用 GET /api/v1/permissions 并支持过滤

    let res
    const hasSearch = Object.keys(params).length > 0
    if (hasSearch) {
      res = await getPermissions(params)
    } else {
      // 无搜索条件时，优先获取树形结构以展示层级
      res = await getPermissionTree()
    }

    // 根据后端返回结构调整
    const list = Array.isArray(res) ? res : res.list || res.data || res.children || []
    permissionList.value = list
  } catch (error) {
    console.error('获取权限列表失败:', error)
    ElMessage.error('获取权限列表失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  if (!canAddPermission.value) {
    ElMessage.error('没有访问权限')
    return
  }
  isEdit.value = false
  permissionForm.value = {
    id: undefined,
    parentId: null,
    name: '',
    code: '',
    type: 1,
    path: '',
    component: '',
    icon: '',
    sortOrder: 0,
  }
  dialogVisible.value = true
}

const handleAddSub = (row) => {
  if (!canAddChild.value) {
    ElMessage.error('没有访问权限')
    return
  }
  isEdit.value = false
  permissionForm.value = {
    id: undefined,
    parentId: row.id,
    name: '',
    code: '',
    type: row.type === 1 ? 2 : 1, // 如果父级是菜单，默认子级是按钮，否则默认菜单(虽然按钮下一般不加菜单)
    path: '',
    component: '',
    icon: '',
    sortOrder: 0,
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  if (!canEditPermission.value) {
    ElMessage.error('没有访问权限')
    return
  }
  isEdit.value = true
  permissionForm.value = { ...row }
  dialogVisible.value = true
}

const handleDelete = (row) => {
  if (!canDeletePermission.value) {
    ElMessage.error('没有访问权限')
    return
  }
  ElMessageBox.confirm('确定要删除该权限吗？如果该权限有子权限，可能也会被删除。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      try {
        await deletePermission(row.id)
        ElMessage.success('删除成功')
        resetSearch()
        await authStore.getUserInfo()
      } catch (error) {
        console.error('删除权限失败:', error)
        ElMessage.error('删除失败')
      }
    })
    .catch(() => {})
}

const submitForm = async () => {
  if (!permissionFormRef.value) return
  await permissionFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        if (isEdit.value) {
          await updatePermission(permissionForm.value)
          ElMessage.success('更新成功')
        } else {
          await createPermission(permissionForm.value)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        // 编辑完成后重置搜索并刷新列表，同时更新全局权限状态以同步侧边栏
        resetSearch()
        await authStore.getUserInfo()
      } catch (error) {
        console.error('提交失败:', error)
        ElMessage.error('提交失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

const resetForm = () => {
  if (permissionFormRef.value) {
    permissionFormRef.value.resetFields()
  }
  permissionForm.value = {
    id: undefined,
    parentId: null,
    name: '',
    code: '',
    type: 1,
    path: '',
    component: '',
    icon: '',
    sortOrder: 0,
  }
}

onMounted(() => {
  fetchPermissions()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.card-header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

/* 
 * 布局样式已提取到 @/assets/styles/management-layout.css 
 */

.search-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

/* 移动端搜索栏 */
.mobile-search-bar {
  display: flex;
  gap: 10px;
}

.mobile-main-search {
  flex: 1;
}

/* 覆盖 el-drawer 的 body padding 以便 splitpanes 充满 */
:deep(.el-drawer__body) {
  padding: 0;
  overflow: hidden;
}

:deep(.mobile-filter-drawer .el-drawer__body) {
  padding: 20px;
  overflow-y: auto;
}

.form-section {
  margin-bottom: 30px;
}

.w-200 {
  width: 200px;
}

.ml-10 {
  margin-left: 10px;
}

.menu-pane-content {
  padding: 10px;
  height: 100%;
}

.form-pane-content {
  padding: 20px;
  height: 100%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}

.permission-form {
  padding-right: 20px;
}

.permission-table {
  width: 100%;
  flex: 1;
}

.w-full {
  width: 100%;
}

.flex-auto {
  flex: auto;
}

.splitpanes-container {
  height: 100%;
  border: 1px solid var(--el-border-color-light);
}

.left-pane {
  background-color: transparent;
}

.transparent-menu {
  border-right: none;
  background-color: transparent;
}

.right-pane {
  overflow: hidden;
  background-color: transparent;
}

/* 移动端卡片列表样式已迁移至 @/assets/styles/mobile-card.css */

.name-section {
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: bold;
  font-size: 15px;
  color: #303133;
}

.indent-dot {
  width: 12px;
  height: 2px;
  background-color: #dcdfe6;
  margin-right: 4px;
}

.folder-icon {
  color: #409eff;
  font-size: 16px;
}

.mobile-form-container {
  padding: 16px;
  height: 100%;
  overflow-y: auto;
}

.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px;
}

@media (max-width: 768px) {
  .permission-management-container {
    padding: 0;
  }

  :deep(.el-card__header) {
    padding: 12px 16px;
  }

  .permission-form {
    padding-right: 0;
  }
}
</style>
