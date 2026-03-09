<template>
  <!--
    Dashboard.vue - 仪表盘页面
    
    系统首页，展示核心业务数据的统计概览。
    主要功能：
    1. 统计卡片：展示货物总数、运输次数、破损率、损失金额等关键指标。
    2. 数据图表：
       - 货物破损统计 (折线图)：展示不同时间维度的破损数量趋势。
       - 运输状态分布 (环形图)：展示当前运输任务的状态分布。
    3. 最近记录：展示最新的检测记录列表。
    4. 交互功能：支持按周、月、季度、年筛选图表数据。
  -->
  <div class="dashboard-container">
    <el-card class="dashboard-main-card">
      <template #header>
        <div class="card-header-content">
          <span>{{ route.meta.title }}</span>
        </div>
      </template>

      <!-- 顶部操作栏 -->
      <div class="search-bar-container">
        <!-- PC端操作栏 -->
        <div v-if="!isMobile" class="search-bar">
          <!-- 刷新数据按钮 - 需要查询仪表盘权限 -->
          <el-button
            v-if="canQueryDashboard"
            type="primary"
            :icon="Refresh"
            @click="fetchDashboardData"
            :loading="loading"
          >
            刷新数据
          </el-button>
        </div>

        <!-- 移动端操作栏 -->
        <div v-else class="mobile-search-bar">
          <!-- 刷新按钮 - 需要查询仪表盘权限 -->
          <el-button
            v-if="canQueryDashboard"
            type="primary"
            :icon="Refresh"
            @click="fetchDashboardData"
            :loading="loading"
          >
            刷新
          </el-button>
        </div>
      </div>

      <div class="stats-cards">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon icon-blue">
              <el-icon>
                <box />
              </el-icon>
            </div>
            <div>
              <p class="stat-label">运输总单数</p>
              <p class="stat-value">{{ stats.transportTotal }}</p>
            </div>
          </div>
        </el-card>
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon icon-green">
              <el-icon>
                <truck />
              </el-icon>
            </div>
            <div>
              <p class="stat-label">运输中</p>
              <p class="stat-value">{{ stats.transportActive }}</p>
            </div>
          </div>
        </el-card>
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon icon-yellow">
              <el-icon><alert-triangle /></el-icon>
            </div>
            <div>
              <p class="stat-label">检测总次数</p>
              <p class="stat-value">{{ stats.detectionTotal }}</p>
            </div>
          </div>
        </el-card>
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon icon-purple">
              <el-icon><dollar-sign /></el-icon>
            </div>
            <div>
              <p class="stat-label">发现破损数</p>
              <p class="stat-value">{{ stats.detectionAbnormal }}</p>
            </div>
          </div>
        </el-card>
      </div>

      <div class="charts-container">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>近7天运输趋势</span>
            </div>
          </template>
          <div class="chart-wrapper">
            <canvas id="transportTrendChart"></canvas>
          </div>
        </el-card>
        <el-card>
          <template #header>
            <div class="card-header">
              <span>破损情况分布</span>
            </div>
          </template>
          <div class="chart-wrapper">
            <canvas id="damageDistributionChart"></canvas>
          </div>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup>
/**
 * Dashboard.vue - 仪表盘页面
 */
