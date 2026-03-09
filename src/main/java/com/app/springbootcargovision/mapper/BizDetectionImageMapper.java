package com.app.springbootcargovision.mapper;

import com.app.springbootcargovision.model.BizDetectionImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 货物检测图片 Mapper 接口
 * 负责 BizDetectionImage 实体的数据库操作
 */
@Mapper
public interface BizDetectionImageMapper {

    /**
     * 新增检测图片
     */
    int insert(BizDetectionImage image);

    /**
     * 批量新增检测图片
     */
    int insertBatch(@Param("list") List<BizDetectionImage> list);

    /**
     * 根据检测记录ID查询图片列表
     */
    List<BizDetectionImage> selectByDetectionId(Long detectionId);

    /**
     * 根据检测记录ID删除图片
     */
    int deleteByDetectionId(Long detectionId);

    /**
     * 更新检测图片信息
     */
    int update(BizDetectionImage image);

    /**
     * 根据ID查询
     */
    BizDetectionImage selectById(Long id);

    /**
     * 查询列表
     */
    List<BizDetectionImage> selectList(BizDetectionImage query);
}
