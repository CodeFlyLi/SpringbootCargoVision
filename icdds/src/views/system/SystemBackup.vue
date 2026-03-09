<template>
  <div class="management-container">
    <el-card class="management-card">
      <template #header>
        <div class="card-header-content">
          <span>{{ route.meta.title }}</span>
        </div>
      </template>

      <div class="search-bar-container">
        <!-- PC 操作栏 -->
        <div v-if="!isMobile" class="search-bar">
          <!-- 新建备份按钮 - 需要新建备份权限 -->
          <el-button v-if="canAddBackup" type="primary" @click="handleCreateBackup" :icon="Plus">
            <span>新建备份</span>
          </el-button>
          <el-input
            v-model="searchName"
            placeholder="备份文件名"
            clearable
            class="w-200"
            @input="handleSearch"
          />
          <!-- 查询按钮 - 需要查询备份权限 -->
          <el-button v-if="canQueryBackup" type="primary" @click="fetchData" :icon="Search">
            <span>查询</span>
          </el-button>
        </div>

        <!-- 移动端操作栏 -->
        <div v-else class="mobile-search-bar">
          <!-- 新建按钮 - 需要新建备份权限 -->
          <el-button v-if="canAddBackup" type="primary" @click="handleCreateBackup" :icon="Plus">
            <span>新建</span>
          </el-button>
          <el-input
            v-model="searchName"
            placeholder="搜索文件名"
            clearable
            class="mobile-main-search"
            @input="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
      </div>

      <el-table
        v-if="!isMobile"
        :data="tableData"
        v-loading="loading"
        stripe
        class="backup-table"
        height="100%"
      >
        <el-table-column prop="name" label="备份文件名" min-width="200" />
        <el-table-column prop="size" label="文件大小" width="120" />
        <el-table-column prop="version" label="系统版本" width="120" />
        <el-table-column prop="createTime" label="备份时间" width="180" />
        <el-table-column prop="operator" label="操作人" width="120" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <!-- 下载按钮 - 需要下载备份权限 -->
            <el-button
              v-if="canDownloadBackup"
              size="small"
              type="success"
              :icon="Download"
              @click="handleDownload(row)"
              >下载</el-button
            >
            <!-- 恢复按钮 - 需要恢复备份权限 -->
            <el-button
              v-if="canRestoreBackup"
              size="small"
              type="warning"
              :icon="RefreshLeft"
              @click="handleRestore(row)"
              >恢复</el-button
            >
            <!-- 删除按钮 - 需要删除备份权限 -->
            <el-button
              v-if="canDeleteBackup"
              size="small"
              type="danger"
              :icon="Delete"
              @click="handleDelete(row)"
              >删除</el-button
            >
          </template>
        </el-table-column>
      </el-table>

      <!-- 移动端卡片列表 -->
      <div v-else class="mobile-card-list" v-loading="loading">
        <el-empty v-if="tableData.length === 0" description="暂无备份记录" />
        <div v-for="row in tableData" :key="row.id" class="mobile-card-wrapper">
          <el-card class="mobile-card-item" shadow="hover">
            <div class="card-top">
              <span class="backup-name">{{ row.name }}</span>
              <el-tag size="small" type="info">{{ row.version }}</el-tag>
            </div>
            <div class="card-content">
              <div class="content-item">
                <span class="label">文件大小:</span>
                <span class="value">{{ row.size }}</span>
              </div>
              <div class="content-item">
                <span class="label">备份时间:</span>
                <span class="value">{{ row.createTime }}</span>
              </div>
              <div class="content-item">
                <span class="label">操作人:</span>
                <span class="value">{{ row.operator }}</span>
              </div>
            </div>
            <div class="card-actions">
              <!-- 下载按钮 - 需要下载备份权限 -->
              <el-button
                v-if="canDownloadBackup"
                size="small"
                type="success"
                :icon="Download"
                @click="handleDownload(row)"
                >下载</el-button
              >
              <!-- 恢复按钮 - 需要恢复备份权限 -->
              <el-button
                v-if="canRestoreBackup"
                size="small"
                type="warning"
                :icon="RefreshLeft"
                @click="handleRestore(row)"
                >恢复</el-button
              >
              <!-- 删除按钮 - 需要删除备份权限 -->
              <el-button
                v-if="canDeleteBackup"
                size="small"
                type="danger"
                :icon="Delete"
                @click="handleDelete(row)"
                >删除</el-button
              >
            </div>
          </el-card>
        </div>
      </div>

      <!-- 分页区域 -->
      <div class="pagination">
        <div v-if="isMobile" class="mobile-pagination">
          <el-button size="small" :disabled="page === 1" @click="handleHome"> 首页 </el-button>
          <el-pagination
            v-model:current-page="page"
            v-model:page-size="size"
            :total="total"
            layout="prev, pager, next"
            :small="true"
            @current-change="handleCurrentChange"
          />
          <el-button size="small" :disabled="page >= Math.ceil(total / size)" @click="handleEnd">
            末页
          </el-button>
        </div>
        <el-pagination
          v-else
          v-model:current-page="page"
          v-model:page-size="size"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 恢复确认抽屉 -->
    <el-drawer
      v-model="restoreDialogVisible"
      title="确认恢复"
      :size="isMobile ? '100%' : '400px'"
      direction="rtl"
    >
      <div class="text-center py-10">
        <el-icon class="text-warning text-6xl mb-4"><WarningFilled /></el-icon>
        <p class="text-xl font-bold">确定要恢复此备份吗？</p>
        <p class="text-gray-500 mt-4 px-6">
          恢复操作将覆盖当前系统数据，且不可撤销！建议在操作前确保已备份当前重要数据。
        </p>
      </div>
      <template #footer>
        <div class="drawer-footer">
          <!-- 取消按钮 -->
          <el-button :icon="Refresh" @click="restoreDialogVisible = false">取消</el-button>
          <!-- 确认恢复按钮 - 需要恢复备份权限 -->
          <el-button
            v-if="canRestoreBackup"
            type="primary"
            :icon="Check"
            :loading="restoring"
            @click="confirmRestore"
          >
            确认恢复
          </el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { useRoute } from 'vue-router'
