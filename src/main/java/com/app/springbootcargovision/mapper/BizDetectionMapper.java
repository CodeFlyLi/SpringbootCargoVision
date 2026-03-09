package com.app.springbootcargovision.mapper;

import com.app.springbootcargovision.model.BizDetection;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * 货物检测记录 Mapper 接口
 * 负责 BizDetection 实体的数据库操作
 * 对应的 XML 配置文件: resources/mapper/BizDetectionMapper.xml
 */
@Mapper
public interface BizDetectionMapper {
    
    /**
     * 根据 ID 查询检测记录
     * @param id 检测记录 ID
     * @return 检测记录实体对象
     */
    BizDetection selectById(Long id);

    /**
     * 根据 ID 列表查询检测记录
     * @param ids 检测记录 ID 列表
     * @return 检测记录列表
     */
    List<BizDetection> selectByIds(@org.apache.ibatis.annotations.Param("ids") List<Long> ids);

    /**
     * 根据条件查询检测记录列表
     * @param detection 查询条件封装对象
     * @return 检测记录列表
     */
    List<BizDetection> selectList(BizDetection detection);

    /**
     * 新增检测记录
     * @param detection 检测记录实体对象
     * @return 影响行数
     */
    int insert(BizDetection detection);

    /**
     * 更新检测记录
     * @param detection 检测记录实体对象
     * @return 影响行数
     */
    int update(BizDetection detection);

    /**
     * 删除检测记录
     * @param id 检测记录 ID
     * @return 影响行数
     */
    int deleteById(Long id);
}
