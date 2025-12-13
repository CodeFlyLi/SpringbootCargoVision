package com.app.springbootcargovision.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Spring Security 配置类
 * 负责应用的安全配置，包括认证、授权、跨域和会话管理
 */
@Configuration
@EnableWebSecurity // 开启 Spring Security 的 Web 安全支持
@EnableMethodSecurity // 开启方法级别的安全注解支持 (@PreAuthorize)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * 配置安全过滤链
     * 定义哪些请求需要认证，哪些可以直接访问，以及如何处理 CSRF、CORS 和会话
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 禁用 CSRF (跨站请求伪造) 保护，因为我们使用 JWT 进行无状态认证
                .csrf(csrf -> csrf.disable())
                // 开启 CORS (跨域资源共享) 配置
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 配置请求授权规则
                .authorizeHttpRequests(auth -> auth
                        // 允许所有用户访问认证接口（登录、注册等）
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // 允许所有用户访问公共接口
                        .requestMatchers("/api/v1/common/**").permitAll()
                        // 允许访问静态资源（上传的文件）
                        .requestMatchers("/uploads/**").permitAll()
                        // 允许访问 Swagger 接口文档
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // 允许访问错误页面，防止 404 变为 403
                        .requestMatchers("/error").permitAll()
                        // 其他所有请求都需要认证（登录）后才能访问
                        .anyRequest().authenticated())
                // 配置会话管理策略为无状态（STATELESS），不创建 HttpSession，因为使用 JWT
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 在 UsernamePasswordAuthenticationFilter 之前添加 JWT 认证过滤器
                // 这样在 Spring Security 处理认证之前，我们的过滤器会先校验 JWT Token
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 配置跨域资源共享 (CORS)
     * 允许前端应用跨域访问后端接口
     */
    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true); // 允许发送 Cookie
        config.addAllowedOriginPattern("*"); // 允许所有来源域
        config.addAllowedHeader("*"); // 允许所有请求头
        config.addAllowedMethod("*"); // 允许所有请求方法 (GET, POST, PUT, DELETE 等)
        source.registerCorsConfiguration("/**", config); // 对所有路径应用此配置
        return source;
    }

    /**
     * 暴露 AuthenticationManager Bean
     * 用于在 Controller 或 Service 中进行用户认证
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * 配置密码编码器
     * 使用 BCryptPasswordEncoder 进行加密
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
