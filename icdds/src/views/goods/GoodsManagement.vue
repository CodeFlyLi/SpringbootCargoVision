<template>
  <!--
    GoodsManagement.vue - 货物管理页面
    
    用于管理系统中的货物基础信息。
    主要功能：
    1. 货物列表：展示货物编号、名称、类型、重量、体积等信息。
    2. 新增/编辑货物：通过弹窗表单录入货物详细信息。
    3. 删除货物：支持删除不再使用的货物信息。
    4. 搜索筛选：支持按编号、名称、类型进行筛选。
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
          <!-- 新增货物按钮 - 需要新增货物权限 -->
          <el-button v-if="canAddGoods" type="primary" @click="handleAddGoods" :icon="Plus">
            <span>新增货物</span>
          </el-button>
          <el-input v-model="searchForm.goodsNo" placeholder="货物编号" clearable class="w-180" />
          <el-input v-model="searchForm.name" placeholder="货物名称" clearable class="w-180" />
          <el-select v-model="searchForm.typeId" placeholder="货物类型" clearable class="w-150">
            <el-option
              v-for="type in goodsTypeList"
              :key="type.id"
              :label="type.typeName"
              :value="type.id"
            />
          </el-select>
          <!-- 查询按钮 - 需要查询货物权限 -->
          <el-button v-if="canQueryGoods" type="primary" @click="handleSearch" :icon="Search">
            <span>查询</span>
          </el-button>
          <!-- 重置按钮 - 需要重置货物权限 -->
          <el-button v-if="canResetGoods" @click="resetSearch" :icon="Refresh">
            <span>重置</span>
          </el-button>
        </div>

        <!-- 移动端搜索栏 -->
        <div v-else class="mobile-search-bar">
          <el-button v-if="canAddGoods" type="primary" @click="handleAddGoods">
            <el-icon><Plus /></el-icon>
            <span>新增</span>
          </el-button>
          <el-button v-if="canQueryGoods" type="info" @click="filterDrawerVisible = true">
            <el-icon><Filter /></el-icon>
            <span>筛选</span>
          </el-button>
        </div>
      </div>

      <!-- PC端表格 -->
      <el-table v-if="!isMobile" :data="goodsList" stripe class="goods-table" height="100%">
        <el-table-column type="selection" width="55" fixed="left" />
        <el-table-column prop="id" label="ID" width="80" fixed="left" />
        <el-table-column prop="goodsNo" label="货物编号" min-width="150" show-overflow-tooltip />
        <el-table-column prop="name" label="货物名称" min-width="180" show-overflow-tooltip />
        <el-table-column label="图片" width="100">
          <template #default="scope">
            <el-image
              v-if="scope.row.imageUrl"
              class="table-img"
              :src="scope.row.imageUrl"
              :preview-src-list="[scope.row.imageUrl]"
              fit="cover"
            />
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" min-width="100">
          <template #default="scope">
            <span :class="{ 'low-stock': scope.row.stock < 10 }">
              {{ scope.row.stock }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="typeName" label="货物类型" min-width="120" show-overflow-tooltip />
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column prop="weight" label="重量(kg)" min-width="120" />
        <el-table-column prop="volume" label="体积(m³)" min-width="120" />
        <el-table-column prop="price" label="价值(¥)" min-width="120" />
        <el-table-column
          prop="manufacturer"
          label="生产厂家"
          min-width="150"
          show-overflow-tooltip
        />
        <el-table-column
          prop="specifications"
          label="规格型号"
          min-width="150"
          show-overflow-tooltip
        />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="创建时间" min-width="180" show-overflow-tooltip />
        <el-table-column prop="operation" label="操作" width="230" fixed="right">
          <template #default="scope">
            <!-- 编辑按钮 - 需要编辑货物权限 -->
            <el-button
              v-if="canEditGoods"
              type="primary"
              size="small"
              :icon="Edit"
              @click="handleEditGoods(scope.row)"
            >
              编辑
            </el-button>
            <!-- 删除按钮 - 需要删除货物权限 -->
            <el-button
              v-if="canDeleteGoods"
              type="danger"
              size="small"
              :icon="Delete"
              @click="handleDeleteGoods(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 移动端卡片列表 -->
      <div v-else class="mobile-card-list">
        <el-empty v-if="goodsList.length === 0" description="暂无货物数据" />
        <div v-for="row in goodsList" :key="row.id" class="mobile-card-wrapper">
          <el-card class="mobile-card-item" shadow="hover">
            <div class="card-top">
              <div class="goods-info-header">
                <span class="goods-name">{{ row.name }}</span>
              </div>
              <el-tag size="small">{{ row.typeName }}</el-tag>
            </div>
            <div class="card-content">
              <div class="content-wrapper">
                <div class="card-image">
                  <el-image
                    v-if="row.imageUrl"
                    :src="row.imageUrl"
                    :preview-src-list="[row.imageUrl]"
                    fit="cover"
                    class="mobile-img"
                  />
                  <div v-else class="no-img">暂无图片</div>
                </div>
                <div class="info-list">
                  <div class="content-item">
                    <span class="label">ID:</span>
                    <span class="value">{{ row.id }}</span>
                  </div>
                  <div class="content-item">
                    <span class="label">编号:</span>
                    <span class="value">{{ row.goodsNo }}</span>
                  </div>
                  <div class="content-item">
                    <span class="label">库存:</span>
                    <span class="value" :class="{ 'low-stock': row.stock < 10 }">
                      {{ row.stock }} {{ row.unit }}
                    </span>
                  </div>
                  <div class="content-item">
                    <span class="label">重量:</span>
                    <span class="value">{{ row.weight }}kg</span>
                  </div>
                  <div class="content-item">
                    <span class="label">体积:</span>
                    <span class="value">{{ row.volume }}m³</span>
                  </div>
                </div>
              </div>

              <div class="content-item" v-if="row.price">
                <span class="label">价值:</span>
                <span class="value">¥{{ row.price }}</span>
              </div>
              <div class="content-item" v-if="row.manufacturer">
                <span class="label">厂家:</span>
                <span class="value">{{ row.manufacturer }}</span>
              </div>
              <div class="content-item" v-if="row.specifications">
                <span class="label">规格:</span>
                <span class="value">{{ row.specifications }}</span>
              </div>
              <div class="content-item" v-if="row.description">
                <span class="label">描述:</span>
                <span class="value">{{ row.description }}</span>
              </div>
              <div class="content-item">
                <span class="label">创建时间:</span>
                <span class="value">{{ row.createdAt }}</span>
              </div>
            </div>
            <div class="card-actions">
              <el-button
                v-if="canEditGoods"
                type="primary"
                size="small"
                :icon="Edit"
                @click="handleEditGoods(row)"
              >
                编辑
              </el-button>
              <el-button
                v-if="canDeleteGoods"
                type="danger"
                size="small"
                :icon="Delete"
                @click="handleDeleteGoods(row)"
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

    <!-- 移动端筛选抽屉 -->
    <el-drawer
      v-model="filterDrawerVisible"
      title="搜索筛选"
      direction="rtl"
      size="80%"
      :with-header="true"
    >
      <div class="mobile-filter-form">
        <el-form label-position="top">
          <el-form-item label="货物编号">
            <el-input v-model="searchForm.goodsNo" placeholder="请输入货物编号" clearable />
          </el-form-item>
          <el-form-item label="货物名称">
            <el-input v-model="searchForm.name" placeholder="请输入货物名称" clearable />
          </el-form-item>
          <el-form-item label="货物类型">
            <el-select
              v-model="searchForm.typeId"
              placeholder="请选择货物类型"
              class="w-full"
              clearable
            >
              <el-option
                v-for="type in goodsTypeList"
                :key="type.id"
                :label="type.typeName"
                :value="type.id"
              />
            </el-select>
          </el-form-item>
        </el-form>
        <div class="drawer-footer">
          <el-button @click="resetSearch">重置</el-button>
          <el-button type="primary" @click="handleFilterConfirm">确定</el-button>
        </div>
      </div>
    </el-drawer>
  </div>

  <!-- 新增/编辑货物抽屉 -->
  <el-drawer
    v-model="goodsDialogVisible"
    :title="goodsForm.id ? '编辑货物' : '新增货物'"
    :size="isMobile ? '100%' : '900px'"
    :close-on-click-modal="false"
    destroy-on-close
    class="goods-drawer"
  >
    <div v-if="isMobile" class="mobile-form-container">
      <el-form
        :model="goodsForm"
        :rules="goodsRules"
        ref="goodsFormRef"
        label-position="top"
        class="goods-form"
      >
        <div class="form-section">
          <h3 class="section-title">基本信息</h3>
          <el-form-item label="货物编号" prop="goodsNo" v-if="!goodsForm.id">
            <el-input v-model="goodsForm.goodsNo" placeholder="请输入货物编号" clearable />
          </el-form-item>
          <el-form-item label="货物名称" prop="name">
            <el-input v-model="goodsForm.name" placeholder="请输入货物名称" clearable />
          </el-form-item>
          <el-form-item label="货物类型" prop="typeId">
            <el-select v-model="goodsForm.typeId" placeholder="请选择货物类型" class="w-full">
              <el-option
                v-for="type in goodsTypeList"
                :key="type.id"
                :label="type.typeName"
                :value="type.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="库存数量" prop="stock">
            <el-input
              v-model="goodsForm.stock"
              type="number"
              placeholder="请输入库存数量"
              clearable
            />
          </el-form-item>
          <el-form-item label="货物图片" prop="imageUrl">
            <el-input v-model="goodsForm.imageUrl" placeholder="请输入图片URL" clearable />
          </el-form-item>
        </div>

        <el-divider />

        <div class="form-section">
          <h3 class="section-title">详细参数</h3>
          <el-form-item label="单位" prop="unit">
            <el-input v-model="goodsForm.unit" placeholder="默认为'件'" clearable />
          </el-form-item>
          <el-form-item label="生产厂家" prop="manufacturer">
            <el-input v-model="goodsForm.manufacturer" placeholder="请输入生产厂家" clearable />
          </el-form-item>
          <el-form-item label="规格型号" prop="specifications">
            <el-input v-model="goodsForm.specifications" placeholder="请输入规格型号" clearable />
          </el-form-item>
          <el-form-item label="价值(¥)" prop="price">
            <el-input v-model="goodsForm.price" type="number" placeholder="请输入价值" clearable />
          </el-form-item>
          <el-form-item label="重量(kg)" prop="weight">
            <el-input v-model="goodsForm.weight" type="number" placeholder="请输入重量" clearable />
          </el-form-item>
          <el-form-item label="体积(m³)" prop="volume">
            <el-input v-model="goodsForm.volume" type="number" placeholder="请输入体积" clearable />
          </el-form-item>
          <el-form-item label="描述" prop="description">
            <el-input
              v-model="goodsForm.description"
              type="textarea"
              rows="3"
              placeholder="请输入描述"
              clearable
            />
          </el-form-item>
        </div>
      </el-form>
    </div>
    <splitpanes v-else class="default-theme goods-drawer-splitpanes">
      <pane size="22" min-size="15" class="goods-menu-pane">
        <div class="menu-pane-content">
          <el-menu
            default-active="1"
            class="el-menu-vertical-demo goods-menu"
            @select="handleMenuSelect"
          >
            <el-menu-item index="1">
              <el-icon>
                <Box />
              </el-icon>
              <span>基本信息</span>
            </el-menu-item>
            <el-menu-item index="2">
              <el-icon>
                <InfoFilled />
              </el-icon>
              <span>详细参数</span>
            </el-menu-item>
          </el-menu>
        </div>
      </pane>
      <pane class="goods-form-pane">
        <div class="form-pane-content">
          <el-scrollbar ref="formScrollbarRef">
            <el-form
              :model="goodsForm"
              :rules="goodsRules"
              ref="goodsFormRef"
              label-width="100px"
              class="goods-form"
            >
              <div id="section-1" class="form-section">
                <h3 class="section-title">基本信息</h3>
                <el-form-item label="货物编号" prop="goodsNo" v-if="!goodsForm.id">
                  <el-input v-model="goodsForm.goodsNo" placeholder="请输入货物编号" clearable />
                </el-form-item>
                <el-form-item label="货物名称" prop="name">
                  <el-input v-model="goodsForm.name" placeholder="请输入货物名称" clearable />
                </el-form-item>
                <el-form-item label="货物类型" prop="typeId">
                  <el-select v-model="goodsForm.typeId" placeholder="请选择货物类型" class="w-full">
                    <el-option
                      v-for="type in goodsTypeList"
                      :key="type.id"
                      :label="type.typeName"
                      :value="type.id"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item label="库存数量" prop="stock">
                  <el-input
                    v-model="goodsForm.stock"
                    type="number"
                    placeholder="请输入库存数量"
                    clearable
                  />
                </el-form-item>
                <el-form-item label="货物图片" prop="imageUrl">
                  <el-input v-model="goodsForm.imageUrl" placeholder="请输入图片URL" clearable />
                </el-form-item>
              </div>

              <div id="section-2" class="form-section">
                <el-divider />
                <h3 class="section-title">详细参数</h3>
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="单位" prop="unit">
                      <el-input v-model="goodsForm.unit" placeholder="默认为'件'" clearable />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="生产厂家" prop="manufacturer">
                      <el-input
                        v-model="goodsForm.manufacturer"
                        placeholder="请输入生产厂家"
                        clearable
                      />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="规格型号" prop="specifications">
                      <el-input
                        v-model="goodsForm.specifications"
                        placeholder="请输入规格型号"
                        clearable
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="价值(¥)" prop="price">
                      <el-input
                        v-model="goodsForm.price"
                        type="number"
                        placeholder="请输入价值"
                        clearable
                      />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="重量(kg)" prop="weight">
                      <el-input
                        v-model="goodsForm.weight"
                        type="number"
                        placeholder="请输入重量"
                        clearable
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="体积(m³)" prop="volume">
                      <el-input
                        v-model="goodsForm.volume"
                        type="number"
                        placeholder="请输入体积"
                        clearable
                      />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-form-item label="描述" prop="description">
                  <el-input
                    v-model="goodsForm.description"
                    type="textarea"
                    rows="3"
                    placeholder="请输入描述"
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
        <!-- 取消按钮 -->
        <el-button @click="goodsDialogVisible = false">取消</el-button>
        <!-- 保存按钮 -->
        <el-button type="primary" :icon="Check" @click="handleSaveGoods">保存</el-button>
      </div>
    </template>
  </el-drawer>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useAuthStore } from '@/stores/auth'
