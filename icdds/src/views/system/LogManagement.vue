<template>
  <div class="management-container">
    <el-card class="management-card">
      <template #header>
        <div class="card-header-content">
          <span>{{ route.meta.title }}</span>
        </div>
      </template>

      <!-- 搜索栏 -->
      <div class="search-bar-container">
        <!-- PC端搜索栏 -->
        <div v-if="!isMobile" class="search-bar">
          <!-- 批量删除按钮 - 需要删除日志权限 -->
          <el-button
            v-if="canDeleteLog"
            type="danger"
            :disabled="selectedIds.length === 0"
            @click="handleDelete"
            :icon="Delete"
          >
            <span>批量删除</span>
          </el-button>
          <!-- 清空日志按钮 - 需要清空日志权限 -->
          <el-button v-if="canCleanLog" type="danger" plain @click="handleClean" :icon="Brush">
            <span>清空日志</span>
          </el-button>
          <el-input v-model="searchForm.module" placeholder="模块名称" clearable class="w-180" />
          <el-input v-model="searchForm.operator" placeholder="操作人" clearable class="w-180" />
          <el-select v-model="searchForm.status" placeholder="状态" clearable class="w-120">
            <el-option label="成功" value="success" />
            <el-option label="失败" value="fail" />
          </el-select>
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            class="w-260"
          />
          <!-- 搜索按钮 - 需要查询日志权限 -->
          <el-button v-if="canQueryLog" type="primary" :icon="Search" @click="handleSearch"
            >搜索</el-button
          >
          <!-- 重置按钮 - 需要重置日志权限 -->
          <el-button v-if="canResetLog" :icon="Refresh" @click="resetSearch">重置</el-button>
        </div>

        <!-- 移动端搜索栏 -->
        <div v-else class="mobile-search-bar">
          <!-- 清空日志按钮 - 需要清空日志权限 -->
          <el-button v-if="canCleanLog" type="danger" :icon="Brush" @click="handleClean">
            清空日志
          </el-button>
          <el-input
            v-model="searchForm.module"
            placeholder="按模块名称搜索"
            clearable
            class="mobile-main-search"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <!-- 筛选按钮 - 需要查询日志权限 -->
          <el-button v-if="canQueryLog" :icon="Filter" @click="filterDrawerVisible = true"
            >筛选</el-button
          >
        </div>
      </div>

      <!-- 日志列表 (PC端表格) -->
      <el-table
        v-if="!isMobile"
        :data="tableData"
        v-loading="loading"
        stripe
        class="log-table"
        height="100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="module" label="功能模块" width="150">
          <template #default="{ row }">
            <el-tag>{{ row.module }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="操作类型" width="150" />
        <el-table-column prop="description" label="操作内容" show-overflow-tooltip />
        <el-table-column prop="operator" label="操作人" width="120" />
        <el-table-column prop="ip" label="IP地址" width="140" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'success' ? 'success' : 'danger'">
              {{ row.status === 'success' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="errorMsg" label="错误详情" show-overflow-tooltip />
        <el-table-column prop="createTime" label="操作时间" width="180" />
        <el-table-column v-if="canDeleteLog" label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <!-- 删除按钮 - 需要删除日志权限 -->
            <el-button type="danger" size="small" :icon="Delete" @click="handleSingleDelete(row)"
              >删除</el-button
            >
          </template>
        </el-table-column>
      </el-table>

      <!-- 日志列表 (移动端卡片) -->
      <div v-else class="mobile-card-list" v-loading="loading">
        <el-empty v-if="tableData.length === 0" description="暂无日志数据" />
        <div v-for="row in tableData" :key="row.id" class="mobile-card-wrapper">
          <el-card class="mobile-card-item" shadow="hover">
            <div class="card-top">
              <el-tag size="small">{{ row.module }}</el-tag>
              <el-tag :type="row.status === 'success' ? 'success' : 'danger'" size="small">
                {{ row.status === 'success' ? '成功' : '失败' }}
              </el-tag>
            </div>
            <div class="card-content">
              <div class="content-item">
                <span class="label">ID:</span>
                <span class="value">{{ row.id }}</span>
              </div>
              <div class="content-item">
                <span class="label">操作类型:</span>
                <span class="value">{{ row.type }}</span>
              </div>
              <div class="content-item">
                <span class="label">操作内容:</span>
                <span class="value">{{ row.description }}</span>
              </div>
              <div class="content-item">
                <span class="label">操作人:</span>
                <span class="value">{{ row.operator }}</span>
              </div>
              <div class="content-item">
                <span class="label">IP地址:</span>
                <span class="value">{{ row.ip }}</span>
              </div>
              <div class="content-item">
                <span class="label">错误详情:</span>
                <span class="value">{{ row.errorMsg || '-' }}</span>
              </div>
              <div class="content-item">
                <span class="label">时间:</span>
                <span class="value">{{ row.createTime }}</span>
              </div>
            </div>
            <div v-if="canDeleteLog" class="card-actions">
              <!-- 删除日志按钮 - 需要删除日志权限 -->
              <el-button
                type="danger"
                size="small"
                :icon="Delete"
                plain
                @click="handleSingleDelete(row)"
              >
                删除日志
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
          <el-form-item label="操作人">
            <el-input v-model="searchForm.operator" placeholder="操作人姓名" clearable />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="全部状态" clearable class="w-full">
              <el-option label="成功" value="success" />
              <el-option label="失败" value="fail" />
            </el-select>
          </el-form-item>
          <el-form-item label="时间范围">
            <el-date-picker
              v-model="searchForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始"
              end-placeholder="结束"
              value-format="YYYY-MM-DD"
              class="w-full"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="drawer-footer">
            <!-- 重置按钮 - 需要重置日志权限 -->
            <el-button v-if="canResetLog" @click="resetSearch">重置</el-button>
            <!-- 确定按钮 - 需要查询日志权限 -->
            <el-button v-if="canQueryLog" type="primary" @click="handleFilterConfirm"
              >确定</el-button
            >
          </div>
        </template>
      </el-drawer>

      <!-- 分页 -->
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
            small
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
</template>

<script setup>
import { useRoute } from 'vue-router'
import { ref, reactive, onMounted, computed } from 'vue'

const route = useRoute()
import { Delete, Search, Refresh, Brush, Filter } from '@element-plus/icons-vue'

import { getSystemLogs, deleteSystemLogs, cleanSystemLogs } from '@/api/system'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { storeToRefs } from 'pinia'
import { hasPermission } from '@/utils/permission'
import { PERMISSIONS } from '@/constants/permissionConstants'

const authStore = useAuthStore()
const { permissions: perms } = storeToRefs(authStore)

const canQueryLog = computed(() => hasPermission(perms.value, PERMISSIONS.LOG_QUERY))
const canResetLog = computed(() => hasPermission(perms.value, PERMISSIONS.LOG_RESET))
const canDeleteLog = computed(() => hasPermission(perms.value, PERMISSIONS.LOG_DELETE))
const canCleanLog = computed(() => hasPermission(perms.value, PERMISSIONS.LOG_CLEAN))

const loading = ref(false)
const selectedIds = ref([])
const searchForm = reactive({
  module: '',
  operator: '',
  status: '',
  dateRange: [],
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0,
})

const tableData = ref([])

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

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.currentPage,
      size: pagination.pageSize,
      module: searchForm.module || undefined,
      operator: searchForm.operator || undefined,
      status: searchForm.status || undefined,
      startTime: searchForm.dateRange?.[0],
      endTime: searchForm.dateRange?.[1],
    }
    const res = await getSystemLogs(params)
    // 后端返回的是 PageInfo 对象
    tableData.value = res.list || []
    pagination.total = res.total || 0
  } catch (error) {
    console.error('获取日志失败', error)
    ElMessage.error('获取日志列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.currentPage = 1
  fetchData()
}

const resetSearch = () => {
  searchForm.module = ''
  searchForm.operator = ''
  searchForm.status = ''
  searchForm.dateRange = []
  pagination.currentPage = 1
  filterDrawerVisible.value = false
  fetchData()
}

const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map((item) => item.id)
}

const handleSingleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确认删除 ID 为 ${row.id} 的日志吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteSystemLogs([row.id])
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败', error)
      ElMessage.error('删除失败')
    }
  }
}

