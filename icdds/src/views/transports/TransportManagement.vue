<template>
  <!--
    TransportManagement.vue - 运输管理页面
    
    用于管理货物的运输任务调度。
    主要功能：
    1. 运输列表：展示运输单号、关联货物、起止地点、时间、状态等信息。
    2. 任务创建/编辑：录入运输任务详情，关联货物。
    3. 状态管理：虽然通常由后台或司机端更新，但管理员可在此维护基础信息。
    4. 搜索筛选：支持按单号、货物名称、状态进行筛选。
  -->
  <div class="management-container">
    <el-card class="management-card">
      <template #header>
        <div class="card-header-content">
          <span>{{ route.meta.title }}</span>
        </div>
      </template>

      <div class="search-bar-container">
        <!-- PC端搜索栏 -->
        <div v-if="!isMobile" class="search-bar">
          <!-- 新增运输按钮 - 需要新增运输权限 -->
          <el-button v-if="canAddTrans" type="primary" @click="handleAddTransport" :icon="Plus">
            <span>新增运输</span>
          </el-button>
          <el-input
            v-model="searchForm.transportNo"
            placeholder="运输单号"
            clearable
            class="w-180"
          />
          <el-input
            v-model="searchForm.customerName"
            placeholder="客户名称"
            clearable
            class="w-180"
          />
          <el-input v-model="searchForm.goodsName" placeholder="货物名称" clearable class="w-180" />
          <el-select v-model="searchForm.status" placeholder="运输状态" clearable class="w-120">
            <el-option
              v-for="item in statusList"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始"
            end-placeholder="结束"
            clearable
            class="w-260"
          />
          <!-- 查询按钮 - 需要查询运输权限 -->
          <el-button v-if="canQueryTrans" type="primary" @click="handleSearch" :icon="Search">
            <span>查询</span>
          </el-button>
          <!-- 重置按钮 - 需要重置运输权限 -->
          <el-button v-if="canResetTrans" @click="resetSearch" :icon="Refresh">
            <span>重置</span>
          </el-button>
        </div>

        <!-- 移动端搜索栏 -->
        <div v-else class="mobile-search-bar">
          <!-- 新增运输按钮 - 需要新增运输权限 -->
          <el-button v-if="canAddTrans" type="primary" @click="handleAddTransport" :icon="Plus">
            <span>新增</span>
          </el-button>
          <!-- 筛选按钮 - 需要查询运输权限 -->
          <el-button
            v-if="canQueryTrans"
            type="info"
            @click="filterDrawerVisible = true"
            :icon="Filter"
          >
            <span>筛选</span>
          </el-button>
        </div>
      </div>

      <!-- 列表内容 -->
      <el-table v-if="!isMobile" :data="transportList" stripe class="transport-table" height="100%">
        <el-table-column type="selection" width="55" fixed="left" />
        <el-table-column prop="id" label="ID" width="80" fixed="left" />
        <el-table-column
          prop="transportNo"
          label="运输单号"
          min-width="150"
          show-overflow-tooltip
        />
        <el-table-column
          prop="customerName"
          label="客户名称"
          min-width="150"
          show-overflow-tooltip
        />
        <el-table-column prop="goodsName" label="货物名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="quantity" label="数量" min-width="100" />
        <el-table-column prop="origin" label="起始地" min-width="150" show-overflow-tooltip />
        <el-table-column prop="destination" label="目的地" min-width="150" show-overflow-tooltip />
        <el-table-column prop="carrier" label="承运商" min-width="120" show-overflow-tooltip />
        <el-table-column prop="vehicleNo" label="车牌号" min-width="120" show-overflow-tooltip />
        <el-table-column prop="driverName" label="司机" min-width="100" show-overflow-tooltip />
        <el-table-column
          prop="driverPhone"
          label="司机电话"
          min-width="120"
          show-overflow-tooltip
        />
        <el-table-column prop="startTime" label="开始时间" min-width="180" show-overflow-tooltip />
        <el-table-column prop="endTime" label="结束时间" min-width="180" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operation" label="操作" width="380" fixed="right">
          <template #default="scope">
            <!-- 发车按钮 - 需要发车权限 -->
            <el-button
              v-if="scope.row.status == 0 && canDepartureTrans"
              type="success"
              size="small"
              :icon="Van"
              @click="handleStartTransport(scope.row)"
            >
              发车
            </el-button>
            <!-- 送达按钮 - 需要送达权限 -->
            <el-button
              v-if="scope.row.status == 1 && canArriveTrans"
              type="primary"
              size="small"
              :icon="Check"
              @click="handleCompleteTransport(scope.row)"
            >
              送达
            </el-button>
            <!-- 取消按钮 - 需要取消运输权限 -->
            <el-button
              v-if="scope.row.status == 0 && canCancelTrans"
              type="warning"
              size="small"
              :icon="Close"
              @click="handleCancelTransport(scope.row)"
            >
              取消
            </el-button>
            <!-- 编辑按钮 - 需要编辑运输权限 -->
            <el-button
              v-if="canEditTrans"
              type="primary"
              size="small"
              :icon="Edit"
              @click="handleEditTransport(scope.row)"
            >
              编辑
            </el-button>
            <!-- 删除按钮 - 需要删除运输权限 -->
            <el-button
              v-if="canDeleteTrans"
              type="danger"
              size="small"
              :icon="Delete"
              @click="handleDeleteTransport(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 移动端卡片列表 -->
      <div v-else class="mobile-card-list">
        <el-empty v-if="transportList.length === 0" description="暂无运输任务" />
        <div v-for="row in transportList" :key="row.id" class="mobile-card-wrapper">
          <el-card class="mobile-card-item" shadow="hover">
            <div class="card-top">
              <div class="transport-info">
                <span class="transport-no">{{ row.transportNo }}</span>
              </div>
              <el-tag :type="getStatusTagType(row.status)" size="small">
                {{ getStatusText(row.status) }}
              </el-tag>
            </div>
            <div class="card-content">
              <div class="content-item">
                <span class="label">ID:</span>
                <span class="value">{{ row.id }}</span>
              </div>
              <div class="content-item">
                <span class="label">客户名称:</span>
                <span class="value">{{ row.customerName }}</span>
              </div>
              <div class="content-item">
                <span class="label">货物名称:</span>
                <span class="value">{{ row.goodsName }}</span>
              </div>
              <div class="content-item">
                <span class="label">数量:</span>
                <span class="value">{{ row.quantity }}</span>
              </div>
              <div class="content-item">
                <span class="label">路线:</span>
                <span class="value">{{ row.origin }} -> {{ row.destination }}</span>
              </div>
              <div class="content-item">
                <span class="label">承运商:</span>
                <span class="value">{{ row.carrier || '-' }}</span>
              </div>
              <div class="content-item">
                <span class="label">车牌号:</span>
                <span class="value">{{ row.vehicleNo || '-' }}</span>
              </div>
              <div class="content-item">
                <span class="label">司机:</span>
                <span class="value">{{ row.driverName || '-' }}</span>
              </div>
              <div class="content-item">
                <span class="label">司机电话:</span>
                <span class="value">{{ row.driverPhone || '-' }}</span>
              </div>
              <div class="content-item">
                <span class="label">开始时间:</span>
                <span class="value">{{ row.startTime || '-' }}</span>
              </div>
              <div class="content-item">
                <span class="label">结束时间:</span>
                <span class="value">{{ row.endTime || '-' }}</span>
              </div>
              <div v-if="row.remarks" class="content-item">
                <span class="label">备注:</span>
                <span class="value">{{ row.remarks }}</span>
              </div>
            </div>
            <div class="card-actions">
              <!-- 发车按钮 - 需要发车权限 -->
              <el-button
                v-if="row.status == 0 && canDepartureTrans"
                type="success"
                size="small"
                :icon="Van"
                @click="handleStartTransport(row)"
              >
                发车
              </el-button>
              <!-- 送达按钮 - 需要送达权限 -->
              <el-button
                v-if="row.status == 1 && canArriveTrans"
                type="primary"
                size="small"
                :icon="Check"
                @click="handleCompleteTransport(row)"
              >
                送达
              </el-button>
              <!-- 取消按钮 - 需要取消运输权限 -->
              <el-button
                v-if="row.status == 0 && canCancelTrans"
                type="warning"
                size="small"
                :icon="Close"
                @click="handleCancelTransport(row)"
              >
                取消
              </el-button>
              <!-- 编辑按钮 - 需要编辑运输权限 -->
              <el-button
                v-if="canEditTrans"
                type="primary"
                size="small"
                :icon="Edit"
                @click="handleEditTransport(row)"
              >
                编辑
              </el-button>
              <!-- 删除按钮 - 需要删除运输权限 -->
              <el-button
                v-if="canDeleteTrans"
                type="danger"
                size="small"
                :icon="Delete"
                @click="handleDeleteTransport(row)"
              >
                删除
              </el-button>
            </div>
          </el-card>
        </div>
      </div>

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

  <!-- 移动端筛选抽屉 -->
  <el-drawer v-model="filterDrawerVisible" title="筛选运输任务" direction="rtl" size="80%">
    <div class="mobile-filter-form">
      <el-form label-position="top">
        <el-form-item label="运输单号">
          <el-input v-model="searchForm.transportNo" placeholder="请输入运输单号" clearable />
        </el-form-item>
        <el-form-item label="客户名称">
          <el-input v-model="searchForm.customerName" placeholder="请输入客户名称" clearable />
        </el-form-item>
        <el-form-item label="货物名称">
          <el-input v-model="searchForm.goodsName" placeholder="请输入货物名称" clearable />
        </el-form-item>
        <el-form-item label="运输状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable class="w-full">
            <el-option
              v-for="item in statusList"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始"
            end-placeholder="结束"
            clearable
            class="w-full"
          />
        </el-form-item>
      </el-form>
      <div class="drawer-footer">
        <el-button @click="resetSearch">重置</el-button>
        <el-button type="primary" @click="handleFilterConfirm">查询</el-button>
      </div>
    </div>
  </el-drawer>

  <!-- 新增/编辑运输抽屉 -->
  <el-drawer
    v-model="transportDialogVisible"
    :title="transportForm.id ? '编辑运输' : '新增运输'"
    size="900px"
    :close-on-click-modal="false"
    destroy-on-close
    class="transport-drawer"
  >
    <splitpanes class="default-theme transport-drawer-splitpanes">
      <pane size="22" min-size="15" class="transport-menu-pane">
        <div class="transport-menu-wrapper">
          <el-menu
            default-active="1"
            class="el-menu-vertical-demo transport-menu"
            @select="handleMenuSelect"
          >
            <el-menu-item index="1">
              <el-icon>
                <Van />
              </el-icon>
              <span>基本信息</span>
            </el-menu-item>
            <el-menu-item index="2">
              <el-icon>
                <Location />
              </el-icon>
              <span>物流详情</span>
            </el-menu-item>
            <el-menu-item index="3">
              <el-icon>
                <User />
              </el-icon>
              <span>司机信息</span>
            </el-menu-item>
          </el-menu>
        </div>
      </pane>
      <pane class="transport-form-pane">
        <div class="transport-form-container">
          <el-scrollbar ref="formScrollbarRef">
            <el-form
              :model="transportForm"
              :rules="transportRules"
              ref="transportFormRef"
              label-width="100px"
              class="transport-form"
            >
              <div id="section-1" class="form-section">
                <h3 class="section-title">基本信息</h3>
                <el-form-item label="运输单号" prop="transportNo" v-if="!transportForm.id">
                  <el-input
                    v-model="transportForm.transportNo"
                    placeholder="请输入运输单号"
                    clearable
                  />
                </el-form-item>
                <el-form-item label="客户名称" prop="customerName">
                  <el-input
                    v-model="transportForm.customerName"
                    placeholder="请输入客户名称"
                    clearable
                  />
                </el-form-item>
                <el-form-item label="货物" prop="goodsId">
                  <el-select
                    v-model="transportForm.goodsId"
                    placeholder="请选择货物"
                    class="w-full"
                  >
                    <el-option
                      v-for="goods in goodsList"
                      :key="goods.id"
                      :label="`${goods.name} (余: ${goods.stock || 0})`"
                      :value="goods.id"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item label="数量" prop="quantity">
                  <el-input
                    v-model="transportForm.quantity"
                    type="number"
                    placeholder="请输入数量"
                    clearable
                  />
                </el-form-item>
                <el-form-item label="状态" prop="status">
                  <el-select v-model="transportForm.status" placeholder="请选择状态" class="w-full">
                    <el-option
                      v-for="item in statusList"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item label="备注" prop="remarks">
                  <el-input
                    v-model="transportForm.remarks"
                    type="textarea"
                    rows="2"
                    placeholder="请输入备注"
                    clearable
                  />
                </el-form-item>
              </div>

              <div id="section-2" class="form-section">
                <el-divider />
                <h3 class="section-title">物流详情</h3>
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="起始地" prop="origin">
                      <el-input
                        v-model="transportForm.origin"
                        placeholder="请输入起始地"
                        clearable
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="目的地" prop="destination">
                      <el-input
                        v-model="transportForm.destination"
                        placeholder="请输入目的地"
                        clearable
                      />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-form-item label="承运商" prop="carrier">
                  <el-input v-model="transportForm.carrier" placeholder="请输入承运商" clearable />
                </el-form-item>
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="开始时间" prop="startTime">
                      <el-date-picker
                        v-model="transportForm.startTime"
                        type="datetime"
                        placeholder="请选择开始时间"
                        class="w-full"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="结束时间" prop="endTime">
                      <el-date-picker
                        v-model="transportForm.endTime"
                        type="datetime"
                        placeholder="请选择结束时间"
                        class="w-full"
                      />
                    </el-form-item>
                  </el-col>
                </el-row>
              </div>

              <div id="section-3" class="form-section">
                <el-divider />
                <h3 class="section-title">司机信息</h3>
                <el-form-item label="车牌号" prop="vehicleNo">
                  <el-input
                    v-model="transportForm.vehicleNo"
                    placeholder="请输入车牌号"
                    clearable
                  />
                </el-form-item>
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="司机姓名" prop="driverName">
                      <el-input
                        v-model="transportForm.driverName"
                        placeholder="请输入司机姓名"
                        clearable
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="司机电话" prop="driverPhone">
                      <el-input
                        v-model="transportForm.driverPhone"
                        placeholder="请输入司机电话"
                        clearable
                      />
                    </el-form-item>
                  </el-col>
                </el-row>
              </div>
            </el-form>
          </el-scrollbar>
        </div>
      </pane>
    </splitpanes>
    <template #footer>
      <div class="flex-auto">
        <!-- 取消按钮 -->
        <el-button :icon="Refresh" @click="transportDialogVisible = false">取消</el-button>
        <!-- 保存按钮 -->
        <el-button type="primary" :icon="Check" @click="handleSaveTransport">保存</el-button>
      </div>
    </template>
  </el-drawer>