import { ref, onMounted, computed, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
import {
  Plus,
  Upload,
  Download,
  RefreshLeft,
  Delete,
  WarningFilled,
  Check,
  Refresh,
  Search,
} from '@element-plus/icons-vue'
import { getBackups, createBackup, restoreBackup, deleteBackup, downloadBackup } from '@/api/system'
import { useAuthStore } from '@/stores/auth'
import { storeToRefs } from 'pinia'
import { hasPermission } from '@/utils/permission'
import { PERMISSIONS } from '@/constants/permissionConstants'

const authStore = useAuthStore()
const { permissions: perms } = storeToRefs(authStore)

const canAddBackup = computed(() => hasPermission(perms.value, PERMISSIONS.BACKUP_ADD))
const canDownloadBackup = computed(() => hasPermission(perms.value, PERMISSIONS.BACKUP_DOWNLOAD))
const canRestoreBackup = computed(() => hasPermission(perms.value, PERMISSIONS.BACKUP_RESTORE))
const canDeleteBackup = computed(() => hasPermission(perms.value, PERMISSIONS.BACKUP_DELETE))
const canQueryBackup = computed(() => hasPermission(perms.value, PERMISSIONS.BACKUP_QUERY))

const loading = ref(false)
const tableData = ref([])
const allData = ref([])
const searchName = ref('')
const restoreDialogVisible = ref(false)
const restoring = ref(false)
const currentBackup = ref(null)

// 分页相关
const page = ref(1)
const size = ref(10)
const total = ref(0)

// 响应式布局
const isMobile = ref(window.innerWidth < 768)
const handleResize = () => {
  isMobile.value = window.innerWidth < 768
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getBackups()
    allData.value = res || []
    handleSearch()
  } catch (error) {
    console.error('获取备份列表失败', error)
    ElMessage.error('获取备份列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  let filtered = allData.value
  if (searchName.value) {
    filtered = allData.value.filter((item) =>
      item.name.toLowerCase().includes(searchName.value.toLowerCase()),
    )
  }
  total.value = filtered.length
  // 本地分页
  const start = (page.value - 1) * size.value
  const end = start + size.value
  tableData.value = filtered.slice(start, end)
}

const handleSizeChange = (val) => {
  size.value = val
  handleSearch()
}

const handleCurrentChange = (val) => {
  page.value = val
  handleSearch()
}

const handleHome = () => {
  page.value = 1
  handleSearch()
}

const handleEnd = () => {
  page.value = Math.ceil(total.value / size.value) || 1
  handleSearch()
}

const handleCreateBackup = () => {
  ElMessageBox.confirm('确定要立即创建新的系统备份吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info',
  }).then(async () => {
    const loadingInstance = ElMessage.success('正在请求创建备份...')
    try {
      await createBackup()
      ElMessage.success('备份创建请求已提交')
      fetchData()
    } catch (error) {
      console.error('创建备份失败', error)
      ElMessage.error('创建备份失败')
    }
  })
}

const handleDownload = async (row) => {
  try {
    ElMessage.info('正在请求下载...')
    const blob = await downloadBackup(row.id)
    if (!blob) {
      throw new Error('下载内容为空')
    }
    const url = window.URL.createObjectURL(new Blob([blob]))
    const link = document.createElement('a')
    link.href = url
    // 使用后端返回的文件名，或者使用 .sql 后缀
    link.setAttribute('download', row.name || `backup_${row.id}.sql`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success(`开始下载: ${row.name}`)
  } catch (error) {
    console.error('下载失败', error)
    ElMessage.error('下载失败')
  }
}

const handleRestore = (row) => {
  currentBackup.value = row
  restoreDialogVisible.value = true
}

const confirmRestore = async () => {
  if (!currentBackup.value) return
  restoring.value = true
  try {
    await restoreBackup(currentBackup.value.id)
    ElMessage.success('系统恢复请求已提交，请稍候')
    restoreDialogVisible.value = false
  } catch (error) {
    console.error('恢复失败', error)
    ElMessage.error(error.message || '恢复失败')
  } finally {
    restoring.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除备份文件 ${row.name} 吗？`, '警告', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await deleteBackup(row.id)
      ElMessage.success('删除成功')
      fetchData()
    } catch (error) {
      console.error('删除失败', error)
      ElMessage.error('删除失败')
    }
  })
}

onMounted(() => {
  fetchData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
/* 布局样式已提取到全局 @/assets/styles/management-layout.css */
/* 移动端卡片样式已提取到 @/assets/styles/mobile-card.css */

.mb-4 {
  margin-bottom: 16px;
}

.backup-name {
  font-weight: bold;
  font-size: 14px;
  color: #303133;
  word-break: break-all;
}

@media (max-width: 768px) {
  .search-bar-container {
    margin-bottom: 12px;
  }
}
</style>