import { ref, onMounted, reactive, nextTick, onUnmounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import Chart from 'chart.js/auto'
import ChartDataLabels from 'chartjs-plugin-datalabels'
import {
  Box,
  Van as Truck,
  Warning as AlertTriangle,
  Money as DollarSign,
  Refresh,
} from '@element-plus/icons-vue'
import { getDashboardStats, getDashboardCharts } from '@/api/dashboard'
import { useAuthStore } from '@/stores/auth'
import { storeToRefs } from 'pinia'
import { hasPermission } from '@/utils/permission'
import { PERMISSIONS } from '@/constants/permissionConstants'

// 注册数据标签插件
Chart.register(ChartDataLabels)

const route = useRoute()

const authStore = useAuthStore()
const { permissions: perms } = storeToRefs(authStore)

const canQueryDashboard = computed(() => hasPermission(perms.value, PERMISSIONS.DASHBOARD_QUERY))

// 响应式布局
const isMobile = ref(window.innerWidth < 768)
const loading = ref(false)
const handleResize = () => {
  const newIsMobile = window.innerWidth < 768
  if (isMobile.value !== newIsMobile) {
    isMobile.value = newIsMobile
    // 重新渲染图表以适应移动端
    fetchDashboardData()
  }
}

// 顶部统计卡片数据
const stats = reactive({
  transportTotal: 0,
  transportActive: 0,
  detectionTotal: 0,
  detectionAbnormal: 0,
})

let transportTrendChart = null
let damageDistributionChart = null

/**
 * 获取仪表盘数据
 */
const fetchDashboardData = async () => {
  loading.value = true
  try {
    // 1. 获取统计数据
    const statsRes = await getDashboardStats()
    if (statsRes) {
      const { transportStats, detectionStats, transportTrend, damageDistribution } = statsRes
      if (transportStats) {
        stats.transportTotal = transportStats.total || 0
        stats.transportActive = transportStats.active || 0
      }
      if (detectionStats) {
        stats.detectionTotal = detectionStats.total || 0
        stats.detectionAbnormal = detectionStats.abnormal || 0
      }

      // 直接使用统计接口返回的图表数据
      renderCharts({ transportTrend, damageDistribution })
    }
  } catch (error) {
    console.error('获取仪表盘数据失败', error)
  } finally {
    loading.value = false
  }
}

const renderCharts = (data) => {
  const { transportTrend, damageDistribution } = data

  // 获取主题颜色
  const style = getComputedStyle(document.documentElement)
  const primaryColor = style.getPropertyValue('--el-color-primary').trim() || '#409EFF'
  const successColor = style.getPropertyValue('--el-color-success').trim() || '#67C23A'
  const warningColor = style.getPropertyValue('--el-color-warning').trim() || '#E6A23C'
  const dangerColor = style.getPropertyValue('--el-color-danger').trim() || '#F56C6C'
  const infoColor = style.getPropertyValue('--el-color-info').trim() || '#909399'

  // 渲染运输趋势图 (折线图)
  if (transportTrend) {
    const ctx = document.getElementById('transportTrendChart')
    if (ctx) {
      if (transportTrendChart) transportTrendChart.destroy()
      transportTrendChart = new Chart(ctx, {
        type: 'line',
        data: {
          labels: transportTrend.map((item) => {
            // 移动端简化日期显示
            if (isMobile.value) {
              const parts = item.date.split('-')
              return parts.length >= 2 ? `${parts[1]}-${parts[2]}` : item.date
            }
            return item.date
          }),
          datasets: [
            {
              label: '运输单数',
              data: transportTrend.map((item) => item.count),
              borderColor: primaryColor,
              backgroundColor: primaryColor + '20', // 添加半透明背景
              fill: true,
              tension: 0.4,
              pointRadius: isMobile.value ? 3 : 5,
            },
          ],
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          layout: {
            padding: {
              top: isMobile.value ? 20 : 25,
              right: isMobile.value ? 10 : 20,
              left: 5,
              bottom: 5,
            },
          },
          scales: {
            y: {
              beginAtZero: true,
              grace: '10%',
              ticks: {
                precision: 0,
                font: {
                  size: isMobile.value ? 10 : 12,
                },
              },
            },
            x: {
              ticks: {
                maxRotation: 45,
                minRotation: 0,
                font: {
                  size: isMobile.value ? 10 : 12,
                },
              },
            },
          },
          plugins: {
            legend: {
              display: false,
            },
            tooltip: {
              padding: 10,
              bodySpacing: 4,
            },
            datalabels: {
              display: !isMobile.value, // 移动端点位密集，不显示数值标签
              align: 'top',
              anchor: 'end',
              color: primaryColor,
              font: {
                weight: 'bold',
                size: 11,
              },
              formatter: (value) => value + '单',
            },
          },
        },
      })
    }
  }

  // 渲染破损分布图 (饼图)
  if (damageDistribution) {
    const ctx = document.getElementById('damageDistributionChart')
    if (ctx) {
      if (damageDistributionChart) damageDistributionChart.destroy()
      damageDistributionChart = new Chart(ctx, {
        type: 'pie',
        data: {
          labels: damageDistribution.map((item) => item.name),
          datasets: [
            {
              data: damageDistribution.map((item) => item.value),
              backgroundColor: [successColor, warningColor, dangerColor, infoColor],
            },
          ],
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: {
              position: isMobile.value ? 'bottom' : 'right',
              labels: {
                boxWidth: 12,
                padding: isMobile.value ? 10 : 20,
                font: {
                  size: isMobile.value ? 11 : 12,
                },
              },
            },
            datalabels: {
              color: '#fff',
              font: {
                weight: 'bold',
                size: isMobile.value ? 10 : 12,
              },
              formatter: (value, ctx) => {
                // 计算百分比
                let sum = 0
                let dataArr = ctx.chart.data.datasets[0].data
                dataArr.map((data) => {
                  sum += data
                })
                let percentage = ((value * 100) / sum).toFixed(0) + '%'
                // 如果值太小，不显示
                if (value === 0 || (value / sum < 0.05 && isMobile.value)) return ''
                return isMobile.value ? `${percentage}` : `${value}件\n(${percentage})`
              },
            },
          },
        },
      })
    }
  }
}

