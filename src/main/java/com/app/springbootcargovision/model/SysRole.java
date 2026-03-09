package com.app.springbootcargovision.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统角色实体类
 * 对应数据库表: sys_role
 */
@Data
@Schema(description = "系统角色")
public class SysRole {
    @Schema(description = "主键 ID")
    private Long id;

    @Schema(description = "角色名称")
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称不能超过50字符")
    private String roleName;

    @Schema(description = "角色编码")
    @NotBlank(message = "角色编码不能为空")
    @Size(max = 50, message = "角色编码不能超过50字符")
    private String roleCode;

    @Schema(description = "描述信息")
    @Size(max = 200, message = "描述信息不能超过200字符")
    private String description;

    @Schema(description = "创建时间", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;

    // --- 非数据库字段 ---
    @Schema(description = "角色拥有的权限列表", accessMode = Schema.AccessMode.READ_ONLY)
    private List<SysPermission> permissions;

    // --- 兼容性字段 ---
    @Schema(description = "角色名称 (兼容前端)", accessMode = Schema.AccessMode.READ_ONLY)
    public String getName() {
        return roleName;
    }
}
