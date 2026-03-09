package com.app.springbootcargovision.service.impl;

import com.app.springbootcargovision.mapper.SysPermissionMapper;
import com.app.springbootcargovision.model.SysPermission;
import com.app.springbootcargovision.service.SysPermissionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 系统权限服务实现类
 * 实现权限（菜单/按钮）的增删改查及树形结构构建
 */
@Service
public class SysPermissionServiceImpl implements SysPermissionService {

    private final SysPermissionMapper sysPermissionMapper;

    public SysPermissionServiceImpl(SysPermissionMapper sysPermissionMapper) {
        this.sysPermissionMapper = sysPermissionMapper;
    }

    /**
     * 创建权限
     * 
     * @param permission 权限实体对象
     */
    @Override
    @Transactional
    public void createPermission(SysPermission permission) {
        // 唯一性校验
        if (sysPermissionMapper.selectByCode(permission.getCode()) != null) {
            throw new RuntimeException("权限标识已存在");
        }
        sysPermissionMapper.insert(permission);
    }

    /**
     * 更新权限
     * 
     * @param permission 权限实体对象
     */
    @Override
    @Transactional
    public void updatePermission(SysPermission permission) {
        // 唯一性校验
        SysPermission existing = sysPermissionMapper.selectByCode(permission.getCode());
        if (existing != null && !existing.getId().equals(permission.getId())) {
            throw new RuntimeException("权限标识已存在");
        }
        sysPermissionMapper.update(permission);
    }

    /**
     * 删除权限
     * 
     * @param id 权限ID
     */
    @Override
    @Transactional
    public void deletePermission(Long id) {
        sysPermissionMapper.deleteById(id);
    }

    /**
     * 根据ID获取权限详情
     * 
     * @param id 权限ID
     * @return 权限实体对象
     */
    @Override
    public SysPermission getPermissionById(Long id) {
        return sysPermissionMapper.selectById(id);
    }

    /**
     * 获取权限列表
     * 
     * @param permission 查询条件
     * @return 权限列表
     */
    @Override
    public List<SysPermission> getPermissionList(SysPermission permission) {
        return sysPermissionMapper.selectList(permission);
    }

    /**
     * 获取权限树形结构
     * 将扁平的权限列表转换为树形结构，便于前端展示菜单树
     * 
     * @return 权限树列表
     */
    @Override
    public List<SysPermission> getPermissionTree() {
        List<SysPermission> allPermissions = sysPermissionMapper.selectList(new SysPermission());
        return buildTree(allPermissions);
    }

    /**
     * 根据用户ID获取该用户的权限树
     * 
     * @param userId 用户ID
     * @return 权限树列表
     */
    @Override
    public List<SysPermission> getUserPermissionTree(Long userId) {
        List<SysPermission> userPermissions = sysPermissionMapper.selectByUserId(userId);
        return buildTree(userPermissions);
    }

    /**
     * 根据角色ID获取该角色拥有的权限列表
     * 
     * @param roleId 角色ID
     * @return 权限列表
     */
    @Override
    public List<SysPermission> getPermissionsByRoleId(Long roleId) {
        return sysPermissionMapper.selectByRoleId(roleId);
    }

    /**
     * 根据用户ID获取该用户拥有的所有权限列表（合并所有角色的权限）
     * 
     * @param userId 用户ID
     * @return 权限列表
     */
    @Override
    public List<SysPermission> getPermissionsByUserId(Long userId) {
        return sysPermissionMapper.selectByUserId(userId);
    }

    /**
     * 构建权限树的内部辅助方法
     * 
     * @param permissions 所有权限列表
     * @return 树形结构的权限列表
     */
    private List<SysPermission> buildTree(List<SysPermission> permissions) {
        List<SysPermission> tree = new ArrayList<>();
        // 按 parentId 分组
        Map<Long, List<SysPermission>> childrenMap = permissions.stream()
                .filter(p -> p.getParentId() != null)
                .collect(Collectors.groupingBy(SysPermission::getParentId));

        // 查找根节点 (parentId 为 0 或 null)
        for (SysPermission p : permissions) {
            if (p.getParentId() == null || p.getParentId() == 0) {
                p.setChildren(getChildren(p.getId(), childrenMap));
                tree.add(p);
            }
        }
        return tree;
    }

    private List<SysPermission> getChildren(Long parentId, Map<Long, List<SysPermission>> childrenMap) {
        List<SysPermission> children = childrenMap.get(parentId);
        if (children != null) {
            for (SysPermission child : children) {
                child.setChildren(getChildren(child.getId(), childrenMap));
            }
        }
        return children;
    }
}
