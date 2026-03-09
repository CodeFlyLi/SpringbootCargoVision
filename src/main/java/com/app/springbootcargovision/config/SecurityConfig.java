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
 * Spring Security 安全配置核心类
 * 
 * 本类是整个系统的安全网关，负责定义所有的安全规则、认证机制和授权策略。
 * 
 * 主要功能：
 * 1. 认证配置：定义如何验证用户身份（登录）。
 * 2. 授权配置：定义哪些接口需要登录才能访问，哪些接口是公开的。
 * 3. 过滤器链：编排安全过滤器，将 JWT 认证过滤器加入到 Spring Security 的执行链中。
 * 4. 跨域与会话：配置 CORS 跨域支持，并禁用 Session（因为我们使用无状态的 JWT）。
 */
@Configuration
@EnableWebSecurity // 启用 Spring Security 的 Web 安全功能
@EnableMethodSecurity // 启用方法级别的安全注解（如 @PreAuthorize），允许在 Controller 方法上控制权限
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // 构造器注入 JWT 过滤器
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * 配置安全过滤器链 (SecurityFilterChain)
     * 
     * 这是 Spring Security 的核心配置方法。它构建了一个过滤器链，所有进入系统的 HTTP 请求
     * 都会经过这个链条的处理。
     * 
     * @param http HttpSecurity 对象，用于构建安全配置
     * @return 配置好的 SecurityFilterChain
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. 禁用 CSRF (跨站请求伪造) 保护
            // 原因：CSRF 攻击主要针对基于 Cookie/Session 的认证。我们使用 JWT Header 认证，
            // 且不依赖 Cookie 存储 Session ID，因此天然免疫 CSRF 攻击，禁用它可以减少不必要的复杂性。
            .csrf(csrf -> csrf.disable())

            // 2. 开启 CORS (跨域资源共享)
            // 原因：前端 (Vue) 和后端 (Spring Boot) 通常运行在不同端口（如 8080 和 5173），
            // 浏览器出于安全限制默认拦截跨域请求。这里启用 CORS 并使用下面的 corsConfigurationSource 配置。
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // 3. 配置 URL 请求的访问权限
            .authorizeHttpRequests(auth -> auth
                // 公开接口：认证相关（登录、注册），无需 Token 即可访问
                .requestMatchers("/api/v1/auth/**").permitAll()
                // 公开接口：公共服务，无需认证
                .requestMatchers("/api/v1/common/**").permitAll()
                // 公开接口：静态资源（上传的图片），允许直接通过 URL 访问
                .requestMatchers("/uploads/**").permitAll()
                // 公开接口：Swagger API 文档，方便开发调试
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                // 公开接口：Spring Boot 默认错误页，防止认证失败时重定向循环
                .requestMatchers("/error").permitAll()
                // 受保护接口：除上述白名单外的所有请求，都必须通过认证（携带有效 Token）
                .anyRequest().authenticated()
            )

            // 4. 配置会话管理策略
            // 模式：STATELESS (无状态)
            // 原因：RESTful API 推荐无状态设计。服务器不保存客户端的 Session 信息，
            // 每次请求都必须携带 JWT Token。这使得系统更易于水平扩展。
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 5. 添加自定义 JWT 过滤器
            // 位置：在 UsernamePasswordAuthenticationFilter 之前执行。
            // 作用：在 Spring Security 标准认证流程开始前，先解析请求头中的 JWT Token。
            // 如果 Token 有效，手动设置 SecurityContext，让 Spring Security 认为用户已登录。
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 配置 CORS (跨域资源共享) 具体规则
     * 
     * @return CORS 配置源
     */
    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许携带凭证（如 Cookie），虽然 JWT 主要在 Header 中，但有时也会用到 Cookie
        config.setAllowCredentials(true); 
        // 允许所有的来源域名（生产环境建议指定具体的域名，如 http://localhost:5173）
        config.addAllowedOriginPattern("*"); 
        // 允许所有的请求头（Header）
        config.addAllowedHeader("*"); 
        // 允许所有的 HTTP 方法（GET, POST, PUT, DELETE, OPTIONS 等）
        config.addAllowedMethod("*"); 
        
        // 将此配置应用到所有路径 (/**)
        source.registerCorsConfiguration("/**", config); 
        return source;
    }

    /**
     * 暴露 AuthenticationManager Bean
     * 
     * 作用：AuthenticationManager 是 Spring Security 的认证入口。
     * 我们需要在 LoginService 中手动调用它来验证用户名和密码。
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * 配置密码加密器
     * 
     * 算法：BCrypt
     * 作用：用户注册时，将明文密码加密存储到数据库；用户登录时，将输入的密码加密后与数据库比对。
     * BCrypt 会自动加盐（Salt），即使两个用户密码相同，加密后的字符串也不同，安全性极高。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
