package com.app.springbootcargovision.service;

import com.app.springbootcargovision.model.BaseGoods;
import com.github.pagehelper.PageInfo;

/**
 * 基础货物信息服务接口
 * 处理货物信息的增删改查业务逻辑
 */
public interface BaseGoodsService {
    
    /**
     * 分页查询货物列表
     * 
     * @param page 页码
     * @param size 每页数量
     * @param goodsNo 货物编号（可选，模糊查询）
     * @param name 货物名称（可选，模糊查询）
     * @param typeId 货物类型ID（可选，精确匹配）
     * @return 包含货物列表的分页信息对象
     */
    PageInfo<BaseGoods> getGoodsList(Integer page, Integer size, String goodsNo, String name, Long typeId);

    /**
     * 根据ID获取货物详情
     * 
     * @param id 货物ID
     * @return 货物实体对象
     */
    BaseGoods getGoodsById(Long id);

    /**
     * 创建新货物信息
     * 
     * @param goods 货物实体对象
     */
    void createGoods(BaseGoods goods);

    /**
     * 更新货物信息
     * 
     * @param goods 货物实体对象
     */
    void updateGoods(BaseGoods goods);

    /**
     * 删除货物信息
     * 逻辑删除，将 is_deleted 标记为 1
     * 
     * @param id 货物ID
     */
    void deleteGoods(Long id);
}
