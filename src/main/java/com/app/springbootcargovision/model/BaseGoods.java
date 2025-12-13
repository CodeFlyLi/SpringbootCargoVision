package com.app.springbootcargovision.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 货物基本信息实体类
 * 对应数据库表: base_goods
 */
@Data
@Schema(description = "货物基本信息")
public class BaseGoods {
    @Schema(description = "主键 ID")
    private Long id;

    @Schema(description = "货物编号 (唯一标识)")
    @NotBlank(message = "货物编号不能为空")
    @Size(max = 50, message = "货物编号不能超过50字符")
    private String goodsNo;

    @Schema(description = "货物名称")
    @NotBlank(message = "货物名称不能为空")
    @Size(max = 100, message = "货物名称不能超过100字符")
    private String name;

    @Schema(description = "货物类型 ID")
    @NotNull(message = "货物类型不能为空")
    private Long typeId;

    @Schema(description = "货物图片 URL")
    private String imageUrl;

    @Schema(description = "计量单位")
    @Size(max = 20, message = "计量单位不能超过20字符")
    private String unit;

    @Schema(description = "重量 (kg)")
    @DecimalMin(value = "0.0", inclusive = false, message = "重量必须大于0")
    private BigDecimal weight;

    @Schema(description = "体积 (m³)")
    @DecimalMin(value = "0.0", inclusive = false, message = "体积必须大于0")
    private BigDecimal volume;

    @Schema(description = "价值 (元)")
    @DecimalMin(value = "0.0", inclusive = false, message = "价值必须大于0")
    private BigDecimal value;

    @Schema(description = "库存数量")
    @Min(value = 0, message = "库存数量不能为负数")
    private Integer stock;

    @Schema(description = "生产厂家")
    @Size(max = 100, message = "生产厂家不能超过100字符")
    private String manufacturer;

    @Schema(description = "规格型号")
    @Size(max = 255, message = "规格型号不能超过255字符")
    private String specifications;

    @Schema(description = "描述信息")
    private String description;

    @Schema(description = "逻辑删除标记: 0-未删除, 1-已删除", hidden = true)
    private Integer isDeleted;

    @Schema(description = "创建人ID")
    private Long createdBy;

    @Schema(description = "创建时间", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;

    // --- 非数据库字段 (业务展示用) ---
    @Schema(description = "货物类型名称", accessMode = Schema.AccessMode.READ_ONLY)
    private String typeName;
}
