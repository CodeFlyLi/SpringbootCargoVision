package com.app.springbootcargovision.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "字典项")
public class DictItem implements Serializable {
    @Schema(description = "标签")
    private String label;

    @Schema(description = "值")
    private Object value;

    @Schema(description = "类型 (如: primary, success, warning, danger)")
    private String type;

    @Schema(description = "描述")
    private String description;
}
