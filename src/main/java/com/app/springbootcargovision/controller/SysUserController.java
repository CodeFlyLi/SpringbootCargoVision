package com.app.springbootcargovision.controller;

import com.app.springbootcargovision.common.Result;
import com.app.springbootcargovision.model.SysUser;
import com.app.springbootcargovision.service.SysUserService;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统用户控制器
 * 提供用户管理的 RESTful API 接口
 */
@Tag(name = "用户管理", description = "系统用户管理的增删改查接口")
@RestController
@RequestMapping("/api/v1/users")
public class SysUserController {

    private final SysUserService sysUserService;

    public SysUserController(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    /**
     * 分页查询用户列表接口
     * 
     * @param page     页码（默认1）
     * @param size     每页数量（默认10）
     * @param username 用户名（可选）
     * @param name     姓名（可选）
     * @param roleId   角色ID（可选）
     * @param status   用户状态（可选）
     * @return 用户列表分页结果
     */
    @Operation(summary = "分页查询用户列表", description = "根据条件分页查询用户信息")
    @GetMapping
    @PreAuthorize("hasAuthority('sys:user:list')")
    public Result<PageInfo<SysUser>> getUserList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "用户名") @RequestParam(required = false) String username,
            @Parameter(description = "姓名") @RequestParam(required = false) String name,
            @Parameter(description = "角色ID") @RequestParam(required = false) Long roleId,
            @Parameter(description = "用户状态") @RequestParam(required = false) Integer status) {
        return Result.success(sysUserService.getUserList(page, size, username, name, roleId, status));
    }

    /**
     * 创建用户接口
     * 
     * @param sysUser 用户实体对象
     * @return 创建成功的用户对象（包含生成的ID）
     */
    @Operation(summary = "创建用户", description = "新增用户信息")
    @PostMapping
    @PreAuthorize("hasAuthority('sys:user:add')")
    public Result<SysUser> createUser(@Valid @RequestBody SysUser sysUser) {
        sysUserService.createUser(sysUser);
        return Result.success(sysUser);
    }

    /**
     * 更新用户信息接口
     * 
     * @param id      用户ID
     * @param sysUser 用户实体对象
     * @return 成功提示信息
     */
    @Operation(summary = "更新用户信息", description = "更新已存在的用户信息")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:user:edit')")
    public Result<String> updateUser(@Parameter(description = "用户ID") @PathVariable Long id,
            @Valid @RequestBody SysUser sysUser) {
        sysUser.setId(id);
        sysUserService.updateUser(sysUser);
        return Result.success("用户更新成功");
    }

    /**
     * 更新用户状态接口
     * 用于启用或禁用用户
     * 
     * @param id   用户ID
     * @param body 包含 status 字段的 Map
     * @return 成功提示信息
     */
    @Operation(summary = "更新用户状态", description = "启用或禁用用户")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('sys:user:edit')")
    public Result<String> updateUserStatus(@Parameter(description = "用户ID") @PathVariable Long id,
            @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        System.out.println("DEBUG: Request to update user " + id + " status to " + status);
        if (status == null) {
            return Result.error("状态不能为空");
        }
        sysUserService.updateUserStatus(id, status);
        return Result.success("状态更新成功");
    }

    /**
     * 重置用户密码接口
     * 
     * @param id   用户ID
     * @param body 包含 password 字段的 Map（可选，不传则使用默认密码）
     * @return 成功提示信息
     */
    @Operation(summary = "重置用户密码", description = "重置用户密码，若未提供密码则使用系统默认密码")
    @PatchMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('sys:user:edit')")
    public Result<String> resetPassword(@Parameter(description = "用户ID") @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String password = (body != null) ? body.get("password") : null;
        sysUserService.resetPassword(id, password);
        return Result.success("密码重置成功");
    }

    /**
     * 分配角色给用户
     *
     * @param id      用户ID
     * @param roleIds 角色ID列表
     * @return 成功提示信息
     */
    @Operation(summary = "分配用户角色", description = "为用户分配一个或多个角色")
    @PostMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('sys:user:edit')")
    public Result<String> assignUserRoles(@Parameter(description = "用户ID") @PathVariable Long id,
            @RequestBody List<Long> roleIds) {
        sysUserService.assignUserRoles(id, roleIds);
        return Result.success("角色分配成功");
    }

    /**
     * 删除用户接口
     *
     * @param id 用户ID
     * @return 成功提示信息
     */
    @Operation(summary = "删除用户", description = "根据ID删除用户信息")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:user:delete')")
    public Result<String> deleteUser(@Parameter(description = "用户ID") @PathVariable Long id) {
        sysUserService.deleteUser(id);
        return Result.success("用户删除成功");
    }
}
