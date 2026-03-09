<template>
  <!--
    RoleManagement.vue - 角色管理页面
    
    功能：
    1. 角色列表展示 (ID, 名称, 描述, 权限数)
    2. 创建/编辑角色
    3. 分配权限 (使用 Tree 组件展示权限结构)
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
          <el-button v-if="canAddRole" type="primary" :icon="Plus" @click="handleAddRole">
            新增角色
          </el-button>
          <el-button
            v-if="canBatchDeleteRole"
            type="danger"
            :icon="Delete"
            :disabled="selectedRoleIds.length === 0"
            @click="handleBatchDelete"
          >
            批量删除
          </el-button>
          <el-input v-model="searchQuery.name" placeholder="角色名称" clearable class="w-200" />
          <el-input v-model="searchQuery.code" placeholder="角色代码" clearable class="w-200" />
          <el-button v-if="canQueryRole" type="primary" :icon="Search" @click="handleSearch">
            查询
          </el-button>
          <el-button v-if="canQueryRole" :icon="Refresh" @click="resetSearch"> 重置 </el-button>
        </div>

        <!-- 移动端搜索栏 -->
        <div v-else class="mobile-search-bar">
          <el-button v-if="canAddRole" type="primary" :icon="Plus" @click="handleAddRole">
            新增
          </el-button>
          <el-input
            v-model="searchQuery.name"
            placeholder="搜索角色名称"
            clearable
            class="mobile-main-search"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button v-if="canQueryRole" :icon="Filter" @click="filterDrawerVisible = true"
            >筛选</el-button
          >
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
        <el-form :model="searchQuery" label-position="top">
          <el-form-item label="角色名称">
            <el-input v-model="searchQuery.name" placeholder="请输入名称" clearable />
          </el-form-item>
          <el-form-item label="角色代码">
            <el-input v-model="searchQuery.code" placeholder="请输入代码" clearable />
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="drawer-footer">
            <el-button @click="resetSearch">重置</el-button>
            <el-button type="primary" @click="handleFilterConfirm">确定</el-button>
          </div>
        </template>
      </el-drawer>

      <!-- 角色数据表格 -->
      <el-table
        v-if="!isMobile"
        :data="roleList"
        class="role-table mt-20"
        height="100%"
        stripe
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" fixed="left" />
        <el-table-column prop="id" label="ID" width="80" fixed="left" />
        <el-table-column prop="name" label="角色名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="code" label="角色代码" min-width="150" show-overflow-tooltip />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="创建时间" min-width="180" show-overflow-tooltip />
        <el-table-column prop="updatedAt" label="更新时间" min-width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button
              v-if="canEditRole"
              type="primary"
              size="small"
              :icon="Edit"
              @click="handleEditRole(scope.row)"
              >编辑</el-button
            >
            <el-button
              v-if="canDeleteRole"
              type="danger"
              size="small"
              :icon="Delete"
              @click="handleDeleteRole(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 移动端卡片列表 -->
      <div v-else class="mobile-card-list" v-loading="loading">
        <el-empty v-if="roleList.length === 0" description="暂无角色数据" />
        <div v-for="row in roleList" :key="row.id" class="mobile-card-wrapper">
          <el-card class="mobile-card-item" shadow="hover">
            <div class="card-top">
              <span class="role-name">{{ row.name }}</span>
              <el-tag size="small" type="info">{{ row.code }}</el-tag>
            </div>
            <div class="card-content">
              <div class="content-item">
                <span class="label">ID:</span>
                <span class="value">{{ row.id }}</span>
              </div>
              <div class="content-item">
                <span class="label">描述:</span>
                <span class="value">{{ row.description || '无描述' }}</span>
              </div>
              <div class="content-item">
                <span class="label">创建时间:</span>
                <span class="value">{{ row.createdAt }}</span>
              </div>
              <div class="content-item">
                <span class="label">更新时间:</span>
                <span class="value">{{ row.updatedAt }}</span>
              </div>
            </div>
            <div class="card-actions">
              <el-button
                v-if="canEditRole"
                type="primary"
                size="small"
                :icon="Edit"
                @click="handleEditRole(row)"
              >
                编辑
              </el-button>
              <el-button
                v-if="canDeleteRole"
                type="danger"
                size="small"
                :icon="Delete"
                @click="handleDeleteRole(row)"
              >
                删除
              </el-button>
            </div>
          </el-card>
        </div>
      </div>

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
          :layout="'total, sizes, prev, pager, next, jumper'"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑角色抽屉 -->
    <el-drawer
      v-model="roleDialogVisible"
      :title="roleForm.id ? '编辑角色' : '新增角色'"
      :size="isMobile ? '100%' : '900px'"
      :close-on-click-modal="false"
      destroy-on-close
      class="role-drawer"
    >
      <div v-if="isMobile" class="mobile-form-container">
        <el-form
          :model="roleForm"
          :rules="roleRules"
          ref="roleFormRef"
          label-position="top"
          class="role-form"
        >
          <div class="form-section">
            <h3 class="section-title">基本信息</h3>
            <el-form-item label="角色名称" prop="roleName">
              <el-input v-model="roleForm.roleName" placeholder="请输入角色名称" clearable />
            </el-form-item>
            <el-form-item label="角色代码" prop="roleCode">
              <el-input v-model="roleForm.roleCode" placeholder="请输入角色代码" clearable />
            </el-form-item>
            <el-form-item label="描述" prop="description">
              <el-input
                v-model="roleForm.description"
                type="textarea"
                placeholder="请输入描述"
                clearable
              />
            </el-form-item>
          </div>

          <div class="form-section">
            <el-divider />
            <h3 class="section-title">权限设置</h3>
            <div class="permission-tree-container">
              <el-tree
                ref="permissionTreeRef"
                :data="permissionTreeData"
                show-checkbox
                node-key="id"
                :default-expand-all="true"
                :props="defaultProps"
              >
                <template #default="{ node, data }">
                  <span class="custom-tree-node">
                    <span>{{ node.label }}</span>
                    <el-tag
                      v-if="data.type"
                      size="small"
                      :type="data.type === 'menu' || data.type == 1 ? 'success' : 'info'"
                      class="ml-8"
                    >
                      {{ data.type === 'menu' || data.type == 1 ? '菜单' : '按钮' }}
                    </el-tag>
                  </span>
                </template>
              </el-tree>
            </div>
          </div>
        </el-form>
      </div>
      <splitpanes v-else class="default-theme role-drawer-splitpanes">
        <pane size="22" min-size="15" class="role-menu-pane">
          <div class="role-menu-wrapper">
            <el-menu
              default-active="1"
              class="el-menu-vertical-demo role-menu"
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
                  <Setting />
                </el-icon>
                <span>权限设置</span>
              </el-menu-item>
            </el-menu>
          </div>
        </pane>
        <pane class="role-content-pane">
          <div class="role-form-container">
            <el-scrollbar ref="formScrollbarRef">
              <el-form
                :model="roleForm"
                :rules="roleRules"
                ref="roleFormRef"
                label-width="100px"
                class="role-form"
              >
                <div id="section-1" class="form-section">
                  <h3 class="section-title">基本信息</h3>
                  <el-form-item label="角色名称" prop="roleName">
                    <el-input v-model="roleForm.roleName" placeholder="请输入角色名称" clearable />
                  </el-form-item>
                  <el-form-item label="角色代码" prop="roleCode">
                    <el-input
                      v-model="roleForm.roleCode"
                      placeholder="请输入角色代码 (如 admin)"
                      clearable
                    />
                  </el-form-item>
                  <el-form-item label="描述" prop="description">
                    <el-input
                      v-model="roleForm.description"
                      type="textarea"
                      placeholder="请输入描述"
                      clearable
                    />
                  </el-form-item>
                </div>

                <div id="section-2" class="form-section">
                  <el-divider />
                  <h3 class="section-title">权限设置</h3>
                  <div class="permission-tree-container">
                    <el-tree
                      ref="permissionTreeRef"
                      :data="permissionTreeData"
                      show-checkbox
                      node-key="id"
                      :default-expand-all="true"
                      :props="defaultProps"
                    >
                      <template #default="{ node, data }">
                        <span class="custom-tree-node">
                          <span>{{ node.label }}</span>
                          <el-tag
                            v-if="data.type"
                            size="small"
                            :type="data.type === 'menu' || data.type == 1 ? 'success' : 'info'"
                            class="ml-8"
                          >
                            {{ data.type === 'menu' || data.type == 1 ? '菜单' : '按钮' }}
                          </el-tag>
                        </span>
                      </template>
                    </el-tree>
                  </div>
                </div>
              </el-form>
            </el-scrollbar>
          </div>
        </pane>
      </splitpanes>
      <template #footer>
        <div class="drawer-footer">
          <el-button @click="roleDialogVisible = false">取消</el-button>
          <el-button type="primary" :icon="Check" @click="handleSaveRole" :loading="submitting"
            >保存</el-button
          >
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, computed, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { usePermissionStore } from '@/stores/permission'

