package com.app.springbootcargovision.service;

import com.app.springbootcargovision.model.SysBackup;
import java.util.List;

public interface SysBackupService {
    List<SysBackup> getBackupList();

    void createBackup();

    void deleteBackup(Long id);

    void restoreBackup(Long id);

    SysBackup getBackupById(Long id);

    /**
     * 直接导出备份流到输出流
     */
    void exportBackup(java.io.OutputStream outputStream);
}
