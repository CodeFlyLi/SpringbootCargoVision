package com.app.springbootcargovision.controller;

import com.app.springbootcargovision.annotation.Log;
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

import com.app.springbootcargovision.model.BizDetectionImage;

/**
 * 货物破损检测管理控制器 (Controller Layer)
 *
 * 【架构定位】
 * 本类属于 Web 层（Controller），是后端服务的统一入口，主要负责：
 * 1. 接收前端发起的 HTTP 请求（GET/POST/PUT 等）。
 * 2. 解析请求参数（包括普通表单字段、JSON 数据、文件流等）。
 * 3. 参数校验（利用 @Valid 或手动校验）。
 * 4. 调用 Service 层（BizDetectionService）执行具体的业务逻辑。
 * 5. 将业务处理结果封装为统一的 Result 响应格式返回给前端。
 *
 * 【核心功能】
 * - 货物破损检测记录的增删改查（CRUD）。
 * - 核心业务：批量上传图片进行 AI 破损检测。
 * - 数据导出：支持 Excel 报表导出和 PDF 详情报告生成。
 *
 * 【开发规范】
 * - 所有接口统一通过 /api/v1/detections 路径访问。
 * - 使用 Swagger/OpenAPI 注解 (@Tag, @Operation) 生成在线接口文档。
 * - 使用自定义注解 @Log 记录关键操作日志，便于审计。
 */
@Tag(name = "货物破损检测管理", description = "提供货物破损检测的核心功能接口，包括图片上传检测、记录查询、报表导出等")
@RestController
@RequestMapping("/api/v1/detections")
public class BizDetectionController {

    // 注入业务逻辑服务层依赖
    private final BizDetectionService bizDetectionService;
    // 注入导出服务依赖
    private final ExportServiceImpl exportService;

    // 构造器注入（Spring 推荐的依赖注入方式，优于 @Autowired）
    public BizDetectionController(BizDetectionService bizDetectionService, ExportServiceImpl exportService) {
        this.bizDetectionService = bizDetectionService;
        this.exportService = exportService;
    }

    /**
     * 更新/修正检测子图详情
     *
     * 【业务场景】
     * 用户在前端对 AI 检测结果（如破损等级、类型）进行人工复核和修正。
     *
     * @param image 包含更新信息的图片对象（必须包含 id）
     * @return 成功状态
     */
    @Operation(summary = "人工修正检测子图", description = "人工修正单张子图的破损等级、类型或备注")
    @Log(module = "货物检测", type = "更新")
    @PutMapping("/image")
    public Result<Void> updateDetectionImage(@RequestBody BizDetectionImage image) {
        bizDetectionService.updateDetectionImage(image);
        return Result.success();
    }

    /**
     * 分页查询检测记录列表
     *
     * 【业务场景】
     * 前端“检测记录”页面列表展示。支持通过检测单号、运输单号、货物名称等多种条件进行筛选查询。
     *
     * 【实现逻辑】
     * 接收分页参数和查询条件 -> 调用 Service 层执行 PageHelper 分页查询 -> 返回封装好的 PageInfo 对象。
     *
     * @param page        当前页码 (默认 1)
     * @param size        每页条数 (默认 10)
     * @param detectionNo 检测单号 (支持模糊查询)
     * @param transportNo 关联运输单号 (支持模糊查询)
     * @param goodsName   货物名称 (支持模糊查询)
     * @param damageLevel 破损等级 (1-正常, 2-轻微, 3-严重)
     * @param sceneType   检测场景 (如：仓库入口、分拣线)
     * @param nodeName    物流节点名称
     * @return 统一响应对象，包含分页信息和数据列表
     */
    @Operation(summary = "分页查询检测记录", description = "根据筛选条件获取检测记录列表，包含分页信息")
    @Log(module = "货物检测", type = "查询") // 记录操作日志：模块为货物检测，类型为查询
    @GetMapping
    public Result<PageInfo<BizDetection>> getDetectionList(
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页显示条数") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "检测单号") @RequestParam(required = false) String detectionNo,
            @Parameter(description = "运输单号") @RequestParam(required = false) String transportNo,
            @Parameter(description = "货物名称") @RequestParam(required = false) String goodsName,
            @Parameter(description = "破损等级") @RequestParam(required = false) Integer damageLevel,
            @Parameter(description = "检测场景") @RequestParam(required = false) String sceneType,
            @Parameter(description = "物流节点") @RequestParam(required = false) String nodeName) {

        // 调用 Service 层查询数据
        PageInfo<BizDetection> result = bizDetectionService.getDetectionList(page, size, detectionNo, transportNo,
                goodsName, damageLevel, sceneType, nodeName);
        return Result.success(result);
    }