import { hasPermission } from '@/utils/permission'
import { PERMISSIONS } from '@/constants/permissionConstants'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Search,
  Refresh,
  Box,
  InfoFilled,
  Edit,
  Delete,
  Check,
} from '@element-plus/icons-vue'
import { Splitpanes, Pane } from 'splitpanes'
import 'splitpanes/dist/splitpanes.css'
import { getGoods, createGoods, updateGoods, deleteGoods, getGoodsTypes } from '@/api/goods'

const route = useRoute()
const authStore = useAuthStore()
const { permissions: perms } = storeToRefs(authStore)

// 响应式布局相关
const isMobile = ref(window.innerWidth < 768)
const filterDrawerVisible = ref(false)

const handleResize = () => {
  isMobile.value = window.innerWidth < 768
}

const handleFilterConfirm = () => {
  filterDrawerVisible.value = false
}

const canAddGoods = computed(() => hasPermission(perms.value, PERMISSIONS.GOODS_ADD))
const canEditGoods = computed(() => hasPermission(perms.value, PERMISSIONS.GOODS_EDIT))
const canDeleteGoods = computed(() => hasPermission(perms.value, PERMISSIONS.GOODS_DELETE))
const canQueryGoods = computed(() => hasPermission(perms.value, PERMISSIONS.GOODS_QUERY))
const canResetGoods = computed(
  () =>
    hasPermission(perms.value, PERMISSIONS.GOODS_RESET) ||
    hasPermission(perms.value, PERMISSIONS.GOODS_QUERY),
)