</template>

<script setup>
import { useRoute } from 'vue-router'
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'

const route = useRoute()
import { useAuthStore } from '@/stores/auth'
import { hasPermission } from '@/utils/permission'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Search,
  Refresh,
  Van,
  Location,
  User,
  Edit,
  Delete,
  Check,
  Close,
  Filter,
  Right,
} from '@element-plus/icons-vue'
import { Splitpanes, Pane } from 'splitpanes'
import 'splitpanes/dist/splitpanes.css'
import {
  getTransports,
  createTransport,
  updateTransport,
  deleteTransport,
  startTransport,
  completeTransport,
} from '@/api/transport'
import { getGoods } from '@/api/goods'
import { getTransportStatusDict } from '@/api/common'
import { storeToRefs } from 'pinia'
import { PERMISSIONS } from '@/constants/permissionConstants'

const authStore = useAuthStore()
const { permissions: perms } = storeToRefs(authStore)

const canAddTrans = computed(() => hasPermission(perms.value, PERMISSIONS.TRANS_ADD))
const canEditTrans = computed(() => hasPermission(perms.value, PERMISSIONS.TRANS_EDIT))
const canDeleteTrans = computed(() => hasPermission(perms.value, PERMISSIONS.TRANS_DELETE))
const canQueryTrans = computed(() => hasPermission(perms.value, PERMISSIONS.TRANS_QUERY))
const canResetTrans = computed(
  () =>
    hasPermission(perms.value, PERMISSIONS.TRANS_RESET) ||
    hasPermission(perms.value, PERMISSIONS.TRANS_QUERY),
)
const canCancelTrans = computed(() => hasPermission(perms.value, PERMISSIONS.TRANS_CANCEL))
const canDepartureTrans = computed(() => hasPermission(perms.value, PERMISSIONS.TRANS_DEPARTURE))
const canArriveTrans = computed(() => hasPermission(perms.value, PERMISSIONS.TRANS_ARRIVE))

