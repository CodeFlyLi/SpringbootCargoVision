package com.app.springbootcargovision.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

/**
 * 文件存储服务接口
 * 支持本地存储，可扩展为云存储 (AWS S3, OSS 等)
 */
public interface FileService {

    /**
     * 上传文件
     * @param file 文件对象
     * @return 文件访问 URL
     * @throws IOException 上传异常
     */
    String upload(MultipartFile file) throws IOException;
}
