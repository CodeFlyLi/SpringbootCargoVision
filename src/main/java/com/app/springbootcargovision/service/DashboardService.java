package com.app.springbootcargovision.service;

import java.util.Map;

/**
 * 仪表盘服务接口
 * 提供系统首页展示所需的统计数据
 */
public interface DashboardService {

    /**
     * 获取仪表盘所有统计数据
     * @return 包含各项统计指标的 Map
     */
    Map<String, Object> getDashboardStats();

    /**
     * 获取指定类型的图表数据
     * @param type 图表类型 (如 damage, status)
     * @return 图表数据 Map
     */
    Map<String, Object> getChartData(String type);
}
