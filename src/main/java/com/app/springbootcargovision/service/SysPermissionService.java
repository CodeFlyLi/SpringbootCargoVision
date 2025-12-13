package com.app.springbootcargovision.service;

import com.app.springbootcargovision.model.SysPermission;

import java.util.List;

/**
 * 权限管理服务接口
 */
public interface SysPermissionService {

    /**
     * 创建权限
     */
    void createPermission(SysPermission permission);

    /**
     * 更新权限
     */
    void updatePermission(SysPermission permission);

    /**
     * 删除权限
     */
    void deletePermission(Long id);

    /**
     * 根据ID获取权限
     */
    SysPermission getPermissionById(Long id);

    /**
     * 获取权限列表
     * 
     * @param permission 查询条件
     * @return 权限列表
     */
    List<SysPermission> getPermissionList(SysPermission permission);

    /**
     * 获取完整的权限树
     */
    List<SysPermission> getPermissionTree();

    /**
     * 根据角色ID获取权限列表
     */
    List<SysPermission> getPermissionsByRoleId(Long roleId);

    /**
     * 根据用户ID获取该用户拥有的所有权限列表（合并所有角色的权限）
     * 
     * @param userId 用户ID
     * @return 权限列表
     */
    List<SysPermission> getPermissionsByUserId(Long userId);
}
