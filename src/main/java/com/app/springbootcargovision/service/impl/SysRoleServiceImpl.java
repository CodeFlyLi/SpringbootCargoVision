package com.app.springbootcargovision.service.impl;

import com.app.springbootcargovision.mapper.SysRoleMapper;
import com.app.springbootcargovision.model.SysRole;
import com.app.springbootcargovision.service.SysRoleService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统角色服务实现类
 * 实现角色管理的各项业务逻辑，包括角色的增删改查及权限分配
 */
@Service
public class SysRoleServiceImpl implements SysRoleService {

    private final SysRoleMapper sysRoleMapper;

    public SysRoleServiceImpl(SysRoleMapper sysRoleMapper) {
        this.sysRoleMapper = sysRoleMapper;
    }

    /**
     * 获取角色列表
     * 
     * @param page 页码
     * @param size 每页大小
     * @param name 角色名称（可选）
     * @param code 角色编码（可选）
     * @return 角色列表
     */
    @Override
    public PageInfo<SysRole> getRoleList(Integer page, Integer size, String name, String code) {
        PageHelper.startPage(page, size);
        List<SysRole> list = sysRoleMapper.selectList(name, code);
        return new PageInfo<>(list);
    }

    /**
     * 根据ID获取角色详情
     * 
     * @param id 角色ID
     * @return 角色实体对象
     */
    @Override
    public SysRole getRoleById(Long id) {
        return sysRoleMapper.selectById(id);
    }

    /**
     * 创建新角色
     * 
     * @param role 角色实体对象
     */
    @Override
    @Transactional
    public void createRole(SysRole role) {
        // 唯一性校验
        if (sysRoleMapper.selectByName(role.getRoleName()) != null) {
            throw new RuntimeException("角色名称已存在");
        }
        if (sysRoleMapper.selectByCode(role.getRoleCode()) != null) {
            throw new RuntimeException("角色编码已存在");
        }
        sysRoleMapper.insert(role);
    }

    /**
     * 更新角色信息
     * 
     * @param role 角色实体对象
     */
    @Override
    @Transactional
    public void updateRole(SysRole role) {
        // 唯一性校验
        SysRole existingName = sysRoleMapper.selectByName(role.getRoleName());
        if (existingName != null && !existingName.getId().equals(role.getId())) {
            throw new RuntimeException("角色名称已存在");
        }
        SysRole existingCode = sysRoleMapper.selectByCode(role.getRoleCode());
        if (existingCode != null && !existingCode.getId().equals(role.getId())) {
            throw new RuntimeException("角色编码已存在");
        }
        sysRoleMapper.update(role);
    }

    /**
     * 删除角色
     * 
     * @param id 角色ID
     */
    @Override
    @Transactional
    public void deleteRole(Long id) {
        sysRoleMapper.deleteById(id);
    }

    /**
     * 批量删除角色
     * 
     * @param ids 角色ID列表
     */
    @Override
    @Transactional
    public void deleteRoles(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        // 先删除用户角色关联
        sysRoleMapper.deleteUserRolesByRoleIds(ids);
        // 再删除角色权限关联
        sysRoleMapper.deleteRolePermissionsByRoleIds(ids);
        // 最后删除角色
        sysRoleMapper.deleteByIds(ids);
    }

    /**
     * 分配角色权限
     * 先删除该角色原有的所有权限关联，再批量插入新的权限关联
     * 
     * @param roleId        角色ID
     * @param permissionIds 权限ID列表
     */
    @Override
    @Transactional
    public void assignRolePermissions(Long roleId, List<Long> permissionIds) {
        // 先删除旧的权限关联
        sysRoleMapper.deleteRolePermissions(roleId);
        // 如果有新权限，则添加关联
        if (permissionIds != null && !permissionIds.isEmpty()) {
            // 去重，防止数据库唯一键冲突
            List<Long> distinctPermissionIds = permissionIds.stream().distinct().collect(Collectors.toList());
            sysRoleMapper.insertRolePermissions(roleId, distinctPermissionIds);
        }
    }
}
