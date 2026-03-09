import http from '@/utils/request'

/**
 * 获取仪表盘统计数据
 * GET /dashboard/stats
 */
export const getDashboardStats = () => http.get('/dashboard/stats')

/**
 * 获取最近的检测记录
 * 使用 /detections 接口，按时间倒序
 * GET /detections?page=1&size=5
 */
export const getRecentDetections = (limit = 5) =>
  http.get('/detections', {
    page: 1,
    size: limit,
  })

/**
 * 获取仪表盘图表数据（趋势图+分布图）
 * GET /dashboard/charts
 */
export const getDashboardCharts = () => http.get('/dashboard/charts')

/**
 * 获取破损趋势图表数据
 * GET /dashboard/chart?type=damage&period=...
 */
export const getDamageChartData = (period) =>
  http.get('/dashboard/chart', {
    type: 'damage',
    period,
  })

/**
 * 获取状态分布图表数据
 * GET /dashboard/chart?type=status&period=...
 */
export const getStatusChartData = (period) =>
  http.get('/dashboard/chart', {
    type: 'status',
    period,
  })
