package com.app.springbootcargovision.mapper;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

/**
 * 仪表盘数据 Mapper 接口
 * 负责统计数据的查询操作
 * 对应的 XML 配置文件: resources/mapper/DashboardMapper.xml
 */
@Mapper
public interface DashboardMapper {
    /**
     * 获取运输统计数据
     * 包括总运单数、运输中数量、已完成数量等
     * @return 统计数据 Map
     */
    Map<String, Object> getTransportStats();

    /**
     * 获取检测统计数据
     * 包括总检测数、破损数量、严重破损数量等
     * @return 统计数据 Map
     */
    Map<String, Object> getDetectionStats();

    /**
     * 获取运输趋势数据
     * 近7天或近30天的运输量变化趋势
     * @return 趋势数据列表
     */
    List<Map<String, Object>> getTransportTrend();

    /**
     * 获取检测趋势数据
     * 近7天或近30天的检测量变化趋势
     * @return 趋势数据列表
     */
    List<Map<String, Object>> getDetectionTrend();

    /**
     * 获取货物破损分布数据
     * 各类破损类型或等级的分布情况
     * @return 分布数据列表
     */
    List<Map<String, Object>> getDamageDistribution();

    /**
     * 获取运输状态分布数据
     * 各状态（运输中、已完成、异常等）的分布情况
     * @return 分布数据列表
     */
    List<Map<String, Object>> getTransportStatusDistribution();
}
