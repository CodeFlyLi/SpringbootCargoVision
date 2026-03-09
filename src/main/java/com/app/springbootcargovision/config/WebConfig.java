package com.app.springbootcargovision.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.lang.NonNull;
import java.nio.file.Paths;

/**
 * Web MVC 全局配置类
 * 
 * 作用：扩展 Spring MVC 的默认功能。
 * 主要用于配置静态资源的访问路径，使上传到服务器本地的图片可以通过 HTTP URL 直接访问。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 从配置文件 (application.yml) 中读取上传文件的存储目录路径
    // 例如：file.upload-dir=./uploads
    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * 配置静态资源映射处理器
     * 
     * 场景：用户上传的图片存储在服务器本地磁盘（如项目根目录下的 uploads 文件夹）。
     * 默认情况下，Spring Boot 只能访问 classpath 下的静态资源。
     * 为了让浏览器能通过 URL（如 http://localhost:8080/uploads/xxx.jpg）访问这些外部文件，
     * 需要配置物理路径到 URL 路径的映射。
     * 
     * @param registry 资源处理器注册表
     */
    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // 1. 获取上传目录的绝对物理路径，并转换为 URI 格式
        // 例如：/Users/username/project/uploads -> file:///Users/username/project/uploads/
        String path = Paths.get(uploadDir).toAbsolutePath().toUri().toString();

        // 2. 建立映射关系
        // addResourceHandler("/uploads/**"): 当请求 URL 匹配 /uploads/开头时
        // addResourceLocations(path): 去本地磁盘的 path 目录下寻找对应的文件
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(path);
    }

    /**
     * 配置 RestTemplate Bean
     * 
     * 作用：RestTemplate 是 Spring 提供的同步 HTTP 客户端。
     * 用途：在本项目中，主要用于后端代码向第三方服务（如百度云 AI 接口）发起 HTTP 请求。
     * 
     * @return RestTemplate 实例
     */
    @org.springframework.context.annotation.Bean
    public org.springframework.web.client.RestTemplate restTemplate() {
        return new org.springframework.web.client.RestTemplate();
    }
}
