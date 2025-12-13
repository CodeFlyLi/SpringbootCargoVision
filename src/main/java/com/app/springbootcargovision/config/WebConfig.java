package com.app.springbootcargovision.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.lang.NonNull;
import java.nio.file.Paths;

/**
 * Web MVC 配置类
 * 用于自定义 Spring MVC 的行为
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir; // 从 application.yml 读取文件上传目录

    /**
     * 配置静态资源映射
     * 将 /uploads/** 的请求映射到本地文件系统的 uploadDir 目录
     * 例如：访问 http://localhost:8080/uploads/abc.jpg 会读取本地 ./uploads/abc.jpg
     */
    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // 将相对路径转换为绝对 URI，避免不同环境下路径解析问题
        String path = Paths.get(uploadDir).toAbsolutePath().toUri().toString();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(path);
    }
}