import { useAuthStore } from '@/stores/auth'
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

const resetSearch = () => {
  searchQuery.name = ''
  searchQuery.code = ''
  handleSearch()
}

import {
  Plus,
  User,
  Setting,
  Delete,
  Edit,
  Check,
  Search,
  Refresh,
  Filter,
} from '@element-plus/icons-vue'
import { Splitpanes, Pane } from 'splitpanes'
import 'splitpanes/dist/splitpanes.css'
import {
  getRoles,
  createRole,
  updateRole,
  deleteRole,
  deleteRoles,
  assignPermissions,
  getRoleDetail,
} from '@/api/role'
import { getPermissionTree } from '@/api/permission'

const permissionStore = usePermissionStore()
const authStore = useAuthStore()
const { permissions: perms } = storeToRefs(authStore)
const router = useRouter()
const route = useRoute()

const canAddRole = computed(
  () =>
    hasPermission(perms.value, PERMISSIONS.ROLE_ADD) ||
    hasPermission(perms.value, PERMISSIONS.ROLE_ADD_LEGACY),
)
const canEditRole = computed(
  () =>
    hasPermission(perms.value, PERMISSIONS.ROLE_EDIT) ||
    hasPermission(perms.value, PERMISSIONS.ROLE_UPDATE),
)
const canDeleteRole = computed(
  () =>
    hasPermission(perms.value, PERMISSIONS.ROLE_DELETE) ||
    hasPermission(perms.value, PERMISSIONS.ROLE_DELETE_LEGACY),
)
const canQueryRole = computed(
  () =>
    hasPermission(perms.value, PERMISSIONS.ROLE_QUERY) ||
    hasPermission(perms.value, PERMISSIONS.ROLE_QUERY_LEGACY),
)
const canBatchDeleteRole = computed(
  () =>
    hasPermission(perms.value, PERMISSIONS.ROLE_BATCH_DELETE) ||
    hasPermission(perms.value, PERMISSIONS.ROLE_BATCH_DELETE_LEGACY),
)

