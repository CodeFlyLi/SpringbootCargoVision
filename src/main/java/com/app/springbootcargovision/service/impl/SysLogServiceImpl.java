package com.app.springbootcargovision.service.impl;

import com.app.springbootcargovision.mapper.SysLogMapper;
import com.app.springbootcargovision.model.SysLog;
import com.app.springbootcargovision.service.SysLogService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysLogServiceImpl implements SysLogService {

    private final SysLogMapper sysLogMapper;

    public SysLogServiceImpl(SysLogMapper sysLogMapper) {
        this.sysLogMapper = sysLogMapper;
    }

    @Override
    public PageInfo<SysLog> getLogList(Integer page, Integer size, String module, String operator, String status,
            String startTime, String endTime) {
        PageHelper.startPage(page, size);
        SysLog sysLog = new SysLog();
        sysLog.setModule(module);
        sysLog.setOperator(operator);
        sysLog.setStatus(status);

        List<SysLog> list = sysLogMapper.selectLogList(sysLog, startTime, endTime);
        return new PageInfo<>(list);
    }

    @Override
    public void saveLog(SysLog log) {
        sysLogMapper.insertLog(log);
    }

    @Override
    public void deleteLogByIds(List<Long> ids) {
        sysLogMapper.deleteLogByIds(ids);
    }

    @Override
    public void cleanLog() {
        sysLogMapper.cleanLog();
    }
}
