package com.app.springbootcargovision.mapper;

import com.app.springbootcargovision.model.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * 系统权限 Mapper 接口
 * 负责 SysPermission 实体的数据库操作
 * 对应的 XML 配置文件: resources/mapper/SysPermissionMapper.xml
 */
@Mapper
public interface SysPermissionMapper {

    /**
     * 新增权限
     * 
     * @param permission 权限实体
     * @return 影响行数
     */
    int insert(SysPermission permission);

    /**
     * 更新权限
     * 
     * @param permission 权限实体
     * @return 影响行数
     */
    int update(SysPermission permission);

    /**
     * 删除权限
     * 
     * @param id 权限 ID
     * @return 影响行数
     */
    int deleteById(Long id);

    /**
     * 根据 ID 查询权限
     * 
     * @param id 权限 ID
     * @return 权限实体
     */
    SysPermission selectById(Long id);

    /**
     * 根据权限标识查询权限
     * 
     * @param code 权限标识
     * @return 权限对象
     */
    SysPermission selectByCode(String code);

    /**
     * 根据条件查询权限列表
     * 
     * @param permission 查询条件
     * @return 权限列表
     */
    List<SysPermission> selectList(SysPermission permission);

    /**
     * 根据角色 ID 查询权限列表
     * 
     * @param roleId 角色 ID
     * @return 权限列表
     */
    List<SysPermission> selectByRoleId(Long roleId);

    /**
     * 根据用户 ID 查询权限列表
     * 
     * @param userId 用户 ID
     * @return 权限列表
     */
    List<SysPermission> selectByUserId(Long userId);
}