// 角色列表加载状态
const loading = ref(false)
// 新增/编辑角色提交状态
const submitting = ref(false)
// 角色列表
const roleList = ref([])
// 选中角色ID
const selectedRoleIds = ref([])
// 新增/编辑角色对话框可见状态
const roleDialogVisible = ref(false)
// 新增/编辑角色表单引用
const roleFormRef = ref(null)
const formScrollbarRef = ref(null)

const handleSelectionChange = (selection) => {
  selectedRoleIds.value = selection.map((item) => item.id)
}

const handleBatchDelete = () => {
  if (selectedRoleIds.value.length === 0) return
  ElMessageBox.confirm('确认要删除选中的角色吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      try {
        await deleteRoles(selectedRoleIds.value)
        ElMessage.success('批量删除成功')
        handleSearch()
        selectedRoleIds.value = []
      } catch (error) {
        console.error('批量删除失败', error)
        ElMessage.error('批量删除失败')
      }
    })
    .catch(() => {})
}

// 菜单选择处理
const handleMenuSelect = (index) => {
  const sectionId = `section-${index}`
  const element = document.getElementById(sectionId)
  if (element) {
    element.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

// 分页和搜索状态
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0,
})
// 搜索查询
const searchQuery = reactive({
  name: '',
  code: '',
})

// 角色表单数据
const roleForm = reactive({
  id: null,
  roleName: '',
  roleCode: '',
  description: '',
})

const roleRules = {
  // 角色名称required是
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  // 角色代码
  roleCode: [{ required: true, message: '请输入角色代码', trigger: 'blur' }],
}

// 权限相关
const permissionDialogVisible = ref(false)
const permissionTreeRef = ref(null)
const currentRoleId = ref(null)

// 权限树数据
const permissionTreeData = ref([])

const defaultProps = {
  children: 'children',
  label: 'name',
}

// 获取权限树数据
const fetchPermissionTree = async () => {
  try {
    // 直接获取完整权限树
    const res = await getPermissionTree()
    // 处理后端返回的权限树数据结构
    const rawTree = Array.isArray(res) ? res : res.list || res || []
    // 直接展示后端返回的完整权限树，不做前端过滤
    permissionTreeData.value = rawTree

    // 之前这里过滤了权限树，导致只能看到自己拥有的权限。现在注释掉，允许看到所有权限。
    // permissionTreeData.value = filterPermissionTree(rawTree, perms.value)
  } catch (error) {
    console.error('获取权限失败', error)
    permissionTreeData.value = []
  }
}

