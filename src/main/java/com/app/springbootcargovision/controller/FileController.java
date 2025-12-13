package com.app.springbootcargovision.controller;

import com.app.springbootcargovision.common.Result;
import com.app.springbootcargovision.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件管理控制器
 * 提供文件上传和访问的 API 接口
 */
@Tag(name = "文件管理", description = "文件上传和访问接口")
@RestController
@RequestMapping("/api/v1/common")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * 文件上传接口
     * 接收文件并保存到服务器本地目录
     * 
     * @param file 上传的文件对象
     * @return 包含文件访问URL和元数据的结果对象
     * @throws IOException 文件保存异常
     */
    @Operation(summary = "文件上传", description = "接收文件并保存到服务器本地目录")
    @PostMapping("/upload")
    public Result<Map<String, Object>> upload(
            @Parameter(description = "上传的文件") @RequestParam("file") MultipartFile file) throws IOException {
        String url = fileService.upload(file);

        Map<String, Object> data = new HashMap<>();
        data.put("url", url);
        data.put("fileName", file.getOriginalFilename());
        data.put("size", file.getSize());

        return Result.success(data);
    }
}
