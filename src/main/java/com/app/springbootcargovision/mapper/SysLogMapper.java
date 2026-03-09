package com.app.springbootcargovision.mapper;

import com.app.springbootcargovision.model.SysLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysLogMapper {
    List<SysLog> selectLogList(@Param("log") SysLog log, @Param("startTime") String startTime,
            @Param("endTime") String endTime);

    int insertLog(SysLog log);

    int deleteLogByIds(@Param("ids") List<Long> ids);

    void cleanLog();
}
