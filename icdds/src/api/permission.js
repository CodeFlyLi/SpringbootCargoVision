import http from '@/utils/request'

/**
 * 获取权限列表
 * GET /permissions
 * @param {Object} params - 查询参数
 */
export const getPermissions = (params) => http.get('/permissions', params)

/**
 * 获取权限树
 * GET /permissions/tree
 * @param {Object} params - 查询参数 (如 name)
 */
export const getPermissionTree = (params) => http.get('/permissions/tree', params)

/**
 * 创建权限
 * POST /permissions
 * @param {Object} data - 权限信息
 */
export const createPermission = (data) => http.post('/permissions', data)

/**
 * 更新权限
 * PUT /permissions
 * @param {Object} data - 更新信息 (必须包含 id)
 */
export const updatePermission = (data) => http.put('/permissions', data)

/**
 * 删除权限
 * DELETE /permissions/{id}
 * @param {string|number} id - 权限ID
 */
export const deletePermission = (id) => http.delete(`/permissions/${id}`)