// 货物类型列表
const goodsTypeList = ref([])

// 获取货物类型列表
const fetchGoodsTypeList = async () => {
  try {
    const res = await getGoodsTypes()
    if (Array.isArray(res)) {
      goodsTypeList.value = res
    } else if (res.list) {
      goodsTypeList.value = res.list
    }
  } catch (error) {
    console.error('获取货物类型列表失败:', error)
  }
}

// 搜索表单
const searchForm = reactive({
  goodsNo: '',
  name: '',
  stock: '',
  typeId: '',
})

// 货物列表数据
const goodsList = ref([])

// 分页信息
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0,
})

// 获取货物列表
const fetchGoodsList = async () => {
  try {
    const params = {
      page: pagination.currentPage,
      size: pagination.pageSize,
      goodsNo: searchForm.goodsNo,
      name: searchForm.name,
      typeId: searchForm.typeId,
    }
    const res = await getGoods(params)
    const rawList = Array.isArray(res) ? res : res.list || res.records || res.data || []
    pagination.total = typeof res.total !== 'undefined' ? Number(res.total) : rawList.length
    goodsList.value = rawList
  } catch (error) {
    console.error('获取货物列表失败:', error)
    ElMessage.error('获取货物列表失败')
  }
}

onMounted(() => {
  fetchGoodsTypeList()
  fetchGoodsList()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})

