export const PERMISSIONS = {
  // User Management
  USER_ADD: 'sys:user:add',
  USER_EDIT: 'sys:user:edit',
  USER_DELETE: 'sys:user:delete',
  USER_RESET: 'sys:user:reset',
  USER_QUERY: 'sys:user:query',
  // Legacy/Alternative identifiers found in code
  USER_ADD_LEGACY: 'user:add',
  USER_EDIT_LEGACY: 'user:edit',
  USER_DELETE_LEGACY: 'user:delete',
  USER_QUERY_LEGACY: 'user:query',

  // Role Management
  ROLE_ADD: 'sys:role:add',
  ROLE_EDIT: 'sys:role:edit',
  ROLE_UPDATE: 'sys:role:update',
  ROLE_DELETE: 'sys:role:delete',
  ROLE_ASSIGN: 'sys:role:assign',
  ROLE_PERMISSIONS: 'sys:role:permissions',
  ROLE_QUERY: 'sys:role:query',
  ROLE_BATCH_DELETE: 'sys:role:deletes',
  // Legacy/Alternative identifiers found in code
  ROLE_ADD_LEGACY: 'role:add',
  ROLE_DELETE_LEGACY: 'role:delete',
  ROLE_QUERY_LEGACY: 'role:query',
  ROLE_BATCH_DELETE_LEGACY: 'role:deletes',

  // Permission Management
  PERMISSION_ADD: 'sys:permission:add',
  PERMISSION_EDIT: 'sys:permission:edit',
  PERMISSION_DELETE: 'sys:permission:delete',
  PERMISSION_QUERY: 'sys:permission:query',
  PERMISSION_ADD_CHILD: 'sys:permission:addchild',
  // Legacy/Alternative identifiers found in code
  PERMISSION_ADD_LEGACY: 'permission:add',
  PERMISSION_EDIT_LEGACY: 'permission:edit',
  PERMISSION_DELETE_LEGACY: 'permission:delete',
  PERMISSION_QUERY_LEGACY: 'permission:query',
  PERMISSION_ADD_CHILD_LEGACY: 'permission:addchild',

  // Detection Management
  DETECT_ADD: 'biz:detect:add',
  DETECT_EDIT: 'biz:detect:edit', // Add Edit Permission
  DETECT_QUERY: 'biz:detect:query',
  DETECT_EXCELS: 'biz:detect:excels', // Export all/report
  DETECT_EXCEL: 'biz:detect:excel', // Export selected
  DETECT_RESET: 'biz:detect:reset',
  DETECT_START_DETECTION: 'biz:detectc:detection', // Start batch detection
  DETECT_CAMERA: 'biz:detect:camera', // Camera access
  DETECT_CONFIG: 'biz:detect:config', // Configuration (transport, location, etc.)
  DETECT_CLEAR: 'biz:detect:clear', // Clear list

  // Transport Management
  TRANS_ADD: 'biz:trans:add',
  TRANS_EDIT: 'biz:trans:edit',
  TRANS_DELETE: 'biz:trans:delete',
  TRANS_QUERY: 'biz:trans:query',
  TRANS_RESET: 'biz:trans:reset',
  TRANS_CANCEL: 'biz:trans:cancel',
  TRANS_DEPARTURE: 'biz:trans:departure',
  TRANS_ARRIVE: 'biz:trans:arrive',

  // Goods Management
  GOODS_ADD: 'biz:goods:add',
  GOODS_EDIT: 'biz:goods:edit',
  GOODS_DELETE: 'biz:goods:delete',
  GOODS_QUERY: 'biz:goods:query',
  GOODS_RESET: 'biz:goods:reset',

  // System/Admin
  ADMIN_WILDCARD: '*:*:*',

  // System Logs
  LOG_QUERY: 'sys:log:query',
  LOG_RESET: 'sys:log:reset',
  LOG_DELETE: 'sys:log:delete',
  LOG_CLEAN: 'sys:log:clean',

  //
  BACKUP_QUERY: 'sys:backup:query',
  BACKUP_ADD: 'sys:backup:add',
  BACKUP_DELETE: 'sys:backup:delete',
  BACKUP_RESTORE: 'sys:backup:restore',
  BACKUP_DOWNLOAD: 'sys:backup:download',

  // System Config
  CONFIG_QUERY: 'sys:config:query',
  CONFIG_UPDATE: 'sys:config:update',

  // Dashboard
  DASHBOARD_QUERY: 'sys:dashboard:query',

  // User Profile
  USER_PROFILE_QUERY: 'sys:user:profile:query',
  USER_PROFILE_UPDATE: 'sys:user:profile:update',
  USER_AVATAR_UPDATE: 'sys:user:avatar:update',
  USER_SETTING: 'sys:user:setting',
}
