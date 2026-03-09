package com.app.springbootcargovision.service;

import com.app.springbootcargovision.model.SysLog;
import com.github.pagehelper.PageInfo;
import java.util.List;

public interface SysLogService {
    PageInfo<SysLog> getLogList(Integer page, Integer size, String module, String operator, String status,
            String startTime, String endTime);

    void saveLog(SysLog log);

    void deleteLogByIds(List<Long> ids);

    void cleanLog();
}
