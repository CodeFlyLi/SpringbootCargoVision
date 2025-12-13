package com.app.springbootcargovision.mapper;

import com.app.springbootcargovision.model.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 系统用户 Mapper 接口
 * 负责 SysUser 实体的数据库操作 (CRUD)
 * 对应的 XML 配置文件: resources/mapper/SysUserMapper.xml
 */
@Mapper
public interface SysUserMapper {

    /**
     * 根据 ID 查询用户
     * 
     * @param id 用户 ID
     * @return 用户实体对象
     */
    SysUser selectById(Long id);

    /**
     * 根据用户名查询用户
     * 用于登录认证
     * 
     * @param username 用户名
     * @return 用户实体对象
     */
    SysUser selectByUsername(String username);

    /**
     * 根据身份证号查询用户
     *
     * @param idCard 身份证号
     * @return 用户实体对象
     */
    SysUser selectByIdCard(String idCard);

    /**
     * 根据用户名或身份证号查询用户 (用于登录)
     *
     * @param account 用户名或身份证号
     * @return 用户实体对象
     */
    SysUser selectLoginUser(String account);

    /**
     * 根据条件查询用户列表
     * 
     * @param sysUser 查询条件封装对象
     * @return 用户列表
     */
    List<SysUser> selectList(SysUser sysUser);

    /**
     * 新增用户
     * 
     * @param sysUser 用户实体对象
     * @return 影响行数
     */
    int insert(SysUser sysUser);

    /**
     * 更新用户信息
     * 
     * @param sysUser 用户实体对象
     * @return 影响行数
     */
    int update(SysUser sysUser);

    /**
     * 批量关联用户角色
     * 
     * @param userId  用户ID
     * @param roleIds 角色ID列表
     */
    void insertUserRoles(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);

    /**
     * 删除用户关联的角色
     * 
     * @param userId 用户ID
     */
    void deleteUserRoles(Long userId);

    /**
     * 根据 ID 逻辑删除用户
     * 
     * @param id 用户 ID
     * @return 影响行数
     */
    int deleteById(Long id);

    /**
     * 更新用户状态
     * 
     * @param id     用户 ID
     * @param status 状态 (1-正常, 0-禁用)
     * @return 影响行数
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 重置用户密码
     * 
     * @param id       用户 ID
     * @param password 新密码 (加密后)
     * @return 影响行数
     */
    int resetPassword(@Param("id") Long id, @Param("password") String password);
}
