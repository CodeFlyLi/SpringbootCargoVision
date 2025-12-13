package com.app.springbootcargovision.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统权限实体类
 * 对应数据库表: sys_permissions
 */
@Data
@Schema(description = "系统权限")
public class SysPermission {
    @Schema(description = "主键 ID")
    private Long id;
    
    @Schema(description = "父级权限 ID")
    private Long parentId;
    
    @Schema(description = "权限名称")
    @NotBlank(message = "权限名称不能为空")
    @Size(max = 50, message = "权限名称不能超过50字符")
    private String name;
    
    @Schema(description = "权限标识")
    @NotBlank(message = "权限标识不能为空")
    @Size(max = 50, message = "权限标识不能超过50字符")
    private String code;
    
    @Schema(description = "类型 (1-菜单, 2-按钮/操作)")
    @NotNull(message = "权限类型不能为空")
    private Integer type;
    
    @Schema(description = "路由路径")
    private String path;
    
    @Schema(description = "组件路径")
    private String component;
    
    @Schema(description = "图标")
    private String icon;
    
    @Schema(description = "排序号")
    private Integer sortOrder;
    
    @Schema(description = "创建时间", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;
    
    @Schema(description = "更新时间", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;

    // --- 非数据库字段 ---
    @Schema(description = "子权限列表", accessMode = Schema.AccessMode.READ_ONLY)
    private List<SysPermission> children;
}
