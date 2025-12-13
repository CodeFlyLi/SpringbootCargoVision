package com.app.springbootcargovision.mapper;

import com.app.springbootcargovision.model.BaseGoodsType;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * 货物类型 Mapper 接口
 * 负责 BaseGoodsType 实体的数据库操作
 * 对应的 XML 配置文件: resources/mapper/BaseGoodsTypeMapper.xml
 */
@Mapper
public interface BaseGoodsTypeMapper {
    
    /**
     * 根据 ID 查询货物类型
     * @param id 类型 ID
     * @return 货物类型实体对象
     */
    BaseGoodsType selectById(Long id);
    
    /**
     * 查询所有货物类型列表
     * 用于下拉选择或分类展示
     * @return 货物类型列表
     */
    List<BaseGoodsType> selectAll();
}
