package com.app.springbootcargovision.service;

import com.app.springbootcargovision.model.SysConfig;
import java.util.List;
import java.util.Map;

/**
 * 系统配置服务接口
 */
public interface SysConfigService {
    /**
     * 获取所有配置
     */
    List<SysConfig> getAllConfigs();

    /**
     * 获取所有配置 (Map形式, key -> value)
     */
    Map<String, String> getConfigMap();

    /**
     * 根据键获取值
     */
    String getValue(String key, String defaultValue);

    /**
     * 更新配置
     */
    void updateConfig(String key, String value);
    
    /**
     * 批量更新配置
     */
    void updateConfigs(Map<String, String> configs);
}
