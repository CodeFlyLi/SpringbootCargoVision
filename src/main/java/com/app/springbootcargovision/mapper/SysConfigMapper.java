package com.app.springbootcargovision.mapper;

import com.app.springbootcargovision.model.SysConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 系统配置 Mapper 接口
 */
@Mapper
public interface SysConfigMapper {
    /**
     * 根据键查询配置
     */
    SysConfig selectByKey(@Param("configKey") String configKey);

    /**
     * 查询所有配置
     */
    List<SysConfig> selectAll();

    /**
     * 更新配置
     */
    int update(SysConfig config);

    /**
     * 插入配置 (如果不存在)
     */
    int insert(SysConfig config);
}
