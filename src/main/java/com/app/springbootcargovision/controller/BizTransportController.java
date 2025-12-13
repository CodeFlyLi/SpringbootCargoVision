package com.app.springbootcargovision.controller;

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
 * 货物运输记录控制器
 * 提供运输记录管理的 RESTful API 接口
 */
@Tag(name = "货物运输", description = "货物运输记录管理接口")
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
    @GetMapping
    public Result<PageInfo<BizTransport>> getTransportList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "运输单号") @RequestParam(required = false) String transportNo,
            @Parameter(description = "货物名称") @RequestParam(required = false) String goodsName,
            @Parameter(description = "运输状态") @RequestParam(required = false) Integer status) {
        return Result.success(bizTransportService.getTransportList(page, size, transportNo, goodsName, status));
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
    @PostMapping("/{id}/cancel")
    public Result<String> cancelTransport(@Parameter(description = "运输记录ID") @PathVariable Long id) {
        bizTransportService.cancelTransport(id);
        return Result.success("运单已取消");
    }
}
