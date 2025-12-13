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

/**
 * 货物检测记录实体类
 * 对应数据库表: biz_detection
 */
@Data
@Schema(description = "货物检测记录")
public class BizDetection {
    @Schema(description = "主键 ID")
    private Long id;

    @Schema(description = "检测编号 (唯一标识)")
    @NotBlank(message = "检测编号不能为空")
    @Size(max = 50, message = "检测编号不能超过50字符")
    private String detectionNo;

    @Schema(description = "关联的运输记录 ID")
    @NotNull(message = "关联运输记录不能为空")
    private Long transportId;

    @Schema(description = "操作员用户 ID")
    private Long operatorId;

    @Schema(description = "原始图片 URL")
    private String imageUrl;

    @Schema(description = "处理后图片 URL")
    private String processedImageUrl;

    @Schema(description = "检测时间")
    private LocalDateTime detectionTime;

    @Schema(description = "破损等级: 0-无破损, 1-轻微, 2-中度, 3-严重")
    private Integer damageLevel;

    @Schema(description = "破损面积 (cm²)")
    @DecimalMin(value = "0.0", message = "破损面积必须大于等于0")
    private BigDecimal damageArea;

    @Schema(description = "置信度 (0.0 - 1.0)")
    @DecimalMin(value = "0.0", message = "置信度必须大于等于0")
    @DecimalMax(value = "1.0", message = "置信度必须小于等于1")
    private BigDecimal confidence;

    @Schema(description = "破损类型")
    @Size(max = 50, message = "破损类型不能超过50字符")
    private String damageType;

    @Schema(description = "破损描述")
    private String damageDescription;

    @Schema(description = "处理建议")
    private String suggestion;

    @Schema(description = "状态: 0-待处理, 1-已处理")
    private Integer status;

    @Schema(description = "创建时间", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;

    // --- 非数据库字段 (业务展示用) ---
    @Schema(description = "运输单号", accessMode = Schema.AccessMode.READ_ONLY)
    private String transportNo;

    @Schema(description = "货物名称", accessMode = Schema.AccessMode.READ_ONLY)
    private String goodsName;

    @Schema(description = "操作员姓名", accessMode = Schema.AccessMode.READ_ONLY)
    private String operatorName;

    @Schema(description = "原始检测框数据 (Mode B 支持)", accessMode = Schema.AccessMode.READ_ONLY)
    private java.util.List<java.util.Map<String, Object>> boxes;
}
/**
 * 这是一个非常敏锐的架构设计问题！您的直觉很准，这些字段确实有些“特殊”。
 * 
 * 简单来说， 是因为“数据库存储的高效性”和“前端展示的直观性”之间存在矛盾，我们需要这些字段来做“桥梁”。
 * 
 * 具体原因分为两类，我为您详细拆解：
 * 
 * ### 1. “关联数据”：为了不存重复数据（数据库规范）
 * 像 transportNo （运输单号）、 goodsName （货物名称）、 operatorName （操作员姓名） 属于这一类。
 * 
 * - 数据库里有吗？
 * - 有，但 不在 biz_detections （检测表）里，而在它们各自的表里（如 biz_transports 、 base_goods 、
 * sys_users ）。
 * - 在 biz_detections 表里，我们只存了 ID （如 transport_id , operator_id
 * ）。这是数据库设计的“范式”原则，为了节省空间并防止数据不一致（例如：如果货物改名了，只改货物表就行，不用把检测表全改一遍）。
 * - 为什么要加这个字段？
 * - 前端显示时，不能只显示 运输单ID: 100 ，用户看不懂。用户要看 运输单号: T20231001 。
 * - 为了方便前端，后端在查询时，会顺便去别的表把这些“名字”查出来，临时塞到这个 transportNo 字段里，一起返给前端。
 * - 结论 ：它们是“搬运工”，负责把别的表里的数据带给前端。
 * ### 2. “动态数据”：内存中临时生成的（如 boxes）
 * 像 boxes （检测框坐标数据）属于这一类。
 * 
 * - 数据库里有吗？
 * - 通常 没有 直接对应的列。
 * - 虽然我们可以把这些坐标存成一个很长的 JSON 字符串放在数据库里，但在大多数业务中，我们主要存“结果”（如图片URL、破损等级）。
 * - 为什么要加这个字段？
 * - 这是给前端 Mode B（前端绘图） 专用的。
 * - 当 AI 刚刚检测完那一瞬间，内存里有这些坐标数据。为了让前端能立即画出红框，我们把这些数据临时放到 boxes 字段里传回去。
 * - 一旦请求结束，这个数据可能就不存了（或者以其他形式如图片存了），下次再查历史记录时，可能就只给图片 URL 了。
 * - 结论 ：它是“一次性餐具”，为了当次请求的即时交互。
 * ### 总结
 * 您可以把 BizDetection 类看作是 “实体 (Entity) + 数据传输对象 (DTO)” 的合体：
 * 
 * 1. 上半部分 （数据库字段）：是 仓库里的货 ，实打实存在表里的。
 * 2. 下半部分 （非数据库字段）：是 快递包装盒 ，为了发货给前端（用户）看，临时打包加上去的标签或说明书。
 * 这样做的好处是： 代码写起来简单 ，不需要单独再写一个 BizDetectionDTO 类，直接复用同一个类，既能存数据库，又能灵活给前端发数据。
 */