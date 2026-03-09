import http from '@/utils/request'

/**
 * 获取用户列表
 * @param {Object} params - 查询参数 { page, size, username, role }
 */
export const getUsers = (params) => http.get('/users', params)

/**
 * 创建新用户
 * @param {Object} data - 用户信息 { username, password, role, ... }
 */
export const createUser = (data) => http.post('/users', data)

/**
 * 更新用户信息
 * @param {string|number} id - 用户ID
 * @param {Object} data - 更新的信息
 */
export const updateUser = (id, data) => http.put(`/users/${id}`, data)

/**
 * 删除用户
 * @param {string|number} id - 用户ID
 */
export const deleteUser = (id) => http.delete(`/users/${id}`)

/**
 * 更新用户状态
 * @param {string|number} id - 用户ID
 * @param {string|number} status - 新状态
 */
export const updateUserStatus = (id, status) => http.patch(`/users/${id}/status`, { status })

/**
 * 重置用户密码
 * @param {string|number} id - 用户ID
 * @param {string} [password] - 新密码 (可选，如果不提供，默认重置为 123456)
 */
export const resetPassword = (id, password) =>
  http.patch(`/users/${id}/reset-password`, { password })

/**
 * 分配角色给用户
 * @param {string|number} id - 用户ID
 * @param {Array} roleIds - 角色ID列表
 */
export const assignRoles = (id, roleIds) => http.post(`/users/${id}/roles`, roleIds)
