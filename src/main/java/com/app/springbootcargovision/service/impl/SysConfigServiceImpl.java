package com.app.springbootcargovision.service.impl;

import com.app.springbootcargovision.mapper.SysConfigMapper;
import com.app.springbootcargovision.model.SysConfig;
import com.app.springbootcargovision.service.SysConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SysConfigServiceImpl implements SysConfigService {

    private final SysConfigMapper sysConfigMapper;

    public SysConfigServiceImpl(SysConfigMapper sysConfigMapper) {
        this.sysConfigMapper = sysConfigMapper;
    }

    @Override
    public List<SysConfig> getAllConfigs() {
        return sysConfigMapper.selectAll();
    }

    @Override
    public Map<String, String> getConfigMap() {
        List<SysConfig> list = sysConfigMapper.selectAll();
        return list.stream().collect(Collectors.toMap(SysConfig::getConfigKey, SysConfig::getConfigValue));
    }

    @Override
    public String getValue(String key, String defaultValue) {
        SysConfig config = sysConfigMapper.selectByKey(key);
        return config != null ? config.getConfigValue() : defaultValue;
    }

    @Override
    @Transactional
    public void updateConfig(String key, String value) {
        SysConfig config = new SysConfig();
        config.setConfigKey(key);
        config.setConfigValue(value);
        
        // 尝试更新，如果影响行数为0（不存在），则应该插入（但这里假设配置项都是预设好的，或者需要先查询）
        // 简单起见，我们假设配置项已存在（初始化脚本已插入）。如果要支持新增，可以先查后改/插
        if (sysConfigMapper.update(config) == 0) {
            // 如果不存在，则插入
            config.setDescription("用户自定义配置");
            sysConfigMapper.insert(config);
        }
    }

    @Override
    @Transactional
    public void updateConfigs(Map<String, String> configs) {
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            updateConfig(entry.getKey(), entry.getValue());
        }
    }
}
