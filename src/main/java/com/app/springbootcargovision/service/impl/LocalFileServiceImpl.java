package com.app.springbootcargovision.service.impl;

import com.app.springbootcargovision.service.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import java.util.Arrays;
import java.util.List;

/**
 * 本地文件存储服务实现类
 * 负责将文件上传并存储到服务器本地文件系统
 */
@Service
public class LocalFileServiceImpl implements FileService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    // 允许的文件扩展名列表
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".bmp",
            ".webp");

    /**
     * 上传文件
     * 将 MultipartFile 保存到配置的本地目录，并返回可访问的 URL
     * 
     * @param file 上传的文件
     * @return 文件的访问 URL
     * @throws IOException 如果 IO 操作失败或文件类型不合法
     */
    @Override
    public String upload(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("文件为空");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null
                ? originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase()
                : "";

        // 校验文件扩展名
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IOException("不支持的文件类型: " + extension + "，仅支持: " + ALLOWED_EXTENSIONS);
        }

        // 检查并创建上传目录
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 生成唯一文件名
        // originalFilename 和 extension 已经在前面定义并获取了
        String fileName = UUID.randomUUID().toString() + extension;
        Path path = Paths.get(uploadDir, fileName);

        // 保存文件
        Files.write(path, file.getBytes());

        // 生成完整访问 URL (http://host:port/uploads/filename)
        String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/")
                .path(fileName)
                .toUriString();

        return fileUrl;
    }
}