    /**
     * 导出检测记录为 Excel
     *
     * 【业务场景】
     * 用户点击“导出”按钮，下载当前筛选条件下的所有记录。
     * 注意：该接口直接写入 HTTP 响应流（Response Output Stream）进行文件下载，不返回 JSON 格式数据。
     *
     * @param response HTTP 响应对象，用于写入文件流
     */
    @Operation(summary = "导出所有/筛选记录", description = "将符合筛选条件的检测记录导出为 Excel 文件")
    @Log(module = "货物检测", type = "导出")
    @GetMapping("/export")
    public void exportDetections(
            HttpServletResponse response,
            @Parameter(description = "检测单号") @RequestParam(required = false) String detectionNo,
            @Parameter(description = "运输单号") @RequestParam(required = false) String transportNo,
            @Parameter(description = "货物名称") @RequestParam(required = false) String goodsName,
            @Parameter(description = "破损等级") @RequestParam(required = false) Integer damageLevel,
            @Parameter(description = "检测场景") @RequestParam(required = false) String sceneType,
            @Parameter(description = "物流节点") @RequestParam(required = false) String nodeName)
            throws IOException {

        // 调用导出服务生成 Excel 并写入响应流
        exportService.exportDetectionToExcel(response, detectionNo, transportNo, goodsName, damageLevel, sceneType,
                nodeName);
    }

    /**
     * 导出选中的检测记录
     *
     * 【业务场景】
     * 用户在表格中勾选多条记录，点击“导出选中”按钮。
     *
     * @param ids 选中的记录 ID 列表 (通过 RequestBody 接收 JSON 数组)
     */
    @Operation(summary = "导出选中记录", description = "将用户勾选的特定检测记录导出为 Excel 文件")
    @Log(module = "货物检测", type = "导出")
    @PostMapping("/export/selected")
    public void exportSelectedDetections(
            HttpServletResponse response,
            @RequestBody List<Long> ids) throws IOException {
        exportService.exportSelectedDetectionToExcel(response, ids);
    }

    /**
     * 生成单个检测记录的 PDF 报告
     *
     * 【业务场景】
     * 在查看详情时，点击“生成报告”或“打印”，下载包含详细图片和信息的 PDF 文件。
     *
     * @param id 检测记录 ID
     */
    @Operation(summary = "生成 PDF 报告", description = "为指定检测记录生成详细的 PDF 报告")
    @Log(module = "货物检测", type = "导出")
    @GetMapping("/{id}/report")
    public void generateReport(
            HttpServletResponse response,
            @Parameter(description = "检测记录ID") @PathVariable Long id) throws Exception {
        exportService.generateDetectionReportPdf(response, id);
    }

    /**
     * 获取检测记录详情
     *
     * 【业务场景】
     * 点击列表中的某一行，进入详情页面查看完整信息。
     *
     * @param id 检测记录 ID
     * @return 检测记录的完整信息实体
     */
    @Operation(summary = "获取记录详情", description = "根据 ID 获取单条检测记录的详细信息")
    @GetMapping("/{id}")
    public Result<BizDetection> getDetectionById(
            @Parameter(description = "检测记录ID") @PathVariable Long id) {
        return Result.success(bizDetectionService.getDetectionById(id));
    }

