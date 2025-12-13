package com.app.springbootcargovision.service.impl;

import com.app.springbootcargovision.mapper.DashboardMapper;
import com.app.springbootcargovision.service.DashboardService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 仪表盘数据服务实现类
 * 负责聚合和统计系统首页所需的各类图表数据
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    private final DashboardMapper dashboardMapper;

    public DashboardServiceImpl(DashboardMapper dashboardMapper) {
        this.dashboardMapper = dashboardMapper;
    }

    /**
     * 获取仪表盘所有统计数据
     * 包括运输统计、检测统计、趋势图数据和分布图数据
     * @return 包含各类统计数据的 Map
     */
    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("transportStats", dashboardMapper.getTransportStats());
        stats.put("detectionStats", dashboardMapper.getDetectionStats());
        stats.put("transportTrend", dashboardMapper.getTransportTrend());
        stats.put("detectionTrend", dashboardMapper.getDetectionTrend());
        stats.put("damageDistribution", dashboardMapper.getDamageDistribution());

        return stats;
    }

    /**
     * 获取特定类型的图表数据
     * @param type 图表类型 ("damage", "status" 等)
     * @return 图表数据 Map
     */
    @Override
    public Map<String, Object> getChartData(String type) {
        Map<String, Object> result = new HashMap<>();

        if ("damage".equals(type)) {
            result.put("data", dashboardMapper.getDamageDistribution());
        } else if ("status".equals(type)) {
            result.put("data", dashboardMapper.getTransportStatusDistribution());
        } else {
            // 如果未指定类型，返回所有图表数据
            result.put("damageChart", dashboardMapper.getDamageDistribution());
            result.put("statusChart", dashboardMapper.getTransportStatusDistribution());
        }

        return result;
    }
}