// 货物对话框
const goodsDialogVisible = ref(false)
const goodsFormRef = ref(null)
const formScrollbarRef = ref(null)

// 菜单选择处理 - 滚动到对应区域
const handleMenuSelect = (index) => {
  const sectionId = `section-${index}`
  const element = document.getElementById(sectionId)
  if (element) {
    element.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

const goodsForm = reactive({
  id: '',
  goodsNo: '',
  name: '',
  stock: 0,
  typeId: '',
  imageUrl: '',
  unit: '件',
  manufacturer: '',
  specifications: '',
  weight: '',
  volume: '',
  price: '',
  description: '',
})

// 货物表单验证规则
const goodsRules = {
  goodsNo: [
    { required: true, message: '请输入货物编号', trigger: 'blur' },
    // { pattern: /^G\d{8}$/, message: '货物编号格式为G+8位数字', trigger: 'blur' }, // 为了灵活性移除了严格模式
  ],
  name: [{ required: true, message: '请输入货物名称', trigger: 'blur' }],
  stock: [
    { required: true, message: '请输入库存数量', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        if (Number(value) >= 0) {
          callback()
        } else {
          callback(new Error('库存必须大于等于0'))
        }
      },
      trigger: 'blur',
    },
  ],
  typeId: [{ required: true, message: '请选择货物类型', trigger: 'change' }],
  weight: [
    { required: true, message: '请输入重量', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        if (Number(value) > 0) {
          callback()
        } else {
          callback(new Error('重量必须大于0'))
        }
      },
      trigger: 'blur',
    },
  ],
  volume: [
    { required: true, message: '请输入体积', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        if (Number(value) > 0) {
          callback()
        } else {
          callback(new Error('体积必须大于0'))
        }
      },
      trigger: 'blur',
    },
  ],
  price: [
    { required: true, message: '请输入价值', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        if (Number(value) >= 0) {
          callback()
        } else {
          callback(new Error('价值必须大于等于0'))
        }
      },
      trigger: 'blur',
    },
  ],
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.currentPage = 1
  fetchGoodsList()
}

