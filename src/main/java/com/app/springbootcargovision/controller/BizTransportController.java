package com.app.springbootcargovision.controller;

import com.app.springbootcargovision.annotation.Log;
import com.app.springbootcargovision.common.Result;
import com.app.springbootcargovision.model.BizTransport;
import com.app.springbootcargovision.service.BizTransportService;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 货物运输记录管理控制器 (Controller Layer)
 *
 * 【架构定位】
 * 本类属于 Web 层（Controller），负责处理与“货物运输 (Transport)”相关的所有 HTTP 请求。
 * 它是系统最基础的业务模块，因为所有的“破损检测”都必须基于一个已存在的“运输单”。
 *
 * 【核心功能】
 * 1. 基础 CRUD：运输单的增删改查。
 * 2. 状态流转：
 * - 开始运输 (Start): 待运输 -> 运输中
 * - 完成运输 (Complete): 运输中 -> 已送达
 * - 取消运输 (Cancel): 待运输 -> 已取消
 * 3. 业务查询：支持按运单号、货物名称、客户名称等多维度组合查询。
 *
 * 【开发规范】
 * - 路径前缀: /api/v1/transports
 * - 权限控制: 结合 Spring Security 和自定义 @Log 注解实现审计。
 */
@Tag(name = "货物运输管理", description = "提供货物运输全流程管理接口，包括运单创建、状态流转、详情查询等")
@RestController
@RequestMapping("/api/v1/transports")
public class BizTransportController {

    private final BizTransportService bizTransportService;

    public BizTransportController(BizTransportService bizTransportService) {
        this.bizTransportService = bizTransportService;
    }

    /**
     * 分页查询运输记录接口
     * 
     * @param page        页码（默认1）
     * @param size        每页数量（默认10）
     * @param transportNo 运输单号（可选）
     * @param goodsName   货物名称（可选）
     * @param status      运输状态（可选）
     * @return 运输记录列表分页结果
     */
    @Operation(summary = "分页查询运输记录", description = "根据条件分页查询运输记录信息")
    @Log(module = "运输管理", type = "查询")
    @GetMapping
    public Result<PageInfo<BizTransport>> getTransportList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "运输单号") @RequestParam(required = false) String transportNo,
            @Parameter(description = "货物名称") @RequestParam(required = false) String goodsName,
            @Parameter(description = "运输状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "客户名称") @RequestParam(required = false) String customerName) {
        return Result.success(
                bizTransportService.getTransportList(page, size, transportNo, goodsName, status, customerName));
    }

    /**
     * 根据ID获取运输记录详情接口
     * 
     * @param id 运输记录ID
     * @return 运输记录详情结果
     */
    @Operation(summary = "获取运输记录详情", description = "根据ID获取运输记录详细信息")
    @GetMapping("/{id}")
    public Result<BizTransport> getTransportById(@Parameter(description = "运输记录ID") @PathVariable Long id) {
        return Result.success(bizTransportService.getTransportById(id));
    }

    /**
     * 创建运输记录接口
     * 
     * @param transport 运输记录实体对象
     * @return 成功提示信息
     */
    @Operation(summary = "创建运输记录", description = "新增运输记录信息")
    @Log(module = "运输管理", type = "新增")
    @PostMapping
    public Result<String> createTransport(@Valid @RequestBody BizTransport transport) {
        bizTransportService.createTransport(transport);
        return Result.success("运输记录创建成功");
    }

    /**
     * 更新运输记录接口
     * 
     * @param id        运输记录ID
     * @param transport 运输记录实体对象
     * @return 成功提示信息
     */
    @Operation(summary = "更新运输记录", description = "更新已存在的运输记录信息")
    @Log(module = "运输管理", type = "修改")
    @PutMapping("/{id}")
    public Result<String> updateTransport(@Parameter(description = "运输记录ID") @PathVariable Long id,
            @Valid @RequestBody BizTransport transport) {
        transport.setId(id);
        bizTransportService.updateTransport(transport);
        return Result.success("运输记录更新成功");
    }

    /**
     * 删除运输记录接口
     * 
     * @param id 运输记录ID
     * @return 成功提示信息
     */
    @Operation(summary = "删除运输记录", description = "根据ID删除运输记录信息")
    @Log(module = "运输管理", type = "删除")
    @DeleteMapping("/{id}")
    public Result<String> deleteTransport(@Parameter(description = "运输记录ID") @PathVariable Long id) {
        bizTransportService.deleteTransport(id);
        return Result.success("运输记录删除成功");
    }

    /**
     * 开始运输接口
     * 
     * @param id 运输记录ID
     * @return 成功提示信息
     */
    @Operation(summary = "开始运输", description = "将运单状态从[待运输]更新为[运输中]")
    @Log(module = "运输管理", type = "状态变更")
    @PostMapping("/{id}/start")
    public Result<String> startTransport(@Parameter(description = "运输记录ID") @PathVariable Long id) {
        bizTransportService.startTransport(id);
        return Result.success("运单已开始运输");
    }

    /**
     * 完成运输接口
     * 
     * @param id 运输记录ID
     * @return 成功提示信息
     */
    @Operation(summary = "完成运输", description = "将运单状态从[运输中]更新为[已送达]")
    @Log(module = "运输管理", type = "状态变更")
    @PostMapping("/{id}/complete")
    public Result<String> completeTransport(@Parameter(description = "运输记录ID") @PathVariable Long id) {
        bizTransportService.completeTransport(id);
        return Result.success("运单已完成");
    }

    /**
     * 取消运输接口
     * 
     * @param id 运输记录ID
     * @return 成功提示信息
     */
    @Operation(summary = "取消运输", description = "将运单状态从[待运输]更新为[已取消]")
    @Log(module = "运输管理", type = "状态变更")
    @PostMapping("/{id}/cancel")
    public Result<String> cancelTransport(@Parameter(description = "运输记录ID") @PathVariable Long id) {
        bizTransportService.cancelTransport(id);
        return Result.success("运单已取消");
    }
}
