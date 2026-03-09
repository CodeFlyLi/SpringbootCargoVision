package com.app.springbootcargovision.service.impl;

import com.app.springbootcargovision.mapper.DashboardMapper;
import com.app.springbootcargovision.service.DashboardService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
     * 
     * @return 包含各类统计数据的 Map
     */
    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("transportStats", dashboardMapper.getTransportStats());
        stats.put("detectionStats", dashboardMapper.getDetectionStats());

        // 处理近7天趋势数据，补全缺失日期
        List<Map<String, Object>> rawTrend = dashboardMapper.getTransportTrend();
        List<Map<String, Object>> filledTrend = fillMissingDates(rawTrend, 7);
        stats.put("transportTrend", filledTrend);

        stats.put("detectionTrend", dashboardMapper.getDetectionTrend());
        stats.put("damageDistribution", dashboardMapper.getDamageDistribution());

        return stats;
    }

    /**
     * 补全缺失的日期数据
     * 
     * @param dataList 原始数据列表
     * @param days     往前推的天数
     * @return 补全后的数据列表
     */
    private List<Map<String, Object>> fillMissingDates(List<Map<String, Object>> dataList, int days) {
        Map<String, Map<String, Object>> dateMap = new HashMap<>();
        if (dataList != null) {
            for (Map<String, Object> item : dataList) {
                String date = (String) item.get("date");
                if (date != null) {
                    dateMap.put(date, item);
                }
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 从6天前到今天（共7天）
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateStr = date.format(formatter);

            if (dateMap.containsKey(dateStr)) {
                result.add(dateMap.get(dateStr));
            } else {
                Map<String, Object> emptyItem = new HashMap<>();
                emptyItem.put("date", dateStr);
                emptyItem.put("count", 0);
                result.add(emptyItem);
            }
        }
        return result;
    }

    /**
     * 获取特定类型的图表数据
     * 
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
