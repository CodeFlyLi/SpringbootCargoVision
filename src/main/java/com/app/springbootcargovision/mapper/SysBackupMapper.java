package com.app.springbootcargovision.mapper;

import com.app.springbootcargovision.model.SysBackup;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface SysBackupMapper {
    List<SysBackup> selectBackupList();

    SysBackup selectBackupById(Long id);

    int insertBackup(SysBackup backup);

    int updateBackup(SysBackup backup);

    int deleteBackupById(Long id);
}
