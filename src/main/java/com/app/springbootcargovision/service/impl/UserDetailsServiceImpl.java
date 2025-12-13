package com.app.springbootcargovision.service.impl;

import com.app.springbootcargovision.mapper.SysPermissionMapper;
import com.app.springbootcargovision.mapper.SysUserMapper;
import com.app.springbootcargovision.model.LoginUser;
import com.app.springbootcargovision.model.SysPermission;
import com.app.springbootcargovision.model.SysRole;
import com.app.springbootcargovision.model.SysUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Spring Security 用户详情服务实现类
 * 用于在认证过程中加载用户信息
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper sysUserMapper;
    private final SysPermissionMapper sysPermissionMapper;

    public UserDetailsServiceImpl(SysUserMapper sysUserMapper, SysPermissionMapper sysPermissionMapper) {
        this.sysUserMapper = sysUserMapper;
        this.sysPermissionMapper = sysPermissionMapper;
    }

    /**
     * 根据用户名加载用户信息
     * Spring Security 调用此方法获取用户详情（密码、权限等）
     * 1. 从数据库查询用户信息
     * 2. 构建用户权限列表（角色 + 菜单/按钮权限）
     * 3. 返回 UserDetails 对象（包含用户名、密码、权限）
     * 4. 如果用户不存在，抛出 UsernameNotFoundException 异常
     *
     * @param username 用户名
     * @return UserDetails 对象
     * @throws UsernameNotFoundException 如果用户不存在
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 查询数据库获取用户信息 (支持用户名或身份证号登录)
        SysUser sysUser = sysUserMapper.selectLoginUser(username);
        if (sysUser == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        
        System.out.println("DEBUG: UserDetailsServiceImpl loaded user: " + sysUser.getUsername() + ", ID: " + sysUser.getId() + ", Status: " + sysUser.getStatus());
        
        // GrantedAuthority是Spring Security中表示权限的接口，用于存储用户的角色和菜单/按钮权限
        // 每个权限都需要以 "ROLE_" 前缀开头，Spring Security 会自动识别并处理角色权限
        List<GrantedAuthority> authorities = new ArrayList<>();

        // 添加角色权限 (ROLE_前缀)
        if (sysUser.getRoles() != null) {
            // 遍历用户角色列表，每个角色添加 ROLE_ 前缀
            // 例如：ROLE_ADMIN, ROLE_USER 等
            for (SysRole role : sysUser.getRoles()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getRoleCode()));
            }
        }

        // 添加菜单/按钮权限
        List<SysPermission> permissions = sysPermissionMapper.selectByUserId(sysUser.getId());
        for (SysPermission perm : permissions) {
            if (perm.getCode() != null && !perm.getCode().isEmpty()) {
                authorities.add(new SimpleGrantedAuthority(perm.getCode()));
            }
        }

        // 构建并返回 Spring Security 需要的 User 对象
        return new LoginUser(sysUser, authorities);
    }
}
// 当前项目中使用了Spring Security和JWT，分别用于认证和授权。
// 我的项目哪些是用了jwt？哪些是用了spring security？、？
// 我的项目中用了JWT来实现无状态的认证和授权，用于保护API接口的安全性。
// 而Spring Security则用于处理用户登录、会话管理、权限校验等功能。

// Spring Security和JWT的区别是：
// 1. 认证方式：
// - Spring Security 使用基于表单的登录、基于 HTTP 基本认证、基于 OAuth2 等多种认证方式。
// - JWT 是一种无状态的认证方式，通过在请求中包含令牌进行认证。
// 2. 授权方式：
// - Spring Security 基于角色和权限进行授权，通过配置访问规则来控制用户对资源的访问。
// - JWT 则通过在令牌中包含用户角色和权限信息，在服务端进行校验。
// 3. 存储位置：
// - Spring Security 通常将用户认证信息存储在会话中，而 JWT 则将认证信息存储在令牌中，通常在 HTTP 头中传递。
// 4. 扩展性：
// - Spring Security 提供了丰富的扩展点和插件机制，开发人员可以根据需求自定义认证和授权逻辑。
// - JWT 则相对简单，主要依赖于标准的 JSON 格式，扩展性较差。
//
// Spring Security是一个基于Java的框架，用于保护应用程序的安全性。
// 它提供了认证（登录）和授权（访问控制）功能，帮助开发人员保护应用程序免受未经授权的访问。
// 主要功能包括：
// 1. 认证（登录）：验证用户身份，确保只有授权用户才能访问应用程序。
// 2. 授权（访问控制）：根据用户角色和权限，决定用户是否有权执行特定操作或访问特定资源。
// 3. 会话管理：处理用户登录会话，包括会话超时、CSRF 保护等。
// 4. 密码加密：对用户密码进行加密存储，防止密码泄露。
// 5. 攻击防护：提供防止常见攻击（如 CSRF、XSS、SQL 注入等）的防护机制。
// 6. 自定义配置：允许开发人员根据应用程序需求自定义认证和授权逻辑。
// 7. 与其他框架集成：与 Spring Boot、Spring MVC、Hibernate 等框架无缝集成。
// 8. 社区支持：有活跃的社区支持，提供文档、教程、插件等资源。