// 递归过滤权限树是为了根据用户权限筛选出用户实际有权限访问的菜单和按钮，是后端的吗？
// 答案是：后端返回的权限树是完整的，前端需要根据用户权限对其进行过滤。
// 不是后端的，是前端的，因为后端返回的权限树是完整的，前端需要根据用户权限筛选出用户实际有权限访问的菜单和按钮
// 不要这个会有什么问题？
// 问题是：如果不进行前端过滤，用户可能会看到所有菜单和按钮，包括他们没有权限访问的那些。
// 这会导致用户体验下降，因为他们会看到一些他们实际上没有权限使用的功能。
// 因此，前端需要根据用户权限对权限树进行过滤，只显示用户实际有权限访问的菜单和按钮。
const filterPermissionTree = (nodes, userPerms) => {
  if (!nodes || !nodes.length) return []

  // 如果是超级管理员，显示所有
  if (hasPermission(userPerms, PERMISSIONS.ADMIN_WILDCARD)) {
    return nodes
  }

  const result = []
  for (const node of nodes) {
    // 1. 递归处理子节点
    //
    const filteredChildren = filterPermissionTree(node.children, userPerms)

    // 2. 检查当前节点是否有权限
    // 兼容处理：有些节点可能没有 code (如目录)，或者 code 属性名不同
    const nodeCode = node.code || node.permission
    const hasNodePerm = nodeCode && hasPermission(userPerms, nodeCode)

    if (hasNodePerm || filteredChildren.length > 0) {
      result.push({
        ...node,
        children: filteredChildren.length > 0 ? filteredChildren : node.children, // 如果没有过滤后的子节点，但自身有权限，可能需要保留原children(如果原children本来就是空)或者置空？
      })
      // 修正 children 赋值
      result[result.length - 1].children = filteredChildren
    }
  }
  return result
}

