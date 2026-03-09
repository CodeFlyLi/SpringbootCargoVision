package com.app.springbootcargovision.controller;

import java.util.List;

import com.app.springbootcargovision.annotation.Log;
import com.app.springbootcargovision.common.Result;
import com.app.springbootcargovision.model.SysLog;
import com.app.springbootcargovision.service.SysLogService;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * 系统日志控制器
 */
@Tag(name = "系统日志", description = "系统日志查询接口")
@RestController
@RequestMapping("/api/v1/system/logs")
public class SysLogController {

    private final SysLogService sysLogService;

    public SysLogController(SysLogService sysLogService) {
        this.sysLogService = sysLogService;
    }

    @Operation(summary = "查询日志列表", description = "分页查询系统日志")
    @GetMapping
    public Result<PageInfo<SysLog>> getLogList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "模块") @RequestParam(required = false) String module,
            @Parameter(description = "操作人") @RequestParam(required = false) String operator,
            @Parameter(description = "状态") @RequestParam(required = false) String status,
            @Parameter(description = "时间范围起") @RequestParam(required = false) String startTime,
            @Parameter(description = "时间范围止") @RequestParam(required = false) String endTime) {
        return Result.success(sysLogService.getLogList(page, size, module, operator, status, startTime, endTime));
    }

    @Operation(summary = "删除日志", description = "根据ID列表删除日志")
    @DeleteMapping("/{ids}")
    @Log(module = "系统日志", type = "删除")
    public Result<Void> deleteLog(@PathVariable List<Long> ids) {
        sysLogService.deleteLogByIds(ids);
        return Result.success();
    }

    @Operation(summary = "清空日志", description = "清空所有日志")
    @DeleteMapping("/clean")
    @Log(module = "系统日志", type = "清空")
    public Result<Void> cleanLog() {
        sysLogService.cleanLog();
        return Result.success();
    }
}
