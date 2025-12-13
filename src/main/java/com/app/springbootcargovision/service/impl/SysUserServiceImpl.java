package com.app.springbootcargovision.service.impl;

import com.app.springbootcargovision.common.SecurityConstants;
import com.app.springbootcargovision.mapper.SysPermissionMapper;
import com.app.springbootcargovision.mapper.SysRoleMapper;
import com.app.springbootcargovision.mapper.SysUserMapper;
import com.app.springbootcargovision.model.SysPermission;
import com.app.springbootcargovision.model.SysUser;
import com.app.springbootcargovision.service.SysUserService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统用户服务实现类
 * 实现用户管理的各项业务逻辑，包含事务管理
 */
@Service
public class SysUserServiceImpl implements SysUserService {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysPermissionMapper sysPermissionMapper;
    private final PasswordEncoder passwordEncoder;

    public SysUserServiceImpl(SysUserMapper sysUserMapper, SysRoleMapper sysRoleMapper,
            SysPermissionMapper sysPermissionMapper, PasswordEncoder passwordEncoder) {
        this.sysUserMapper = sysUserMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysPermissionMapper = sysPermissionMapper;
        this.passwordEncoder = passwordEncoder;
    }

    // 根据ID查询用户实现
    @Override
    public SysUser getUserById(Long id) {
        return sysUserMapper.selectById(id);
    }

    // 根据用户名查询用户实现
    @Override
    public SysUser getUserByUsername(String username) {
        SysUser user = sysUserMapper.selectByUsername(username);
        if (user != null) {
            List<SysPermission> permissions = sysPermissionMapper.selectByUserId(user.getId());
            List<String> permissionCodes = permissions.stream()
                    .map(SysPermission::getCode)
                    .filter(code -> code != null && !code.isEmpty())
                    .toList();
            user.setPermissions(permissionCodes);
        }
        return user;
    }

    /**
     * 分页查询用户列表实现
     */
    @Override
    public PageInfo<SysUser> getUserList(Integer page, Integer size, String username, String name, Long roleId,
            Integer status) {
        PageHelper.startPage(page, size);
        SysUser query = new SysUser();
        query.setUsername(username);
        query.setName(name);
        query.setRoleId(roleId);
        query.setStatus(status);
        List<SysUser> list = sysUserMapper.selectList(query);
        return new PageInfo<>(list);
    }

    /**
     * 创建用户实现
     * 包含业务校验：检查用户名是否已存在
     */
    @Override
    @Transactional
    public void createUser(SysUser sysUser) {
        // 校验用户名唯一性
        if (sysUserMapper.selectByUsername(sysUser.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 校验身份证号唯一性
        if (sysUser.getIdCard() != null && !sysUser.getIdCard().isEmpty()) {
            if (sysUserMapper.selectByIdCard(sysUser.getIdCard()) != null) {
                throw new RuntimeException("身份证号已存在");
            }
        }

        // 密码加密
        if (sysUser.getPassword() != null) {
            sysUser.setPassword(passwordEncoder.encode(sysUser.getPassword()));
        }

        sysUserMapper.insert(sysUser);

        // 如果提供了角色ID，则分配角色
        if (sysUser.getRoleId() != null) {
            List<Long> roleIds = new ArrayList<>();
            roleIds.add(sysUser.getRoleId());
            assignUserRoles(sysUser.getId(), roleIds);
        }
    }

    // 更新用户实现
    @Override
    @Transactional
    public void updateUser(SysUser sysUser) {
        // 如果要修改用户名，需要校验唯一性
        if (sysUser.getUsername() != null && !sysUser.getUsername().isEmpty()) {
            SysUser existingUser = sysUserMapper.selectByUsername(sysUser.getUsername());
            // 如果用户名已存在，且不是当前用户自己，则报错
            if (existingUser != null && !existingUser.getId().equals(sysUser.getId())) {
                throw new RuntimeException("用户名已存在");
            }
        }

        // 如果要修改身份证号，需要校验唯一性
        if (sysUser.getIdCard() != null && !sysUser.getIdCard().isEmpty()) {
            SysUser existingUser = sysUserMapper.selectByIdCard(sysUser.getIdCard());
            // 如果身份证号已存在，且不是当前用户自己，则报错
            if (existingUser != null && !existingUser.getId().equals(sysUser.getId())) {
                throw new RuntimeException("身份证号已存在");
            }
        }

        sysUserMapper.update(sysUser);
    }

    // 删除用户实现
    @Override
    @Transactional
    public void deleteUser(Long id) {
        sysUserMapper.deleteById(id);
    }

    // 更新用户状态实现
    @Override
    @Transactional
    public void updateUserStatus(Long id, Integer status) {
        int rows = sysUserMapper.updateStatus(id, status);
        System.out
                .println("DEBUG: Executed updateStatus for user " + id + " to " + status + ". Rows affected: " + rows);
        if (rows == 0) {
            System.err.println("WARNING: Update status failed, user " + id + " not found or status not changed.");
        }
    }

    // 重置用户密码实现
    @Override
    @Transactional
    public void resetPassword(Long id, String password) {
        // 如果未提供密码，则使用默认密码
        if (password == null || password.trim().isEmpty()) {
            password = SecurityConstants.DEFAULT_PASSWORD;
        }
        sysUserMapper.resetPassword(id, passwordEncoder.encode(password));
    }

    /**
     * 分配用户角色实现
     * 1. 先删除用户旧的角色关联
     * 2. 如果有新角色，添加关联
     * 3. 注意：如果角色列表为空，也会删除旧关联（保持用户无角色）
     */
    @Override
    @Transactional
    public void assignUserRoles(Long userId, List<Long> roleIds) {
        // 先删除旧的关联
        sysUserMapper.deleteUserRoles(userId);
        // 如果有新角色，则添加关联
        if (roleIds != null && !roleIds.isEmpty()) {
            // 1. 去重 (防止前端传递重复ID导致数据库主键冲突)
            List<Long> distinctRoleIds = roleIds.stream().distinct().collect(Collectors.toList());

            // 2. 校验角色是否存在 (防止外键约束报错)
            List<Long> validRoleIds = new ArrayList<>();
            for (Long roleId : distinctRoleIds) {
                if (sysRoleMapper.selectById(roleId) != null) {
                    validRoleIds.add(roleId);
                } else {
                    System.err.println("WARNING: Ignored non-existent role ID: " + roleId + " for user " + userId);
                }
            }

            // 3. 只有存在有效角色时才插入
            if (!validRoleIds.isEmpty()) {
                sysUserMapper.insertUserRoles(userId, validRoleIds);
            }
        }
    }
}
