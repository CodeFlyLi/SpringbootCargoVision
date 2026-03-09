package com.app.springbootcargovision.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 货物检测图片实体类 (Entity Layer)
 * 对应数据库表: biz_detection_images
 *
 * 【作用】
 * 存储单次检测中包含的每一张原始图片及其对应的 AI 识别结果。
 * 一次检测 (BizDetection) 可能对应多张图片 (BizDetectionImage)。
 */
@Data
@Schema(description = "货物检测图片信息 (子表实体)")
public class BizDetectionImage implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键 ID")
    private Long id;

    @Schema(description = "关联的主检测记录 ID (外键)")
    private Long detectionId;

    @Schema(description = "原始图片存储路径/URL (未处理的原图)")
    private String originalUrl;

    @Schema(description = "AI 处理后的图片路径/URL (包含检测框和标注)")
    private String processedUrl;

    @Schema(description = "单张图片的破损等级: 0-无, 1-轻微, 2-中度, 3-严重")
    private Integer damageLevel;

    @Schema(description = "单图破损类型描述 (如: 划痕, 凹陷)")
    private String damageType;

    @Schema(description = "AI置信度 (0.0 - 1.0)")
    private BigDecimal confidence;

    @Schema(description = "排序号 (用于前端展示顺序)")
    private Integer sortOrder;

    @Schema(description = "AI 检测出的边框位置信息 (JSON 格式)")
    private String boundingBoxes;

    @Schema(description = "图片记录创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "Base64 图片数据 (仅用于接收前端上传的修改后图片)", accessMode = Schema.AccessMode.WRITE_ONLY)
    private transient String base64Image;
}