onMounted(() => {
  fetchDashboardData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.dashboard-container {
  padding: 0;
  height: 100%;
  display: flex;
  flex-direction: column;
}

:deep(.el-card) {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.dashboard-main-card {
  border: none;
  border-radius: 0;
}

:deep(.dashboard-main-card > .el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  padding: 15px;
}

.card-header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.search-bar-container {
  margin-bottom: 15px;
  flex-shrink: 0;
}

.search-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.mobile-search-bar {
  display: flex;
  gap: 10px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(1, 1fr);
  gap: 16px;
  margin-bottom: 24px;
  flex-shrink: 0;
}

@media (min-width: 768px) {
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (min-width: 1024px) {
  .stats-cards {
    grid-template-columns: repeat(4, 1fr);
  }
}

.stat-card {
  min-width: 0;
  /* Prevent grid blowout */
}

.stat-content {
  display: flex;
  align-items: center;
}

.stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px;
  border-radius: 9999px;
  margin-right: 16px;
  width: 48px;
  height: 48px;
  box-sizing: border-box;
}

.stat-icon .el-icon {
  font-size: 24px;
}

/* 图标颜色 */
.icon-blue {
  background-color: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
}

.icon-green {
  background-color: var(--el-color-success-light-9);
  color: var(--el-color-success);
}

.icon-yellow {
  background-color: var(--el-color-warning-light-9);
  color: var(--el-color-warning);
}

.icon-purple {
  background-color: var(--el-color-danger-light-9);
  color: var(--el-color-danger);
}

.stat-label {
  color: var(--text-color-regular);
  font-size: 14px;
  margin: 0 0 4px 0;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  margin: 0;
  color: var(--text-color-primary);
}

.charts-container {
  display: grid;
  grid-template-columns: 1fr;
  gap: 24px;
  margin-bottom: 24px;
}

@media (min-width: 1024px) {
  .charts-container {
    grid-template-columns: repeat(2, 1fr);
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.card-header span {
  font-weight: bold;
  white-space: nowrap;
  flex-shrink: 0;
}

.chart-wrapper {
  width: 100%;
  position: relative;
  height: 300px;
}

@media (max-width: 768px) {
  .chart-wrapper {
    height: 240px;
  }
}

.recent-activities {
  margin-top: 24px;
}
</style>