const handleCurrentChange = (page) => {
  pagination.currentPage = page
  fetchGoodsList()
}

// 搜索和重置
const handleSearch = () => {
  pagination.currentPage = 1
  fetchGoodsList()
}

const resetSearch = () => {
  Object.keys(searchForm).forEach((key) => {
    searchForm[key] = ''
  })
  pagination.currentPage = 1
  fetchGoodsList()
}

// 新增货物
const handleAddGoods = () => {
  Object.keys(goodsForm).forEach((key) => {
    if (key === 'unit') goodsForm[key] = '件'
    else goodsForm[key] = ''
  })
  delete goodsForm.id
  goodsDialogVisible.value = true
}

// 编辑货物
const handleEditGoods = (row) => {
  Object.assign(goodsForm, row)
  goodsDialogVisible.value = true
}

// 保存货物
const handleSaveGoods = () => {
  goodsFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const payload = {
          ...goodsForm,
          typeId: Number(goodsForm.typeId),
          stock: Number(goodsForm.stock),
          weight: Number(goodsForm.weight),
          volume: Number(goodsForm.volume),
          price: Number(goodsForm.price),
        }

        if (goodsForm.id) {
          // 编辑货物
          await updateGoods(goodsForm.id, payload)
          ElMessage.success('货物信息已更新')
        } else {
          // 新增货物
          await createGoods(payload)
          ElMessage.success('货物已创建')
        }
        goodsDialogVisible.value = false
        fetchGoodsList()
      } catch (error) {
        console.error('保存货物失败', error)
      }
    }
  })
}

