package com.app.springbootcargovision.service;

import com.app.springbootcargovision.model.SysUser;
import com.github.pagehelper.PageInfo;

import java.util.List;

/**
 * 系统用户服务接口
 * 处理用户管理、状态变更、密码重置等业务逻辑
 */
public interface SysUserService {

    /**
     * 根据ID获取用户详情
     * 
     * @param id 用户ID
     * @return 用户实体对象
     */
    SysUser getUserById(Long id);

    /**
     * 根据用户名获取用户详情
     * 通常用于登录认证或检查用户名是否已存在
     * 
     * @param username 用户名
     * @return 用户实体对象
     */
    SysUser getUserByUsername(String username);

    /**
     * 分页查询用户列表
     * 
     * @param page     页码
     * @param size     每页数量
     * @param username 用户名（可选，模糊查询）
     * @param name     姓名（可选，模糊查询）
     * @param roleId   角色ID（可选，精确匹配）
     * @param status   用户状态（可选，精确匹配）
     * @return 包含用户列表的分页信息对象
     */
    PageInfo<SysUser> getUserList(Integer page, Integer size, String username, String name, Long roleId,
            Integer status);

    /**
     * 创建新用户
     * 
     * @param sysUser 用户实体对象
     */
    void createUser(SysUser sysUser);

    /**
     * 更新用户信息
     * 
     * @param sysUser 用户实体对象
     */
    void updateUser(SysUser sysUser);

    /**
     * 删除用户
     * 逻辑删除，将 is_deleted 标记为 1
     * 
     * @param id 用户ID
     */
    void deleteUser(Long id);

    /**
     * 更新用户状态
     * 
     * @param id     用户ID
     * @param status 新状态（1-正常，0-禁用）
     */
    void updateUserStatus(Long id, Integer status);

    /**
     * 分配角色给用户
     * 
     * @param userId  用户ID
     * @param roleIds 角色ID列表
     */
    void assignUserRoles(Long userId, List<Long> roleIds);

    /**
     * 重置用户密码
     * 
     * @param id       用户ID
     * @param password 新密码（明文，内部应加密存储）。如果为空，则使用默认密码。
     */
    void resetPassword(Long id, String password);
}
