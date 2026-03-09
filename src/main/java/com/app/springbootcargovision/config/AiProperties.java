package com.app.springbootcargovision.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI 模型配置属性
 * 对应 application.yml 中的 ai 前缀配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    /**
     * 模型配置
     */
    private Model model;

    @Data
    public static class Model {
        /**
         * 云平台 AI 配置
         */
        private Cloud cloud;
    }

    @Data
    public static class Cloud {
        /**
         * 服务提供商: baidu, aliyun
         */
        private String provider;

        /**
         * API Key / Access Key ID
         */
        private String apiKey;

        /**
         * Secret Key / Access Key Secret
         */
        private String secretKey;

        /**
         * API 接口地址 (Endpoint)
         */
        private String endpoint;

        /**
         * 自定义 AI 接口地址 (EasyDL Custom Endpoint)
         */
        private String customEndpoint;
    }
}
