package com.app.springbootcargovision.controller;

import com.app.springbootcargovision.model.SysPermission;
import com.app.springbootcargovision.service.SysPermissionService;
import com.app.springbootcargovision.annotation.Log;
import com.app.springbootcargovision.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统权限控制器
 * 提供权限（菜单、按钮）管理的 API 接口
 */
@RestController
@RequestMapping("/api/v1/permissions")
@Tag(name = "权限管理", description = "系统权限管理接口")
public class SysPermissionController {

    private final SysPermissionService sysPermissionService;

    public SysPermissionController(SysPermissionService sysPermissionService) {
        this.sysPermissionService = sysPermissionService;
    }

    /**
     * 获取权限树接口
     * 返回树形结构的权限列表，用于前端菜单展示
     * 
     * @return 权限树列表
     */
    @Operation(summary = "获取权限树", description = "获取所有权限的树形结构")
    @GetMapping("/tree")
    public Result<List<SysPermission>> getPermissionTree() {
        return Result.success(sysPermissionService.getPermissionTree());
    }

    /**
     * 获取权限列表接口
     * 返回扁平的权限列表，支持条件查询
     * 
     * @param permission 查询条件
     * @return 权限列表
     */
    @Operation(summary = "获取权限列表", description = "获取所有权限列表，支持根据名称(name)、标识(code)、类型(type)进行筛选")
    @GetMapping
    public Result<List<SysPermission>> getPermissionList(SysPermission permission) {
        return Result.success(sysPermissionService.getPermissionList(permission));
    }

    /**
     * 创建权限接口
     * 
     * @param permission 权限实体对象
     * @return 成功提示
     */
    @Operation(summary = "创建权限", description = "新增权限信息")
    @PostMapping
    public Result<SysPermission> createPermission(@Valid @RequestBody SysPermission permission) {
        sysPermissionService.createPermission(permission);
        return Result.success(permission);
    }

    /**
     * 更新权限接口
     * 
     * @param permission 权限实体对象
     * @return 成功提示
     */
    @Operation(summary = "更新权限", description = "更新已存在的权限信息")
    @PutMapping
    public Result<Void> updatePermission(@Valid @RequestBody SysPermission permission) {
        sysPermissionService.updatePermission(permission);
        return Result.success();
    }

    /**
     * 删除权限接口
     * 
     * @param id 权限ID
     * @return 成功提示
     */
    @Operation(summary = "删除权限", description = "根据ID删除权限信息")
    @Log(module = "权限管理", type = "删除")
    @DeleteMapping("/{id}")
    public Result<Void> deletePermission(@Parameter(description = "权限ID") @PathVariable Long id) {
        sysPermissionService.deletePermission(id);
        return Result.success();
    }
}
