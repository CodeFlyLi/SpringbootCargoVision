package com.app.springbootcargovision.controller;

import com.app.springbootcargovision.common.Result;
import com.app.springbootcargovision.model.DictItem;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "字典管理", description = "系统字典数据接口")
@RestController
@RequestMapping("/api/v1/dict")
public class DictController {

    @Operation(summary = "获取运输状态字典")
    @GetMapping("/transport/status")
    public Result<List<DictItem>> getTransportStatus() {
        List<DictItem> list = new ArrayList<>();
        list.add(new DictItem("待运输", 0, "info", "货物等待运输"));
        list.add(new DictItem("运输中", 1, "primary", "货物正在运输途中"));
        list.add(new DictItem("已完成", 2, "success", "货物已送达目的地"));
        list.add(new DictItem("异常", 3, "danger", "运输过程中发生异常"));
        list.add(new DictItem("已取消", 4, "info", "订单已取消"));
        return Result.success(list);
    }

    @Operation(summary = "获取破损等级字典")
    @GetMapping("/damage/levels")
    public Result<List<DictItem>> getDamageLevels() {
        List<DictItem> list = new ArrayList<>();
        list.add(new DictItem("无破损 (0)", 0, "success", "货物表面完好，无任何可见损伤"));
        list.add(new DictItem("轻微破损 (1)", 1, "info", "货物表面有轻微划痕或凹陷，不影响使用功能"));
        list.add(new DictItem("中度破损 (2)", 2, "warning", "货物有明显破损，部分功能可能受到影响"));
        list.add(new DictItem("严重破损 (3)", 3, "danger", "货物严重损坏，无法正常使用"));
        return Result.success(list);
    }

    @Operation(summary = "获取检测破损等级字典")
    @GetMapping("/detection/damage-level")
    public Result<List<DictItem>> getDamageLevel() {
        List<DictItem> list = new ArrayList<>();
        list.add(new DictItem("无破损", 0, "success", "货物表面完好，无任何可见损伤"));
        list.add(new DictItem("轻微破损", 1, "info", "货物表面有轻微划痕或凹陷，不影响使用功能"));
        list.add(new DictItem("中度破损", 2, "warning", "货物有明显破损，部分功能可能受到影响"));
        list.add(new DictItem("严重破损", 3, "danger", "货物严重损坏，无法正常使用"));
        return Result.success(list);
    }

    @Operation(summary = "获取检测状态字典")
    @GetMapping("/detection/status")
    public Result<List<DictItem>> getDetectionStatus() {
        List<DictItem> list = new ArrayList<>();
        list.add(new DictItem("待处理", 0, "warning", "检测结果待确认"));
        list.add(new DictItem("已处理", 1, "success", "检测结果已确认"));
        return Result.success(list);
    }

    @Operation(summary = "获取用户状态字典")
    @GetMapping("/user/status")
    public Result<List<DictItem>> getUserStatus() {
        List<DictItem> list = new ArrayList<>();
        list.add(new DictItem("启用", 1, "success", "账号正常使用"));
        list.add(new DictItem("禁用", 0, "danger", "账号已被禁止登录"));
        return Result.success(list);
    }
}
