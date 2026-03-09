package com.app.springbootcargovision.controller;

import com.app.springbootcargovision.common.Result;
import com.app.springbootcargovision.annotation.Log;
import com.app.springbootcargovision.model.SysUser;
import com.app.springbootcargovision.service.AuthService;
import com.app.springbootcargovision.service.SysUserService;
import com.app.springbootcargovision.model.SysPermission;
import com.app.springbootcargovision.service.SysPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 认证控制器
 * 处理用户登录、登出及获取当前用户信息的 HTTP 请求
 */
@Tag(name = "认证管理", description = "处理用户登录、登出及获取当前用户信息的接口")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final SysUserService sysUserService;
    private final SysPermissionService sysPermissionService;

    public AuthController(AuthService authService,
            SysUserService sysUserService,
            SysPermissionService sysPermissionService) {
        this.authService = authService;
        this.sysUserService = sysUserService;
        this.sysPermissionService = sysPermissionService;
    }

    /**
     * 用户登录接口
     * 接收用户名和密码，验证成功后返回 JWT Token 和用户信息
     * 
     * @param loginRequest 包含 username 和 password 的 Map
     * @return 包含 token 和 userInfo 的结果对象
     */
    @Operation(summary = "用户登录", description = "接收用户名和密码，验证成功后返回 JWT Token 和用户信息")
    @Log(module = "认证管理", type = "登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        // 调用认证服务进行登录
        String token = authService.login(username, password);

        // 获取用户详细信息
        SysUser currentUser = sysUserService.getUserByUsername(username);

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userInfo", currentUser);

        return Result.success(data);
    }

    /**
     * 获取当前登录用户信息接口
     * 
     * @return 当前登录用户实体
     */
    @Operation(summary = "获取当前用户", description = "获取当前登录用户的详细信息")
    @GetMapping("/me")
    public Result<SysUser> getCurrentUser() {
        return Result.success(authService.getCurrentUser());
    }

    /**
     * 获取动态路由接口
     * 
     * @return 路由树
     */
    @Operation(summary = "获取动态路由", description = "获取系统菜单路由树")
    @GetMapping("/routers")
    public Result<List<SysPermission>> getRouters() {
        // 获取当前用户ID
        Long userId = authService.getCurrentUser().getId();
        // 根据用户ID获取权限树
        return Result.success(sysPermissionService.getUserPermissionTree(userId));
    }

    /**
     * 用户登出接口
     * 
     * @return 成功提示信息
     */
    @Operation(summary = "用户登出", description = "客户端侧清除 Token，服务端无需特殊处理")
    @Log(module = "认证管理", type = "登出")
    @PostMapping("/logout")
    public Result<String> logout() {
        // 客户端侧清除 Token，服务端无需特殊处理（JWT 无状态）
        return Result.success("Logged out successfully");
    }
}
