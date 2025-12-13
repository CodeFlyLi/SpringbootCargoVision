package com.app.springbootcargovision.service;

import com.app.springbootcargovision.model.SysRole;
import com.github.pagehelper.PageInfo;
import java.util.List;

/**
 * 系统角色服务接口
 * 定义角色管理的相关操作
 */
public interface SysRoleService {
    /**
     * 获取角色列表
     * 
     * @param page 页码
     * @param size 每页大小
     * @param name 角色名称（可选）
     * @param code 角色编码（可选）
     * @return 角色列表
     */
    PageInfo<SysRole> getRoleList(Integer page, Integer size, String name, String code);

    /**
     * 根据ID获取角色
     * 
     * @param id 角色ID
     * @return 角色对象
     */
    SysRole getRoleById(Long id);

    /**
     * 创建角色
     * 
     * @param role 角色对象
     */
    void createRole(SysRole role);

    /**
     * 更新角色
     * 
     * @param role 角色对象
     */
    void updateRole(SysRole role);

    /**
     * 删除角色
     * 
     * @param id 角色ID
     */
    void deleteRole(Long id);

    /**
     * 批量删除角色
     * 
     * @param ids 角色ID列表
     */
    void deleteRoles(List<Long> ids);

    /**
     * 分配权限给角色
     * 
     * @param roleId        角色ID
     * @param permissionIds 权限ID列表
     */
    void assignRolePermissions(Long roleId, List<Long> permissionIds);
}