// 删除货物
const handleDeleteGoods = (row) => {
  ElMessageBox.confirm(`确定要删除货物"${row.name}"吗？`, '确认删除', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      try {
        await deleteGoods(row.id)
        ElMessage.success('货物已删除')
        // 如果当前页只有一条数据且不是第一页，则跳转到上一页
        if (goodsList.value.length === 1 && pagination.currentPage > 1) {
          pagination.currentPage--
        }
        fetchGoodsList()
      } catch (error) {
        console.error('删除货物失败', error)
      }
    })
    .catch(() => {
      // 取消删除
    })
}

// 获取货物类型名称
const getTypeName = (typeId) => {
  const type = goodsTypeList.value.find((item) => item.id === typeId)
  return type ? type.typeName : typeId
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

.mobile-pagination :deep(.el-pagination) {
  padding: 0;
}

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
.w-full {
  width: 100%;
}

.table-img {
  width: 50px;
  height: 50px;
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

.goods-form {
  padding-right: 20px;
}

.low-stock {
  color: var(--el-color-danger);
  font-weight: bold;
}

.goods-table {
  width: 100%;
  flex: 1;
}

.text-danger {
  color: var(--el-color-danger);
}

.goods-drawer-splitpanes {
  height: 100%;
  border: 1px solid var(--el-border-color-light);
}

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

  .mobile-form-container {
    padding: 0 4px;
  }

  :deep(.el-drawer__body) {
    padding: 16px;
  }

  :deep(.el-card__body) {
    padding: 10px !important;
  }
}

.goods-info-header {
  display: flex;
  align-items: center;
}

.goods-name {
  font-weight: bold;
  font-size: 15px;
  margin-right: 8px;
}

.content-wrapper {
  display: flex;
  gap: 12px;
  margin-bottom: 8px;
}

.card-image {
  width: 80px;
  height: 80px;
  flex-shrink: 0;
  border-radius: 4px;
  overflow: hidden;
  background-color: var(--el-fill-color-light);
}

.mobile-img {
  width: 100%;
  height: 100%;
}

.no-img {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.info-list {
  flex: 1;
  min-width: 0;
}

.goods-menu-pane {
  background-color: transparent;
}

.goods-menu {
  border-right: none;
  background-color: transparent;
}

.goods-form-pane {
  overflow: hidden;
  background-color: transparent;
}
</style>
