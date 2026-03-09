package com.app.springbootcargovision.controller;

import com.app.springbootcargovision.annotation.Log;
import com.app.springbootcargovision.common.Result;
import com.app.springbootcargovision.model.SysConfig;
import com.app.springbootcargovision.service.SysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统配置控制器
 * 提供系统参数配置的管理接口
 */
@Tag(name = "系统配置", description = "系统参数配置管理接口")
@RestController
@RequestMapping("/api/v1/system/configs")
public class SysConfigController {

    private final SysConfigService sysConfigService;

    public SysConfigController(SysConfigService sysConfigService) {
        this.sysConfigService = sysConfigService;
    }

    @Operation(summary = "获取所有配置", description = "获取系统所有参数配置列表")
    @GetMapping
    public Result<List<SysConfig>> getAllConfigs() {
        return Result.success(sysConfigService.getAllConfigs());
    }

    @Operation(summary = "批量更新配置", description = "批量更新系统参数配置")
    @Log(module = "系统配置", type = "修改")
    @PutMapping
    public Result<Void> updateConfigs(@RequestBody Map<String, String> configs) {
        sysConfigService.updateConfigs(configs);
        return Result.success();
    }
}
