package com.app.springbootcargovision.mapper;

import com.app.springbootcargovision.model.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 系统角色 Mapper 接口
 * 负责 SysRole 实体的数据库操作
 * 对应的 XML 配置文件: resources/mapper/SysRoleMapper.xml
 */
@Mapper
public interface SysRoleMapper {

    /**
     * 根据 ID 查询角色
     * 
     * @param id 角色 ID
     * @return 角色实体对象
     */
    SysRole selectById(Long id);

    /**
     * 根据角色名查询角色
     * 
     * @param roleName 角色名
     * @return 角色对象
     */
    SysRole selectByName(String roleName);

    /**
     * 根据角色编码查询角色
     * 
     * @param roleCode 角色编码
     * @return 角色对象
     */
    SysRole selectByCode(String roleCode);

    /**
     * 查询角色列表
     * 支持根据角色名称模糊查询
     * 
     * @param name 角色名称（可选）
     * @param code 角色编码（可选）
     * @return 角色列表
     */
    List<SysRole> selectList(@Param("name") String name, @Param("code") String code);

    /**
     * 新增角色
     * 
     * @param role 角色实体
     * @return 影响行数
     */
    int insert(SysRole role);

    /**
     * 更新角色信息
     * 
     * @param role 角色实体
     * @return 影响行数
     */
    int update(SysRole role);

    /**
     * 根据 ID 删除角色
     * 
     * @param id 角色 ID
     * @return 影响行数
     */
    int deleteById(Long id);

    /**
     * 批量添加角色权限关联
     * 
     * @param roleId        角色 ID
     * @param permissionIds 权限 ID 列表
     */
    void insertRolePermissions(@Param("roleId") Long roleId, @Param("permissionIds") List<Long> permissionIds);

    /**
     * 删除指定角色的所有权限关联
     * 
     * @param roleId 角色 ID
     */
    void deleteRolePermissions(Long roleId);

    /**
     * 批量删除角色
     * 
     * @param ids 角色ID列表
     */
    void deleteByIds(@Param("ids") List<Long> ids);

    /**
     * 批量删除角色权限关联
     * 
     * @param roleIds 角色ID列表
     */
    void deleteRolePermissionsByRoleIds(@Param("roleIds") List<Long> roleIds);

    /**
     * 批量删除用户角色关联
     * 
     * @param roleIds 角色ID列表
     */
    void deleteUserRolesByRoleIds(@Param("roleIds") List<Long> roleIds);
}
