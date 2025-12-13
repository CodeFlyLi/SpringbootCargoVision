package com.app.springbootcargovision.mapper;

import com.app.springbootcargovision.model.BizTransport;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * 货物运输记录 Mapper 接口
 * 负责 BizTransport 实体的数据库操作
 * 对应的 XML 配置文件: resources/mapper/BizTransportMapper.xml
 */
@Mapper
public interface BizTransportMapper {

    /**
     * 根据 ID 查询运输记录
     * 
     * @param id 运输记录 ID
     * @return 运输记录实体对象
     */
    BizTransport selectById(Long id);

    /**
     * 根据条件查询运输记录列表
     * 
     * @param transport 查询条件封装对象
     * @return 运输记录列表
     */
    List<BizTransport> selectList(BizTransport transport);

    /**
     * 新增运输记录
     * 
     * @param transport 运输记录实体对象
     * @return 影响行数
     */
    int insert(BizTransport transport);

    /**
     * 更新运输记录
     * 
     * @param transport 运输记录实体对象
     * @return 影响行数
     */
    int update(BizTransport transport);

    /**
     * 根据 ID 逻辑删除运输记录
     * 
     * @param id 运输记录 ID
     * @return 影响行数
     */
    int deleteById(Long id);
}
