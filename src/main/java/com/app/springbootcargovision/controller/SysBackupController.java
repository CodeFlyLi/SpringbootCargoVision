package com.app.springbootcargovision.controller;

import com.app.springbootcargovision.common.Result;
import com.app.springbootcargovision.annotation.Log;
import com.app.springbootcargovision.model.SysBackup;
import com.app.springbootcargovision.service.SysBackupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.List;

/**
 * 系统备份控制器
 */
@Tag(name = "系统备份", description = "系统备份管理接口")
@RestController
@RequestMapping("/api/v1/system/backups")
public class SysBackupController {

    private final SysBackupService sysBackupService;

    public SysBackupController(SysBackupService sysBackupService) {
        this.sysBackupService = sysBackupService;
    }

    @Operation(summary = "查询备份列表", description = "获取所有备份文件记录")
    @GetMapping
    public Result<List<SysBackup>> getBackupList() {
        return Result.success(sysBackupService.getBackupList());
    }

    @Operation(summary = "创建新备份", description = "触发系统备份")
    @Log(module = "系统备份", type = "新增")
    @PostMapping
    public Result<Void> createBackup() {
        sysBackupService.createBackup();
        return Result.success();
    }

    @Operation(summary = "删除备份", description = "删除指定的备份文件")
    @DeleteMapping("/{id}")
    public Result<Void> deleteBackup(@PathVariable Long id) {
        sysBackupService.deleteBackup(id);
        return Result.success();
    }

    @Operation(summary = "恢复备份", description = "将系统恢复到指定备份")
    @PostMapping("/{id}/restore")
    public Result<Void> restoreBackup(@PathVariable Long id) {
        sysBackupService.restoreBackup(id);
        return Result.success();
    }

    @Operation(summary = "下载备份文件", description = "下载指定的备份文件")
    @Log(module = "系统备份", type = "导出")
    @GetMapping("/{id}/download")
    public void downloadBackup(@PathVariable Long id, HttpServletResponse response) throws IOException {
        SysBackup backup = sysBackupService.getBackupById(id);
        if (backup == null) {
            response.setStatus(404);
            return;
        }

        File file = new File(backup.getPath());
        if (!file.exists()) {
            response.setStatus(404);
            return;
        }

        String fileName = backup.getName();
        response.setContentType("application/octet-stream");
        // 使用标准的 RFC 5987 编码方式，解决中文和空格问题
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + fileName + "\"; filename*=UTF-8''"
                        + URLEncoder.encode(fileName, "UTF-8").replace("+", "%20"));
        response.setContentLength((int) file.length());

        try (FileInputStream in = new FileInputStream(file);
                OutputStream out = response.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }
    }

    @Operation(summary = "直接导出备份", description = "实时生成备份并流式下载，不在服务器保存文件")
    @GetMapping("/export")
    public void exportBackup(HttpServletResponse response) throws IOException {
        String timestamp = new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date());
        String fileName = "export_backup_" + timestamp + ".sql";

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + fileName + "\"; filename*=UTF-8''"
                        + URLEncoder.encode(fileName, "UTF-8").replace("+", "%20"));

        sysBackupService.exportBackup(response.getOutputStream());
    }
}
