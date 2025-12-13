package com.app.springbootcargovision.mapper;

import com.app.springbootcargovision.model.BaseGoods;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * 货物基本信息 Mapper 接口
 * 负责 BaseGoods 实体的数据库操作
 * 对应的 XML 配置文件: resources/mapper/BaseGoodsMapper.xml
 */
@Mapper
public interface BaseGoodsMapper {
    
    /**
     * 根据 ID 查询货物信息
     * @param id 货物 ID
     * @return 货物实体对象
     */
    BaseGoods selectById(Long id);
    
    /**
     * 根据条件查询货物列表
     * @param goods 查询条件封装对象
     * @return 货物列表
     */
    List<BaseGoods> selectList(BaseGoods goods);
    
    /**
     * 新增货物信息
     * @param goods 货物实体对象
     * @return 影响行数
     */
    int insert(BaseGoods goods);
    
    /**
     * 更新货物信息
     * @param goods 货物实体对象
     * @return 影响行数
     */
    int update(BaseGoods goods);
    
    /**
     * 根据 ID 逻辑删除货物
     * @param id 货物 ID
     * @return 影响行数
     */
    int deleteById(Long id);
    
    /**
     * 根据货物编号查询货物信息
     * 用于校验编号唯一性或扫码查询
     * @param goodsNo 货物编号
     * @return 货物实体对象
     */
    BaseGoods selectByGoodsNo(String goodsNo);
}
