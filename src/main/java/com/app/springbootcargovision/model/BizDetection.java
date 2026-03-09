package com.app.springbootcargovision.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import java.util.List;
import java.util.Map;

/**
 * 货物检测记录实体类 (Entity Layer)
 * 对应数据库表: biz_detection
 *
 * 【领域模型定位】
 * 本类是货物破损检测业务的核心领域模型，聚合了单次检测的所有信息。
 * 它处于“运输单 (Transport)”与“检测图片 (DetectionImage)”之间：
 * 1. 多对一关联 BizTransport：一次运输可能有多次检测。
 * 2. 一对多关联 BizDetectionImage：一次检测包含多张图片。
 *
 * 【生命周期】
 * 1. 创建 (Created)：用户上传图片并发起检测，系统生成记录，状态为 "0-待处理"。
 * 2. 检测中 (Detecting)：调用 AI 接口分析图片（此过程通常是同步的，但也支持异步）。
 * 3. 完成 (Completed)：AI 返回结果，系统计算最大破损等级，更新状态。
 * 4. 审核 (Verified)：管理员人工复核 AI 结果（可选），修改 isVerified 状态。
 *
 * 【字段分类】
 * - 基础信息：ID, 编号, 时间
 * - 关联信息：TransportID, OperatorID
 * - 核心业务结果：DamageLevel, DamageType, Confidence
 * - V2 扩展字段：DetectionMode (采集方式), Location (地点)
 */
@Data
@Schema(description = "货物检测记录 (核心业务实体)")
public class BizDetection {
    @Schema(description = "主键 ID (自增)")
    private Long id;

    @Schema(description = "检测编号 (唯一业务标识, 格式: D + UUID前8位)")
    @NotBlank(message = "检测编号不能为空")
    @Size(max = 50, message = "检测编号不能超过50字符")
    private String detectionNo;

    @Schema(description = "关联的运输记录 ID (外键关联 biz_transport 表)")
    @NotNull(message = "关联运输记录不能为空")
    private Long transportId;

    @Schema(description = "操作员用户 ID (当前登录用户)")
    private Long operatorId;

    @Schema(description = "检测发生时间")
    private LocalDateTime detectionTime;

    @Schema(description = "综合破损等级: 0-无破损, 1-轻微, 2-中度, 3-严重 (取所有图片中最高等级)")
    private Integer damageLevel;

    @Schema(description = "预估破损面积 (cm²)")
    @DecimalMin(value = "0.0", message = "破损面积必须大于等于0")
    private BigDecimal damageArea;

    @Schema(description = "AI 识别置信度 (0.0 - 1.0, 越接近 1 越可信)")
    @DecimalMin(value = "0.0", message = "置信度必须大于等于0")
    @DecimalMax(value = "1.0", message = "置信度必须小于等于1")
    private BigDecimal confidence;

    @Schema(description = "主要破损类型 (如: 划痕, 凹陷, 破洞)")
    @Size(max = 50, message = "破损类型不能超过50字符")
    private String damageType;

    @Schema(description = "详细破损描述 (支持 AI 生成或人工备注)")
    private String damageDescription;

    @Schema(description = "AI 处理建议 (如: 建议拒收, 建议修复)")
    private String suggestion;

    @Schema(description = "业务状态: 0-待处理 (默认), 1-已处理 (人工确认)")
    private Integer status;

    @Schema(description = "记录创建时间 (自动填充)", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "记录最后更新时间 (自动填充)")
    private LocalDateTime updatedAt;

    @Schema(description = "检测场景 (如: 仓库入库, 码头卸货)")
    private String sceneType;

    @Schema(description = "物流节点名称")
    private String nodeName;

    @Schema(description = "检测图片子列表 (一对多关联)", accessMode = Schema.AccessMode.READ_ONLY)
    private List<BizDetectionImage> imageList;

    // --- 新增业务字段 (V2) ---
    @Schema(description = "图片采集方式: 1-本地文件上传, 2-摄像头实时拍摄")
    private Integer detectionMode;

    @Schema(description = "具体检测地点 (手动输入或 GPS 定位)")
    @Size(max = 100, message = "检测地点不能超过100字符")
    private String detectionLocation;

    @Schema(description = "责任主体 (如: 物流公司 A, 供应商 B)")
    @Size(max = 50, message = "责任主体不能超过50字符")
    private String responsibilitySubject;

    @Schema(description = "是否经过人工核对: 0-否 (AI 结果), 1-是 (人工已确认)")
    private Integer isVerified;

    @Schema(description = "AI 原始返回结果 (JSON 字符串备份)")
    private String originalAiResult;

    @Schema(description = "AI 原始平均置信度")
    private BigDecimal aiConfidence;

    // --- 非数据库字段 (DTO 视图字段) ---
    @Schema(description = "关联运输单号 (用于前端展示)", accessMode = Schema.AccessMode.READ_ONLY)
    private String transportNo;

    @Schema(description = "关联货物名称 (用于前端展示)", accessMode = Schema.AccessMode.READ_ONLY)
    private String goodsName;

    @Schema(description = "操作员姓名 (用于前端展示)", accessMode = Schema.AccessMode.READ_ONLY)
    private String operatorName;

    @Schema(description = "原始检测框坐标数据 (用于前端绘图)", accessMode = Schema.AccessMode.READ_ONLY)
    private List<Map<String, Object>> boxes;
}
