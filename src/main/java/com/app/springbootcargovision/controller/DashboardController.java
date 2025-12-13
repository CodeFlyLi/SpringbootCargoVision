package com.app.springbootcargovision.controller;

import com.app.springbootcargovision.common.Result;
import com.app.springbootcargovision.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 仪表盘控制器
 * 提供系统首页所需的统计数据和图表数据 API 接口
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "仪表盘", description = "首页统计数据接口")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * 获取统计数据接口
     * 返回运输和检测的聚合统计信息，用于首页概览展示
     * @return 统计数据 Map
     */
    @Operation(summary = "获取统计数据", description = "获取运输和检测的聚合统计数据")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        return Result.success(dashboardService.getDashboardStats());
    }

    /**
     * 获取图表数据接口
     * 根据类型获取特定的图表数据（如破损情况分布、运输状态分布）
     * @param type 图表类型（可选）
     * @return 图表数据 Map
     */
    @Operation(summary = "获取图表数据", description = "获取特定图表的数据（破损情况或状态分布）")
    @GetMapping("/charts")
    public Result<Map<String, Object>> getCharts(@Parameter(description = "图表类型") @RequestParam(required = false) String type) {
        return Result.success(dashboardService.getChartData(type));
    }
}