// 响应式布局相关
const isMobile = ref(window.innerWidth < 768)
const filterDrawerVisible = ref(false)

const handleResize = () => {
  isMobile.value = window.innerWidth < 768
}

const handleFilterConfirm = () => {
  filterDrawerVisible.value = false
  handleSearch()
}

// 搜索表单
const searchForm = reactive({
  transportNo: '',
  goodsName: '',
  status: '',
  dateRange: [],
})

// 货物列表（用于下拉选择）
const goodsList = ref([])

// 状态字典列表
const statusList = ref([])

// 运输列表数据
const transportList = ref([])

// 分页信息
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0,
})

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

// 获取运输列表
const fetchTransportList = async () => {
  try {
    const params = {
      page: pagination.currentPage,
      size: pagination.pageSize,
      transportNo: searchForm.transportNo || undefined,
      goodsName: searchForm.goodsName || undefined,
      status: searchForm.status !== '' ? searchForm.status : undefined,
      // 假设后端通过单独的参数处理日期范围，或者如果不在规范中则暂时忽略
      // 如果规范没有提到日期范围参数，我们就跳过，或者如果我们知道参数名就进行适配。
      // 规范说：参数：page, size, transportNo, goodsName, status
      // 所以 dateRange 是客户端过滤或者 API 尚未支持。我在 API 调用中忽略它以避免错误，或者假设后端忽略多余的参数。
    }
    const res = await getTransports(params)
    // 后端返回结构分析:
    // request.js 拦截器已处理 Result 包装器，res 即为 Result.data
    // 若为 PageInfo，包含 list/records 和 total
    if (res.list || res.records) {
      transportList.value = res.list || res.records
      pagination.total = Number(res.total)
    } else if (Array.isArray(res)) {
      transportList.value = res
      pagination.total = res.length
    } else {
      transportList.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('获取运输列表失败:', error)
  }
}

// 获取货物列表 (用于下拉选择)
const fetchGoodsList = async () => {
  try {
    const res = await getGoods({ page: 1, size: 1000 })
    if (res.records || res.list) {
      goodsList.value = res.records || res.list
    } else if (Array.isArray(res)) {
      goodsList.value = res
    }
  } catch (error) {
    console.error('获取货物失败:', error)
  }
}

// 获取状态字典列表
const fetchStatusDict = async () => {
  try {
    const res = await getTransportStatusDict()
    if (res && Array.isArray(res)) {
      statusList.value = res
    }
  } catch (error) {
    console.error('获取运输状态字典失败:', error)
  }
}

onMounted(() => {
  fetchTransportList()
  fetchGoodsList()
  fetchStatusDict()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})

// 运输对话框
const transportDialogVisible = ref(false)
const transportFormRef = ref(null)
const transportForm = reactive({
  id: '',
  transportNo: '',
  customerName: '',
  goodsId: '',
  quantity: '',
  origin: '',
  destination: '',
  carrier: '',
  vehicleNo: '',
  driverName: '',
  driverPhone: '',
  startTime: '',
  endTime: '',
  status: '',
  remarks: '',
})

// 运输表单验证规则
const transportRules = {
  transportNo: [
    { required: true, message: '请输入运输单号', trigger: 'blur' },
    // { pattern: /^T\d{8}$/, message: '运输单号格式为T+8位数字', trigger: 'blur' },
  ],
  goodsId: [{ required: true, message: '请选择货物', trigger: 'change' }],
  quantity: [
    { required: true, message: '请输入数量', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (Number(value) > 0) {
          callback()
        } else {
          callback(new Error('数量必须大于0'))
        }
      },
      trigger: 'blur',
    },
  ],
  origin: [{ required: true, message: '请输入起始地', trigger: 'blur' }],
  destination: [{ required: true, message: '请输入目的地', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

// 获取状态文本
const getStatusText = (status) => {
  const item = statusList.value.find((item) => item.value === Number(status))
  return item ? item.label : '未知'
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  const item = statusList.value.find((item) => item.value === Number(status))
  return item ? item.type : 'info'
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.currentPage = 1
  fetchTransportList()
}

const handleCurrentChange = (page) => {
  pagination.currentPage = page
  fetchTransportList()
}

// 搜索和重置
const handleSearch = () => {
  pagination.currentPage = 1
  fetchTransportList()
}

const resetSearch = () => {
  Object.keys(searchForm).forEach((key) => {
    searchForm[key] = ''
  })
  searchForm.dateRange = []
  pagination.currentPage = 1
  fetchTransportList()
}

// 新增运输
const handleAddTransport = () => {
  Object.keys(transportForm).forEach((key) => {
    transportForm[key] = ''
  })
  delete transportForm.id
  transportDialogVisible.value = true
}

// 编辑运输
const handleEditTransport = (row) => {
  Object.assign(transportForm, row)
  // 转换日期格式
  if (row.startTime) transportForm.startTime = new Date(row.startTime)
  if (row.endTime) transportForm.endTime = new Date(row.endTime)
  transportDialogVisible.value = true
}

// 保存运输
const handleSaveTransport = () => {
  transportFormRef.value.validate(async (valid) => {
    if (valid) {
      // 处理日期格式
      const formData = {
        ...transportForm,
        startTime: transportForm.startTime ? formatDateTime(transportForm.startTime) : null,
        endTime: transportForm.endTime ? formatDateTime(transportForm.endTime) : null,
        // 如果发送 goodsId，通常不需要发送 goodsName，但如果后端需要它：
        goodsName: goodsList.value.find((g) => g.id === transportForm.goodsId)?.name || '',
      }

      try {
        if (transportForm.id) {
          // 编辑运输
          await updateTransport(transportForm.id, formData)
          ElMessage.success('运输信息已更新')
        } else {
          // 新增运输
          await createTransport(formData)
          ElMessage.success('运输记录已创建')
        }
        transportDialogVisible.value = false
        fetchTransportList()
      } catch (error) {
        console.error('保存运输失败', error)
        const msg = error.response?.data?.message || error.message || ''
        if (msg.includes('库存不足')) {
          ElMessage.error(msg)
        }
      }
    }
  })
}

// 状态操作处理
const handleStartTransport = (row) => {
  ElMessageBox.confirm(`确定要开始运输任务"${row.transportNo}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'success',
  })
    .then(async () => {
      try {
        await startTransport(row.id)
        ElMessage.success('已开始运输')
        fetchTransportList()
      } catch (error) {
        console.error('操作失败', error)
      }
    })
    .catch(() => {})
}

const handleCompleteTransport = (row) => {
  ElMessageBox.confirm(`确定要完成运输任务"${row.transportNo}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'success',
  })
    .then(async () => {
      try {
        await completeTransport(row.id)
        ElMessage.success('运输任务已完成')
        fetchTransportList()
      } catch (error) {
        console.error('操作失败', error)
      }
    })
    .catch(() => {})
}

const handleCancelTransport = (row) => {
  ElMessageBox.confirm(`确定要取消运输任务"${row.transportNo}"吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      try {
        await cancelTransport(row.id)
        ElMessage.success('运输任务已取消')
        fetchTransportList()
      } catch (error) {
        console.error('操作失败', error)
      }
    })
    .catch(() => {})
}

// 删除运输
const handleDeleteTransport = (row) => {
  ElMessageBox.confirm(`确定要删除运输记录"${row.transportNo}"吗？`, '确认删除', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      try {
        await deleteTransport(row.id)
        ElMessage.success('运输记录已删除')
        // 如果当前页只有一条数据且不是第一页，则跳转到上一页
        if (transportList.value.length === 1 && pagination.currentPage > 1) {
          pagination.currentPage--
        }
        fetchTransportList()
      } catch (error) {
        console.error('删除运输失败', error)
      }
    })
    .catch(() => {
      // 取消删除
    })
}

// 格式化日期时间
const formatDateTime = (date) => {
  if (!date) return ''
  // 检查是否已经是字符串
  if (typeof date === 'string') return date

  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`
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

.mobile-search-bar {
  display: flex;
  gap: 10px;
  padding: 0 4px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-top: 15px;
}

.mobile-pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  width: 100%;
}

.form-section {
  margin-bottom: 30px;
}

/* 常用宽度类 */
.w-120 {
  width: 120px;
}
.w-150 {
  width: 150px;
}
.w-180 {
  width: 180px;
}
.w-200 {
  width: 200px;
}
.w-260 {
  width: 260px;
}
.w-300 {
  width: 300px;
}
.w-full {
  width: 100%;
}

.transport-table {
  width: 100%;
  flex: 1;
}

.text-danger {
  color: var(--el-color-danger);
}

.transport-drawer-splitpanes {
  height: 100%;
  border: 1px solid var(--el-border-color-light);
}

.transport-menu-pane {
  background-color: transparent;
}

.transport-menu-wrapper {
  padding: 10px;
  height: 100%;
}

.transport-menu {
  border-right: none;
  background-color: transparent;
}

.transport-form-pane {
  overflow: hidden;
  background-color: transparent;
}

.transport-form-container {
  padding: 20px;
  height: 100%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}

.transport-form {
  padding-right: 20px;
}

/* 移动端样式适配 */
@media screen and (max-width: 768px) {
  .search-bar-container {
    margin-bottom: 10px;
  }

  .mobile-filter-form {
    padding: 0 16px;
    height: 100%;
    display: flex;
    flex-direction: column;
  }

  .drawer-footer {
    margin-top: auto;
    padding: 20px 0;
    display: flex;
    gap: 12px;
  }

  :deep(.el-card__body) {
    padding: 10px !important;
  }
}

.transport-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.transport-no {
  font-weight: bold;
  font-size: 14px;
}

/* Pagination */
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
</style>
