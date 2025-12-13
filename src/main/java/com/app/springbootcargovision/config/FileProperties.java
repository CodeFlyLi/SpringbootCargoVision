package com.app.springbootcargovision.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 文件配置属性
 * 对应 application.yml 中的 file 前缀配置
 * 告诉系统文件 存在哪里
 */
@Data
@Component
@ConfigurationProperties(prefix = "file")
public class FileProperties {
    /**
     * 文件上传目录
     */
    private String uploadDir;
}
