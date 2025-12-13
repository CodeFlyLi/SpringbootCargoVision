package com.app.springbootcargovision.config;

import com.app.springbootcargovision.mapper.SysUserMapper;
import com.app.springbootcargovision.model.SysUser;
import com.app.springbootcargovision.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.util.Date;

/**
 * JWT 认证过滤器
 * 继承自 OncePerRequestFilter，确保每次请求只执行一次
 * 作用：拦截请求，检查 Header 中的 Authorization 是否包含有效的 JWT Token
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // JWT 工具类，用于解析和校验 JWT Token
    private final JwtUtils jwtUtils;
    // 用户详情服务，用于加载用户信息
    private final UserDetailsService userDetailsService;
    // 系统用户Mapper，用于直接查询用户最后重置密码时间，避免循环依赖
    private final SysUserMapper sysUserMapper;

    /**
     * 构造函数，注入 JWT 工具类和用户详情服务
     *
     * @param jwtUtils           JWT 工具类，用于解析和校验 JWT Token
     * @param userDetailsService 用户详情服务，用于加载用户信息
     * @param sysUserMapper      系统用户Mapper
     */
    public JwtAuthenticationFilter(JwtUtils jwtUtils, UserDetailsService userDetailsService,
            SysUserMapper sysUserMapper) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
        this.sysUserMapper = sysUserMapper;
    }

    /**
     * 过滤器核心逻辑
     * 1. 从请求头中提取 JWT
     * 2. 校验 JWT 是否有效
     * 3. 如果有效，加载用户信息并设置到 Spring Security 上下文中
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        // 从请求头中提取 Authorization 字段
        final String authHeader = request.getHeader("Authorization");
        String username = null;
        String jwt = null;

        // 检查 Authorization 头是否存在且以 "Bearer " 开头
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7); // 截取 "Bearer " 之后的部分作为 Token
            try {
                username = jwtUtils.extractUsername(jwt); // 从 Token 中提取用户名
            } catch (Exception e) {
                // Token 无效或过期，忽略异常，后续 SecurityContext 中没有认证信息，会被 Security 框架拦截
                logger.warn("JWT Token 无效或过期: " + e.getMessage());
            }
        }

        // 如果用户名不为空，且当前上下文没有认证信息（说明尚未认证）
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 加载用户详情
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // 验证 Token 是否合法（是否过期，用户名是否匹配）
            // 同时检查用户账号是否启用
            boolean isValid = jwtUtils.validateToken(jwt, userDetails.getUsername());
            boolean isEnabled = userDetails.isEnabled();
            boolean isPasswordValid = true;

            // 检查 Token 签发时间是否早于最后一次重置密码时间
            if (isValid && isEnabled) {
                SysUser sysUser = sysUserMapper.selectByUsername(username);
                if (sysUser != null && sysUser.getLastPasswordResetTime() != null) {
                    Date issuedAt = jwtUtils.extractIssuedAt(jwt);
                    // 如果 Token 签发时间早于密码重置时间（容忍1秒误差），则认为无效
                    if (issuedAt != null && issuedAt
                            .before(java.sql.Timestamp.valueOf(sysUser.getLastPasswordResetTime().minusSeconds(1)))) {
                        isPasswordValid = false;
                        logger.warn("JWT Token 已失效: 密码已重置 (User: " + username + ")");
                    }
                }
            }

            if (isValid && isEnabled && isPasswordValid) {
                // 创建认证 Token 对象
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                // 设置请求详情（IP, SessionId 等）
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // 将认证信息设置到 SecurityContextHolder，表示该请求已通过认证
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                if (!isEnabled) {
                    System.out.println("DEBUG: JWT Filter blocked disabled user: " + username);
                    // 账号被禁用，直接返回 403 错误，不继续执行过滤器链
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":403,\"message\":\"账号已被禁用，请联系管理员\",\"data\":null}");
                    return;
                }
                if (!isPasswordValid) {
                    // 密码已重置，Token 失效，返回 401 错误
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":401,\"message\":\"密码已修改，请重新登录\",\"data\":null}");
                    return;
                }
            }
        }
        // 继续执行过滤器链
        filterChain.doFilter(request, response);
    }
}
