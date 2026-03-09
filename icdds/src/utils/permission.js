/**
 * 权限工具函数
 * 提供权限检查、验证等相关功能
 * 作用和流程：
 * 1. 检查用户是否拥有指定权限，指定权限是一个字符串，例如 'sys:role:add'
 * 2. 检查用户是否拥有任意一个指定权限，指定权限是一个数组，例如 ['sys:role:add', 'sys:role:edit']
 * 3. 检查用户是否拥有所有指定权限，指定权限是一个数组，例如 ['sys:role:add', 'sys:role:edit']
  *为什么要三个函数：
  * 1. 检查用户是否拥有指定权限，用于单个权限的检查
  * 2. 检查用户是否拥有任意一个指定权限，用于多个权限中任意一个的检查
  * 3. 检查用户是否拥有所有指定权限，用于多个权限中所有权限的检查
  * 每个函数都有特定的功能，分别检查用户是否拥有指定权限、任意一个指定权限、所有指定权限。
  // 每个函数都有自己的参数和返回值，分别对应不同的场景
*/

import { usePermissionStore } from '@/stores/permission'

/**
 * 检查用户是否拥有指定权限
 * @param {Array} userPermissions - 用户权限列表
 * @param {string} requiredPermission - 需要的权限
 * @returns {boolean}
 */
export function hasPermission(userPermissions, requiredPermission) {
  // 检查用户权限列表是否有效，检查用户权限列表是否存在且是否为数组类型，如果无效则返回 false
  if (!userPermissions || !Array.isArray(userPermissions)) {
    return false
  }

  // 管理员权限检查，检查用户权限列表是否包含 '*' 或 '*:*:*'，如果包含则返回 true
  // '*' 或 '*:*:*' 是管理员权限通配符，用于表示用户拥有所有权限
  if (userPermissions.includes('*') || userPermissions.includes('*:*:*')) {
    return true
  }
  // 普通权限检查，检查用户权限列表是否包含需要的权限，如果包含则返回 true
  return userPermissions.includes(requiredPermission)
}

/**
 * 检查用户是否拥有任意一个指定权限
 * @param {Array} userPermissions - 用户权限列表
 * @param {Array} requiredPermissions - 需要的权限列表
 * @returns {boolean}
 */
export function hasAnyPermission(userPermissions, requiredPermissions) {
  if (!userPermissions || !Array.isArray(userPermissions)) {
    return false
  }
  // 管理员权限检查
  if (userPermissions.includes('*') || userPermissions.includes('*:*:*')) {
    return true
  }
  // 普通权限检查
  return requiredPermissions.some((permission) => userPermissions.includes(permission))
}

/**
 * 检查用户是否拥有所有指定权限
 * @param {Array} userPermissions - 用户权限列表
 * @param {Array} requiredPermissions - 需要的权限列表
 * @returns {boolean}
 */
export function hasAllPermissions(userPermissions, requiredPermissions) {
  if (!userPermissions || !Array.isArray(userPermissions)) {
    return false
  }

  // 管理员权限检查
  if (userPermissions.includes('*') || userPermissions.includes('*:*:*')) {
    return true
  }
  // 普通权限检查
  return requiredPermissions.every((permission) => userPermissions.includes(permission))
}

/**
 * 获取权限描述
 * @param {string} permission - 权限标识
 * @returns {string}
 */
export function getPermissionDesc(permission) {
  try {
    const permissionStore = usePermissionStore()
    return permissionStore.getPermissionDesc(permission)
  } catch (e) {
    // 如果 Pinia 未激活或 store 未准备好，直接返回权限标识
    return permission
  }
}

/**
 * 过滤用户拥有的权限（排除管理员通配符）
 * @param {Array} userPermissions - 用户权限列表
 * @returns {Array}
 */
export function filterUserPermissions(userPermissions) {
  if (!userPermissions || !Array.isArray(userPermissions)) {
    return []
  }
  // 过滤管理员通配符
  return userPermissions.filter((permission) => permission !== '*' && permission !== '*:*:*')
}
