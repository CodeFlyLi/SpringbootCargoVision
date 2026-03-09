package com.app.springbootcargovision.service.impl;

import com.app.springbootcargovision.mapper.SysBackupMapper;
import com.app.springbootcargovision.model.LoginUser;
import com.app.springbootcargovision.model.SysBackup;
import com.app.springbootcargovision.service.SysBackupService;
import com.app.springbootcargovision.utils.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 系统备份服务实现类
 */
@Service
public class SysBackupServiceImpl implements SysBackupService {

    private static final Logger log = LoggerFactory.getLogger(SysBackupServiceImpl.class);
    private final SysBackupMapper sysBackupMapper;
    private static final String BACKUP_DIR = "backups";

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    public SysBackupServiceImpl(SysBackupMapper sysBackupMapper) {
        this.sysBackupMapper = sysBackupMapper;
        // 确保备份目录存在
        File dir = new File(BACKUP_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    @Override
    public List<SysBackup> getBackupList() {
        return sysBackupMapper.selectBackupList();
    }

    @Override
    @Transactional
    public void createBackup() {
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String fileName = "backup_" + timestamp + ".sql";
        File file = new File(BACKUP_DIR, fileName);

        try {
            // 从 URL 中解析数据库名称
            String dbName = getDbName();

            // 构建 mysqldump 命令
            // 注意：假设 mysqldump 已添加到系统环境变量 PATH 中
            ProcessBuilder pb = new ProcessBuilder(
                    "mysqldump",
                    "-u" + dbUsername,
                    "-p" + dbPassword,
                    "--result-file=" + file.getAbsolutePath(),
                    "--default-character-set=utf8",
                    "--hex-blob", // 处理二进制数据
                    dbName);

            pb.redirectErrorStream(true);
            Process process = pb.start();

            // 读取输出以便调试
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            // 等待进程结束，超时时间设置为 2 分钟
            if (process.waitFor(2, TimeUnit.MINUTES) && process.exitValue() == 0) {
                // 备份成功
                String operator = "System";
                try {
                    LoginUser currentUser = SecurityUtils.getLoginUser();
                    if (currentUser != null) {
                        operator = currentUser.getUsername();
                    }
                } catch (Exception e) {
                    // 忽略无登录上下文的情况（例如定时任务触发）
                }

                SysBackup backup = new SysBackup();
                backup.setName(fileName);
                backup.setPath(file.getAbsolutePath());
                backup.setSize(file.length());
                backup.setStatus("SUCCESS");
                backup.setVersion("v1.0.0");
                backup.setOperator(operator);
                backup.setCreateTime(new Date());
                backup.setDelFlag(0); // 设置初始删除标志为0（正常）
                sysBackupMapper.insertBackup(backup);
                log.info("备份创建成功: {}", file.getAbsolutePath());
            } else {
                log.error("备份失败。退出码: {}。输出信息: {}", process.exitValue(), output);
                throw new RuntimeException("备份进程执行失败，请检查日志。");
            }

        } catch (Exception e) {
            log.error("备份失败", e);
            throw new RuntimeException("备份失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deleteBackup(Long id) {
        SysBackup backup = sysBackupMapper.selectBackupById(id);
        if (backup != null) {
            // 软删除逻辑：只修改数据库标志，不删除磁盘文件
            backup.setDelFlag(1); // 1 代表已删除
            sysBackupMapper.updateBackup(backup);
            log.info("备份已标记为删除（软删除）: {}", backup.getPath());
        }
    }

    @Override
    public void restoreBackup(Long id) {
        SysBackup backup = sysBackupMapper.selectBackupById(id);
        if (backup == null) {
            throw new RuntimeException("备份记录未找到");
        }

        File file = new File(backup.getPath());
        if (!file.exists()) {
            throw new RuntimeException("磁盘上未找到备份文件");
        }

        try {
            String dbName = getDbName();

            // 构建 mysql 导入命令
            ProcessBuilder pb = new ProcessBuilder(
                    "mysql",
                    "-u" + dbUsername,
                    "-p" + dbPassword,
                    "--default-character-set=utf8",
                    dbName);

            pb.redirectInput(file); // 将备份文件作为标准输入
            pb.redirectErrorStream(true);

            Process process = pb.start();

            // 读取输出信息
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            // 等待进程结束，超时时间设置为 5 分钟
            if (process.waitFor(5, TimeUnit.MINUTES) && process.exitValue() == 0) {
                log.info("从文件恢复成功: {}", file.getAbsolutePath());
            } else {
                log.error("恢复失败。退出码: {}。输出信息: {}", process.exitValue(), output);
                throw new RuntimeException("恢复进程执行失败，请检查日志。");
            }

        } catch (Exception e) {
            log.error("恢复失败", e);
            throw new RuntimeException("恢复失败: " + e.getMessage());
        }
    }

    @Override
    public SysBackup getBackupById(Long id) {
        return sysBackupMapper.selectBackupById(id);
    }

    @Override
    public void exportBackup(java.io.OutputStream out) {
        try {
            String dbName = getDbName();
            // 构建 mysqldump 命令，不使用 --result-file，直接输出到 stdout
            ProcessBuilder pb = new ProcessBuilder(
                    "mysqldump",
                    "-u" + dbUsername,
                    "-p" + dbPassword,
                    "--default-character-set=utf8",
                    "--hex-blob",
                    dbName);

            pb.redirectErrorStream(false);
            Process process = pb.start();

            // 将进程的输入流（即 mysqldump 的输出）拷贝到 response 的输出流
            try (java.io.InputStream in = process.getInputStream()) {
                byte[] buffer = new byte[8192];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
                out.flush();
            }

            // 检查退出码
            if (!process.waitFor(2, TimeUnit.MINUTES) || process.exitValue() != 0) {
                log.error("导出备份失败，退出码: {}", process.exitValue());
                throw new RuntimeException("数据库导出进程执行失败");
            }
            log.info("数据库备份流导出成功");

        } catch (Exception e) {
            log.error("导出备份异常", e);
            throw new RuntimeException("导出备份失败: " + e.getMessage());
        }
    }

    /**
     * 从 JDBC URL 中解析数据库名
     */
    private String getDbName() {
        String url = dbUrl;
        if (url.contains("?")) {
            url = url.substring(0, url.indexOf("?"));
        }
        return url.substring(url.lastIndexOf("/") + 1);
    }
}
