package com.app.springbootcargovision.controller;

import com.app.springbootcargovision.annotation.Log;
import com.app.springbootcargovision.common.Result;
import com.app.springbootcargovision.model.SysRole;
import com.app.springbootcargovision.service.SysRoleService;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统角色控制器
 * 提供角色管理的 API 接口，包括角色的增删改查及权限分配
 */
@RestController
@RequestMapping("/api/v1/roles")
@Tag(name = "角色管理", description = "系统角色管理接口")
public class SysRoleController {

    private final SysRoleService sysRoleService;

    public SysRoleController(SysRoleService sysRoleService) {
        this.sysRoleService = sysRoleService;
    }

    /**
     * 获取角色列表接口
     * 
     * @param page 页码
     * @param size 每页大小
     * @param name 角色名称
     * @param code 角色编码
     * @return 角色列表
     */
    @Operation(summary = "获取角色列表", description = "获取角色列表，支持根据角色名或编码模糊查询")
    @Log(module = "角色管理", type = "查询")
    @GetMapping
    public Result<PageInfo<SysRole>> getRoleList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "角色名称") @RequestParam(required = false) String name,
            @Parameter(description = "角色编码") @RequestParam(required = false) String code) {
        return Result.success(sysRoleService.getRoleList(page, size, name, code));
    }

    /**
     * 获取角色详情接口
     * 
     * @param id 角色ID
     * @return 角色详细信息
     */
    @Operation(summary = "获取角色详情", description = "根据ID获取角色详细信息")
    @GetMapping("/{id}")
    public Result<SysRole> getRoleById(@Parameter(description = "角色ID") @PathVariable Long id) {
        return Result.success(sysRoleService.getRoleById(id));
    }

    /**
     * 创建角色接口
     * 
     * @param role 角色实体对象
     * @return 成功提示
     */
    @Operation(summary = "创建角色", description = "新增角色信息")
    @Log(module = "角色管理", type = "新增")
    @PostMapping
    public Result<SysRole> createRole(@Valid @RequestBody SysRole role) {
        sysRoleService.createRole(role);
        return Result.success(role);
    }

    /**
     * 更新角色接口
     * 
     * @param id   角色ID
     * @param role 角色实体对象
     * @return 成功提示
     */
    @Operation(summary = "更新角色", description = "更新已存在的角色信息")
    @Log(module = "角色管理", type = "修改")
    @PutMapping("/{id}")
    public Result<Void> updateRole(@Parameter(description = "角色ID") @PathVariable Long id,
            @Valid @RequestBody SysRole role) {
        role.setId(id);
        sysRoleService.updateRole(role);
        return Result.success();
    }

    /**
     * 删除角色接口
     * 
     * @param id 角色ID
     * @return 成功提示
     */
    @Operation(summary = "删除角色", description = "根据ID删除角色信息")
    @Log(module = "角色管理", type = "删除")
    @DeleteMapping("/{id}")
    public Result<Void> deleteRole(@Parameter(description = "角色ID") @PathVariable Long id) {
        sysRoleService.deleteRole(id);
        return Result.success();
    }

    /**
     * 批量删除角色接口
     * 
     * @param ids 角色ID列表
     * @return 成功提示
     */
    @Operation(summary = "批量删除角色", description = "根据ID列表批量删除角色")
    @Log(module = "角色管理", type = "删除")
    @DeleteMapping
    public Result<Void> deleteRoles(@RequestBody List<Long> ids) {
        sysRoleService.deleteRoles(ids);
        return Result.success();
    }

    /**
     * 分配角色权限接口
     * 为指定角色分配一组权限
     * 
     * @param id            角色ID
     * @param permissionIds 权限ID列表
     * @return 成功提示
     */
    @Operation(summary = "分配权限给角色", description = "为角色分配一个或多个权限")
    @Log(module = "角色管理", type = "授权")
    @PostMapping("/{id}/permissions")
    public Result<Void> assignRolePermissions(@Parameter(description = "角色ID") @PathVariable Long id,
            @RequestBody List<Long> permissionIds) {
        sysRoleService.assignRolePermissions(id, permissionIds);
        return Result.success();
    }
}
