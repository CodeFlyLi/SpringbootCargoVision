package com.app.springbootcargovision.service;

import com.app.springbootcargovision.model.BizTransport;
import com.github.pagehelper.PageInfo;

/**
 * 货物运输记录服务接口
 * 处理运输记录的增删改查业务逻辑
 */
public interface BizTransportService {
    
    /**
     * 分页查询运输记录列表
     * 
     * @param page 页码
     * @param size 每页数量
     * @param transportNo 运输单号（可选，模糊查询）
     * @param goodsName 货物名称（可选，模糊查询）
     * @param status 运输状态（可选，精确匹配）
     * @return 包含运输记录列表的分页信息对象
     */
    PageInfo<BizTransport> getTransportList(Integer page, Integer size, String transportNo, String goodsName, Integer status);

    /**
     * 根据ID获取运输记录详情
     * 
     * @param id 运输记录ID
     * @return 运输记录实体对象
     */
    BizTransport getTransportById(Long id);

    /**
     * 创建新运输记录
     * 
     * @param transport 运输记录实体对象
     */
    void createTransport(BizTransport transport);

    /**
     * 更新运输记录
     * 
     * @param transport 运输记录实体对象
     */
    void updateTransport(BizTransport transport);

    /**
     * 删除运输记录
     * 逻辑删除，将 is_deleted 标记为 1
     * 
     * @param id 运输记录ID
     */
    void deleteTransport(Long id);

    /**
     * 开始运输
     * 状态从 0(待运输) -> 1(运输中)
     * 
     * @param id 运输记录ID
     */
    void startTransport(Long id);

    /**
     * 完成运输
     * 状态从 1(运输中) -> 2(已送达)
     * 
     * @param id 运输记录ID
     */
    void completeTransport(Long id);

    /**
     * 取消运输
     * 状态从 0(待运输) -> 3(已取消)
     * 
     * @param id 运输记录ID
     */
    void cancelTransport(Long id);
}
