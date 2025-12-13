package com.app.springbootcargovision.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 货物类型实体类
 * 对应数据库表: base_goods_type
 */
@Data
@Schema(description = "货物类型实体")
public class BaseGoodsType {
    @Schema(description = "主键 ID")
    private Long id;

    @Schema(description = "类型名称")
    private String typeName;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "创建时间", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;
}