const handleDelete = async () => {
  if (selectedIds.value.length === 0) return
  try {
    await ElMessageBox.confirm('确认删除选中的日志吗？此操作不可恢复', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteSystemLogs(selectedIds.value)
    ElMessage.success('删除成功')
    // 如果当前页数据都被删除了且不是第一页，则跳转到上一页
    if (tableData.value.length === selectedIds.value.length && pagination.currentPage > 1) {
      pagination.currentPage--
    }
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败', error)
      ElMessage.error('删除失败')
    }
  }
}

const handleClean = async () => {
  try {
    await ElMessageBox.confirm('确认清空所有日志吗？此操作不可恢复', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await cleanSystemLogs()
    ElMessage.success('清空成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('清空失败', error)
      ElMessage.error('清空失败')
    }
  }
}

const handleSizeChange = (val) => {
  pagination.pageSize = val
  fetchData()
}

const handleCurrentChange = (val) => {
  pagination.currentPage = val
  fetchData()
}

onMounted(() => {
  fetchData()
  window.addEventListener('resize', handleResize)
})

import { onUnmounted } from 'vue'
onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
/* 布局样式已提取到 @/assets/styles/management-layout.css */
/* 移动端卡片样式已提取到 @/assets/styles/mobile-card.css */

.ml-10 {
  margin-left: 0; /* 在 flex 容器中使用 gap 代替 ml-10 */
}
</style>
