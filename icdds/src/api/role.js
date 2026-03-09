import http from '@/utils/request'

/**
 * 获取角色列表
 * @param {Object} params - 查询参数
 */
export const getRoles = (params) => http.get('/roles', params)

/**
 * 获取角色详情
 * @param {string|number} id - 角色ID
 */
export const getRoleDetail = (id) => http.get(`/roles/${id}`)

/**
 * 创建角色
 * @param {Object} data - 角色信息
 */
export const createRole = (data) => http.post('/roles', data)

/**
 * 更新角色
 * @param {string|number} id - 角色ID
 * @param {Object} data - 更新的信息
 */
export const updateRole = (id, data) => http.put(`/roles/${id}`, data)

/**
 * 删除角色
 * @param {string|number} id - 角色ID
 */
export const deleteRole = (id) => http.delete(`/roles/${id}`)

/**
 * 批量删除角色
 * @param {Array} ids - 角色ID数组
 */
export const deleteRoles = (ids) => http.delete('/roles', ids)

/**
 * 分配权限给角色
 * @param {string|number} id - 角色ID
 * @param {Array} permissionIds - 权限ID列表
 */
export const assignPermissions = (id, permissionIds) =>
  http.post(`/roles/${id}/permissions`, permissionIds)
