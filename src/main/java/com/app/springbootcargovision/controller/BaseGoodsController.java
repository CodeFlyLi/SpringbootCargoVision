package com.app.springbootcargovision.controller;

import com.app.springbootcargovision.common.Result;
import com.app.springbootcargovision.model.BaseGoods;
import com.app.springbootcargovision.service.BaseGoodsService;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 基础货物信息控制器
 * 提供货物管理的 RESTful API 接口
 */
@Tag(name = "货物管理", description = "基础货物信息的增删改查接口")
@RestController
@RequestMapping("/api/v1/goods")
public class BaseGoodsController {

    private final BaseGoodsService baseGoodsService;

    public BaseGoodsController(BaseGoodsService baseGoodsService) {
        this.baseGoodsService = baseGoodsService;
    }

    /**
     * 分页查询货物列表接口
     * 
     * @param page    页码（默认1）
     * @param size    每页数量（默认10）
     * @param goodsNo 货物编号（可选）
     * @param name    货物名称（可选）
     * @param typeId  货物类型ID（可选）
     * @return 货物列表分页结果
     */
    @Operation(summary = "分页查询货物列表", description = "根据条件分页查询货物信息")
    @GetMapping
    public Result<PageInfo<BaseGoods>> getGoodsList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "货物编号") @RequestParam(required = false) String goodsNo,
            @Parameter(description = "货物名称") @RequestParam(required = false) String name,
            @Parameter(description = "货物类型ID") @RequestParam(required = false) Long typeId) {
        return Result.success(baseGoodsService.getGoodsList(page, size, goodsNo, name, typeId));
    }

    /**
     * 根据ID获取货物详情接口
     * 
     * @param id 货物ID
     * @return 货物详情结果
     */
    @Operation(summary = "获取货物详情", description = "根据ID获取货物详细信息")
    @GetMapping("/{id}")
    public Result<BaseGoods> getGoodsById(@Parameter(description = "货物ID") @PathVariable Long id) {
        return Result.success(baseGoodsService.getGoodsById(id));
    }

    /**
     * 创建货物接口
     * 
     * @param goods 货物实体对象
     * @return 成功提示信息
     */
    @Operation(summary = "创建货物", description = "新增货物信息")
    @PostMapping
    public Result<String> createGoods(@Valid @RequestBody BaseGoods goods) {
        baseGoodsService.createGoods(goods);
        return Result.success("货物创建成功");
    }

    /**
     * 更新货物接口
     * 
     * @param id    货物ID
     * @param goods 货物实体对象
     * @return 成功提示信息
     */
    @Operation(summary = "更新货物", description = "更新已存在的货物信息")
    @PutMapping("/{id}")
    public Result<String> updateGoods(@Parameter(description = "货物ID") @PathVariable Long id,
            @Valid @RequestBody BaseGoods goods) {
        goods.setId(id);
        baseGoodsService.updateGoods(goods);
        return Result.success("货物更新成功");
    }

    /**
     * 删除货物接口
     * 
     * @param id 货物ID
     * @return 成功提示信息
     */
    @Operation(summary = "删除货物", description = "根据ID删除货物信息")
    @DeleteMapping("/{id}")
    public Result<String> deleteGoods(@Parameter(description = "货物ID") @PathVariable Long id) {
        baseGoodsService.deleteGoods(id);
        return Result.success("货物删除成功");
    }
}
