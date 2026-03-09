import http from '@/utils/request'

// ================= 日志管理 =================

/**
 * 获取系统日志列表
 * GET /system/logs
 * @param {Object} params - 查询参数 { page, size, module, operator, status, startTime, endTime }
 */
export const getSystemLogs = (params) => http.get('/system/logs', params)

/**
 * 删除系统日志
 * DELETE /system/logs/{ids}
 * @param {Array} ids - 日志ID列表
 */
export const deleteSystemLogs = (ids) => http.delete(`/system/logs/${ids}`)

/**
 * 清空系统日志
 * DELETE /system/logs/clean
 */
export const cleanSystemLogs = () => http.delete('/system/logs/clean')

// ================= 数据备份 =================

/**
 * 获取备份列表
 * GET /system/backups
 */
export const getBackups = () => http.get('/system/backups')

/**
 * 创建新备份
 * POST /system/backups
 */
export const createBackup = () => http.post('/system/backups')

/**
 * 恢复备份
 * POST /system/backups/{id}/restore
 * @param {string|number} id - 备份ID
 */
export const restoreBackup = (id) => http.post(`/system/backups/${id}/restore`)

/**
 * 删除备份
 * DELETE /system/backups/{id}
 * @param {string|number} id - 备份ID
 */
export const deleteBackup = (id) => http.delete(`/system/backups/${id}`)

/**
 * 下载备份
 * GET /system/backups/{id}/download
 * @param {string|number} id - 备份ID
 */
export const downloadBackup = (id) => http.download(`/system/backups/${id}/download`)

// ================= 系统配置 =================

/**
 * 获取所有系统配置
 * GET /system/configs
 */
export const getSystemConfigs = () => http.get('/system/configs')

/**
 * 批量更新系统配置
 * PUT /system/configs
 * @param {Object} configs - 配置键值对对象
 */
export const updateSystemConfigs = (configs) => http.put('/system/configs', configs)
