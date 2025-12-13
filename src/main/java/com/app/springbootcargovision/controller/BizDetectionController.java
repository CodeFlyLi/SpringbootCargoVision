package com.app.springbootcargovision.controller;

import com.app.springbootcargovision.common.Result;
import com.app.springbootcargovision.model.BizDetection;
import com.app.springbootcargovision.service.BizDetectionService;
import com.app.springbootcargovision.service.impl.ExportServiceImpl;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 货物检测控制器
 * 提供货物检测记录管理及图片上传检测的 API 接口
 */
@Tag(name = "货物检测", description = "货物检测记录管理及图片上传检测接口")
@RestController
@RequestMapping("/api/v1/detections")
public class BizDetectionController {

    private final BizDetectionService bizDetectionService;
    private final ExportServiceImpl exportService;

    public BizDetectionController(BizDetectionService bizDetectionService, ExportServiceImpl exportService) {
        this.bizDetectionService = bizDetectionService;
        this.exportService = exportService;
    }

    /**
     * 分页查询检测记录接口
     * 
     * @param page        页码（默认1）
     * @param size        每页数量（默认10）
     * @param detectionNo 检测编号（可选）
     * @param transportNo 运输单号（可选）
     * @param goodsName   货物名称（可选）
     * @param damageLevel 破损等级（可选）
     * @return 检测记录分页结果
     */
    @Operation(summary = "分页查询检测记录", description = "根据条件分页查询检测记录信息")
    @GetMapping

    public Result<PageInfo<BizDetection>> getDetectionList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "检测编号") @RequestParam(required = false) String detectionNo,
            @Parameter(description = "运输单号") @RequestParam(required = false) String transportNo,
            @Parameter(description = "货物名称") @RequestParam(required = false) String goodsName,
            @Parameter(description = "破损等级") @RequestParam(required = false) Integer damageLevel) {
        return Result.success(
                bizDetectionService.getDetectionList(page, size, detectionNo, transportNo, goodsName, damageLevel));
    }

    /**
     * 导出检测记录 Excel
     */
    @Operation(summary = "导出检测记录", description = "导出符合条件的检测记录为 Excel 文件")
    @GetMapping("/export")

    public void exportDetections(
            HttpServletResponse response,
            @Parameter(description = "检测编号") @RequestParam(required = false) String detectionNo,
            @Parameter(description = "运输单号") @RequestParam(required = false) String transportNo,
            @Parameter(description = "货物名称") @RequestParam(required = false) String goodsName,
            @Parameter(description = "破损等级") @RequestParam(required = false) Integer damageLevel) throws IOException {
        exportService.exportDetectionToExcel(response, detectionNo, transportNo, goodsName, damageLevel);
    }

    /**
     * 导出选中检测记录 Excel
     */
    @Operation(summary = "导出选中检测记录", description = "导出选中的检测记录为 Excel 文件")
    @PostMapping("/export/selected")

    public void exportSelectedDetections(
            HttpServletResponse response,
            @RequestBody List<Long> ids) throws IOException {
        exportService.exportSelectedDetectionToExcel(response, ids);
    }

    /**
     * 生成检测报告 PDF
     */
    @Operation(summary = "生成检测报告", description = "生成指定检测记录的 PDF 报告")
    @GetMapping("/{id}/report")

    public void generateReport(
            HttpServletResponse response,
            @Parameter(description = "检测记录ID") @PathVariable Long id) throws Exception {
        exportService.generateDetectionReportPdf(response, id);
    }

    /**
     * 根据ID获取检测记录详情接口
     * 
     * @param id 检测记录ID
     * @return 检测记录详情结果
     */
    @Operation(summary = "获取检测记录详情", description = "根据ID获取检测记录详细信息")
    @GetMapping("/{id}")
    public Result<BizDetection> getDetectionById(@Parameter(description = "检测记录ID") @PathVariable Long id) {
        return Result.success(bizDetectionService.getDetectionById(id));
    }

    /**
     * 创建检测记录接口
     * 
     * @param detection 检测记录实体对象
     * @return 成功提示信息
     */
    @Operation(summary = "创建检测记录", description = "新增检测记录信息")
    @PostMapping
    public Result<String> createDetection(@Valid @RequestBody BizDetection detection) {
        bizDetectionService.createDetection(detection);
        return Result.success("检测记录保存成功");
    }

    /**
     * 货物图片检测接口
     * 上传图片进行货物破损检测
     * 
     * @param file        上传的图片文件
     * @param transportId 关联的运输单ID (可选)
     * @return 检测结果实体
     * @throws IOException 文件处理异常
     */
    @Operation(summary = "货物图片检测", description = "上传图片进行货物破损检测")
    @PostMapping("/detect")
    public Result<BizDetection> detect(
            @Parameter(description = "上传的图片文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "关联的运输单ID") @RequestParam(value = "transportId", required = false) Long transportId)
            throws IOException {
        return Result.success(bizDetectionService.detect(file, transportId));
    }
}