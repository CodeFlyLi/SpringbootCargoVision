package com.app.springbootcargovision.service.impl;

import com.app.springbootcargovision.mapper.SysPermissionMapper;
import com.app.springbootcargovision.mapper.SysUserMapper;
import com.app.springbootcargovision.model.SysPermission;
import com.app.springbootcargovision.model.SysUser;
import com.app.springbootcargovision.model.SysRole;
import com.app.springbootcargovision.service.AuthService;
import com.app.springbootcargovision.utils.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 认证服务实现类
 * 实现用户登录和获取当前用户信息的具体业务逻辑
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final SysUserMapper sysUserMapper;
    private final SysPermissionMapper sysPermissionMapper;

    public AuthServiceImpl(AuthenticationManager authenticationManager, JwtUtils jwtUtils,
            SysUserMapper sysUserMapper, SysPermissionMapper sysPermissionMapper) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.sysUserMapper = sysUserMapper;
        this.sysPermissionMapper = sysPermissionMapper;
    }

    /**
     * 用户登录实现
     * 1. 使用 AuthenticationManager 进行用户认证
     * 2. 认证成功后获取用户信息
     * 3. 生成并返回 JWT Token
     */
    @Override
    public String login(String username, String password) {
        // 1. 先查询用户状态，进行防御性检查 (支持用户名或身份证号)
        SysUser sysUser = sysUserMapper.selectLoginUser(username);
        if (sysUser == null) {
            throw new RuntimeException("用户不存在");
        }

        if (sysUser.getStatus() != null && sysUser.getStatus() != 1) {
            throw new org.springframework.security.authentication.DisabledException("账号已被禁用");
        }

        // 2. 调用 Spring Security 的认证管理器进行认证
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));

        // 获取认证后的用户信息
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // 3. 再次确认状态 (虽然 AuthenticationManager 应该已经检查过了)
        if (!userDetails.isEnabled()) {
            throw new org.springframework.security.authentication.DisabledException("账号已被禁用");
        }

        // 获取用户角色，默认为 USER
        String roleCode = "USER";
        if (sysUser.getRoles() != null && !sysUser.getRoles().isEmpty()) {
            roleCode = sysUser.getRoles().stream()
                    .map(SysRole::getRoleCode)
                    .collect(Collectors.joining(","));
        }

        // 生成 JWT Token
        return jwtUtils.generateToken(userDetails.getUsername(), roleCode);
    }

    /**
     * 获取当前用户信息实现
     * 从 Spring Security 上下文中获取当前已认证的用户详情
     */
    @Override
    public SysUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            SysUser user = sysUserMapper.selectByUsername(userDetails.getUsername());
            if (user != null) {
                List<SysPermission> permissions = sysPermissionMapper.selectByUserId(user.getId());
                List<String> permissionCodes = permissions.stream()
                        .map(SysPermission::getCode)
                        .filter(code -> code != null && !code.isEmpty())
                        .collect(Collectors.toList());

                user.setPermissions(permissionCodes);
            }
            return user;
        }
        return null;
    }
}