    /**
     * 创建检测记录 (手动录入)
     *
     * 【业务场景】
     * 人工补录一条检测记录（非通过图片识别自动生成）。
     *
     * @param detection 提交的检测信息表单对象
     */
    @Operation(summary = "手动创建记录", description = "人工添加一条检测记录 (非 AI 识别)")
    @Log(module = "货物检测", type = "新增")
    @PostMapping
    public Result<String> createDetection(@Valid @RequestBody BizDetection detection) {
        bizDetectionService.createDetection(detection);
        return Result.success("检测记录保存成功");
    }

    /**
     * 更新检测记录
     *
     * 【业务场景】
     * 用户修改已有的检测记录信息（例如：人工修正 AI 识别错误的等级或描述）。
     *
     * @param id        检测记录 ID
     * @param detection 修改后的信息对象
     */
    @Operation(summary = "更新检测记录", description = "修改已有检测记录信息，支持人工修正 AI 结果")
    @Log(module = "货物检测", type = "更新")
    @PutMapping("/{id}")
    public Result<Void> updateDetection(@PathVariable Long id, @RequestBody BizDetection detection) {
        detection.setId(id);
        bizDetectionService.updateDetection(detection);
        return Result.success();
    }

    /**
     * 删除检测记录
     *
     * @param id 检测记录主键 ID
     * @return 成功状态
     */
    @Operation(summary = "删除检测记录", description = "删除指定的检测记录及其关联的所有图片信息")
    @Log(module = "货物检测", type = "删除")
    @DeleteMapping("/{id}")
    public Result<Void> deleteDetection(@PathVariable Long id) {
        bizDetectionService.deleteDetection(id);
        return Result.success();
    }

    /**
     * 核心接口：批量图片 AI 检测
     *
     * 【业务场景】
     * 前端上传多张货物照片，后端调用 AI 模型进行破损检测，并返回检测结果。
     * 这是本系统最核心的业务入口。
     *
     * 【处理流程】
     * 1. 接收 MultipartFile 文件列表。
     * 2. 接收业务参数（关联运输单、检测模式等）。
     * 3. 异步/同步调用 AI 引擎分析图片。
     * 4. 保存检测结果到数据库。
     * 5. 返回识别结果给前端展示。
     *
     * @param files                 图片文件列表
     * @param transportId           关联的运输单 ID
     * @param detectionMode         检测模式 (1: 本地上传, 2: 摄像头拍摄)
     * @param detectionLocation     检测地点
     * @param responsibilitySubject 责任主体
     * @return 检测结果实体 (包含识别出的破损等级、置信度等)
     */
    @Operation(summary = "批量 AI 图片检测", description = "上传多张图片并调用 AI 模型进行破损识别")
    @Log(module = "货物检测", type = "AI检测")
    @PostMapping("/detect/batch")
    public Result<BizDetection> batchDetect(
            @Parameter(description = "图片文件列表") @RequestParam("files") List<MultipartFile> files,
            @Parameter(description = "关联运输单ID") @RequestParam(value = "transportId", required = false) Long transportId,
            @Parameter(description = "检测模式: 1-本地上传, 2-摄像头") @RequestParam(value = "detectionMode", defaultValue = "1") Integer detectionMode,
            @Parameter(description = "检测地点") @RequestParam(value = "detectionLocation", required = false) String detectionLocation,
            @Parameter(description = "责任主体") @RequestParam(value = "responsibilitySubject", required = false) String responsibilitySubject)
            throws IOException {

        // 调用 Service 层执行复杂的 AI 检测流程
        BizDetection result = bizDetectionService.batchDetect(files, transportId, detectionMode, detectionLocation,
                responsibilitySubject);
        return Result.success(result);
    }
}