// 获取角色列表
const fetchRoleList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.currentPage,
      size: pagination.pageSize,
      name: searchQuery.name || undefined,
      code: searchQuery.code || undefined,
    }
    const res = await getRoles(params)
    // 处理列表数据
    const rawList = Array.isArray(res) ? res : res.list || res.data || []
    // 处理总数
    pagination.total = typeof res.total === 'number' ? res.total : rawList.length

    roleList.value = rawList.map((item) => ({
      ...item,
      // 优先使用标准字段，兼容旧字段
      name: item.name || item.roleName,
      code: item.code || item.roleCode,
    }))
  } catch (error) {
    console.error('获取角色列表失败', error)
    ElMessage.error('获取角色列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索角色
const handleSearch = () => {
  pagination.currentPage = 1
  fetchRoleList()
}

// 分页大小改变
const handleSizeChange = (val) => {
  pagination.pageSize = val
  fetchRoleList()
}
// 分页当前页改变
const handleCurrentChange = (val) => {
  pagination.currentPage = val
  fetchRoleList()
}
// 初始化加载角色列表和权限树
onMounted(() => {
  fetchRoleList()
  fetchPermissionTree()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})

// 新增角色
const handleAddRole = () => {
  resetRoleForm()
  roleDialogVisible.value = true
  nextTick(() => {
    if (permissionTreeRef.value) {
      permissionTreeRef.value.setCheckedKeys([])
    }
  })
}

// 编辑角色
const handleEditRole = async (row) => {
  try {
    const res = await getRoleDetail(row.id)
    roleForm.id = res.id
    roleForm.roleName = res.roleName
    roleForm.roleCode = res.roleCode
    roleForm.description = res.description

    // 处理权限回显
    const permissions = res.permissions || res.permissionIds || []
    const allCheckedIds = permissions.map((p) => (typeof p === 'object' ? p.id : p))
    const allLeafIds = getAllLeafIds(permissionTreeData.value)
    const leafCheckedKeys = allCheckedIds.filter((id) => allLeafIds.includes(id))

    roleDialogVisible.value = true

    nextTick(() => {
      if (permissionTreeRef.value) {
        permissionTreeRef.value.setCheckedKeys(leafCheckedKeys, false)
      }
    })
  } catch (error) {
    console.error('获取角色详情失败', error)
    ElMessage.error('获取角色详情失败')
  }
}

const resetRoleForm = () => {
  roleForm.id = null
  roleForm.roleName = ''
  roleForm.roleCode = ''
  roleForm.description = ''
  if (roleFormRef.value) {
    roleFormRef.value.resetFields()
  }
}

// 保存角色 (包含基本信息和权限)
const handleSaveRole = () => {
  roleFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const payload = { ...roleForm }
        let savedRoleId = roleForm.id

        if (roleForm.id) {
          await updateRole(roleForm.id, payload)
          ElMessage.success('角色信息更新成功')
        } else {
          const res = await createRole(payload)
          // 兼容返回结构
          savedRoleId = res.id || res
          ElMessage.success('角色创建成功')
        }

        // 保存权限
        if (savedRoleId && permissionTreeRef.value) {
          const checkedKeys = permissionTreeRef.value.getCheckedKeys()
          const halfCheckedKeys = permissionTreeRef.value.getHalfCheckedKeys()
          const allKeys = [...checkedKeys, ...halfCheckedKeys]

          await assignPermissions(savedRoleId, allKeys)
        }

        roleDialogVisible.value = false
        fetchRoleList()

        // 刷新当前用户的权限和路由
        await authStore.getUserInfo()
        await permissionStore.fetchMenus()
        const routes = await permissionStore.generateRoutes()
        routes.forEach((route) => {
          router.addRoute('Root', route)
        })
      } catch (error) {
        console.error('保存失败', error)
        ElMessage.error('保存失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

// 删除角色
const handleDeleteRole = (row) => {
  ElMessageBox.confirm('确定要删除该角色吗？', '警告', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消',
  }).then(async () => {
    try {
      await deleteRole(row.id)
      ElMessage.success('删除成功')
      // 如果当前页只有一条数据且不是第一页，则跳转到上一页
      if (roleList.value.length === 1 && pagination.currentPage > 1) {
        pagination.currentPage--
      }
      fetchRoleList()
    } catch (error) {
      console.error('删除失败', error)
      ElMessage.error('删除失败')
    }
  })
}

// 辅助函数：获取所有叶子节点的 ID
const getAllLeafIds = (nodes, leafIds = []) => {
  nodes.forEach((node) => {
    if (node.children && node.children.length > 0) {
      getAllLeafIds(node.children, leafIds)
    } else {
      leafIds.push(node.id)
    }
  })
  return leafIds
}
</script>

<style scoped>
.card-header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

/* 布局样式已提取到全局 */

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

.pagination {
  padding: 10px 0;
  display: flex;
  justify-content: flex-end;
  flex-shrink: 0;
}

.mobile-pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  gap: 5px;
}

.mobile-pagination :deep(.el-pagination) {
  padding: 0;
}

.permission-tree-container {
  max-height: 400px;
  overflow-y: auto;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  padding: 10px;
}

.dialog-tip {
  margin-bottom: 10px;
  color: var(--el-text-color-regular);
}

.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  font-size: 14px;
}

/* 样式重构 */
.w-200 {
  width: 200px;
}

.w-full {
  width: 100%;
}

.mr-10 {
  margin-right: 10px;
}

.mt-20 {
  margin-top: 20px;
}

.ml-8 {
  margin-left: 8px;
}

.flex-auto {
  flex: auto;
}

.role-drawer-splitpanes {
  height: 100%;
  border: 1px solid var(--el-border-color-light);
}

.role-menu-pane {
  background-color: transparent;
}

.role-menu-wrapper {
  padding: 10px;
  height: 100%;
}

.role-menu {
  border-right: none;
  background-color: transparent;
}

.role-content-pane {
  overflow: hidden;
  background-color: transparent;
}

.role-form-container {
  padding: 20px;
  height: 100%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}

.role-form {
  padding-right: 20px;
}

.role-table {
  width: 100%;
  flex: 1;
}

/* 移动端卡片列表样式已迁移至 @/assets/styles/mobile-card.css */

.role-name {
  font-weight: bold;
  font-size: 15px;
  color: #303133;
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

.pagination {
  padding: 10px 0;
  display: flex;
  justify-content: flex-end;
}

.mobile-pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  gap: 5px;
}

.mobile-pagination :deep(.el-pagination) {
  padding: 0;
}

:deep(.mobile-filter-drawer .el-drawer__body) {
  padding: 20px;
}

@media (max-width: 768px) {
  .role-management-container {
    padding: 0;
  }

  :deep(.el-card__header) {
    padding: 12px 16px;
  }
}
</style>
