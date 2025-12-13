package com.app.springbootcargovision.model;

import com.app.springbootcargovision.utils.DataMaskingSerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import java.time.LocalDateTime;

/**
 * 用户表
 */
@Data
@Schema(description = "系统用户")
public class SysUser {
    @Schema(description = "主键 ID")
    private Long id;

    @Schema(description = "用户名 (登录账号)")
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 50, message = "用户名长度必须在2-50字符之间")
    private String username;

    @Schema(description = "身份证号 (登录账号)")
    @JsonSerialize(using = DataMaskingSerializer.IdCardMaskingSerializer.class)
    private String idCard;

    /**
     * 标准 Setter: 身份证号
     * 处理空字符串，将其转换为 null
     */
    public void setIdCard(String idCard) {
        this.idCard = (idCard != null && idCard.trim().isEmpty()) ? null : idCard;
    }

    @Schema(description = "密码 (加密存储)", accessMode = Schema.AccessMode.WRITE_ONLY)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Schema(description = "真实姓名")
    @Size(max = 50, message = "姓名长度不能超过50字符")
    private String name;

    @Schema(description = "头像 URL")
    private String avatar;

    @Schema(description = "电子邮箱")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(description = "手机号码")
    @JsonSerialize(using = DataMaskingSerializer.PhoneMaskingSerializer.class)
    private String phone;

    /**
     * 标准 Setter: 手机号码
     * 处理空字符串，将其转换为 null
     */
    public void setPhone(String phone) {
        this.phone = (phone != null && phone.trim().isEmpty()) ? null : phone;
    }

    @Schema(description = "状态: 1-正常, 0-禁用")
    private Integer status;

    @Schema(description = "最后一次重置密码时间", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime lastPasswordResetTime;

    @Schema(description = "最后登录时间", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime lastLoginTime;

    @Schema(description = "最后登录 IP", accessMode = Schema.AccessMode.READ_ONLY)
    private String lastLoginIp;

    @Schema(description = "逻辑删除标记: 0-未删除, 1-已删除", hidden = true)
    private Integer isDeleted;

    @Schema(description = "创建时间", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;

    // --- 非数据库字段 (业务展示用) ---
    @Schema(description = "搜索条件: 角色ID", accessMode = Schema.AccessMode.WRITE_ONLY)
    private Long roleId;

    @Schema(description = "用户角色列表", accessMode = Schema.AccessMode.READ_ONLY)
    // 标记为只读，避免默认反序列化器尝试将字符串强转为 List 导致报错
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private java.util.List<SysRole> roles;

    // --- 扩展字段 ---
    @Schema(description = "角色名称字符串 (兼容前端)", accessMode = Schema.AccessMode.READ_ONLY)
    public String getRoleName() {
        if (roles != null && !roles.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (SysRole role : roles) {
                if (sb.length() > 0)
                    sb.append(",");
                sb.append(role.getRoleName());
            }
            return sb.toString();
        }
        return null;
    }

    @Schema(description = "状态名称 (兼容前端)", accessMode = Schema.AccessMode.READ_ONLY)
    public String getStatusName() {
        return status != null && status == 1 ? "正常" : "禁用";
    }

    // --- 兼容性 Setters (处理前端传递的 String 类型数据) ---

    /**
     * 标准 Setter: 状态
     * 手动添加以避免 Lombok 因重载而跳过生成
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * 兼容性 Setter: 状态 (处理字符串)
     */
    public void setStatus(String status) {
        if ("正常".equals(status)) {
            this.status = 1;
        } else if ("禁用".equals(status)) {
            this.status = 0;
        } else {
            try {
                this.status = Integer.parseInt(status);
            } catch (NumberFormatException e) {
                // 忽略非数字且非特定关键词的字符串
            }
        }
    }

    /**
     * 标准 Setter: 角色ID
     * 手动添加以避免 Lombok 因重载而跳过生成
     */
    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    /**
     * 兼容性 Setter: 角色ID (处理字符串)
     */
    public void setRoleId(String roleId) {
        try {
            this.roleId = Long.parseLong(roleId);
        } catch (NumberFormatException e) {
            // 忽略非数字字符串
        }
    }

    /**
     * 自定义反序列化逻辑：处理前端可能传递的异常格式 roles 数据
     * 正常情况下前端应传 roleId，但如果传了 roles="1" 这种字符串，在此兼容
     */
    @com.fasterxml.jackson.annotation.JsonSetter("roles")
    public void setRolesFromJson(Object roles) {
        if (roles instanceof String) {
            try {
                // 如果前端把 ID 放在 roles 字段里传过来（字符串格式）
                this.roleId = Long.parseLong((String) roles);
            } catch (NumberFormatException e) {
                // 忽略非数字
            }
        } else if (roles instanceof Number) {
            // 如果前端把 ID 放在 roles 字段里传过来（数字格式）
            this.roleId = ((Number) roles).longValue();
        } else if (roles instanceof java.util.List) {
            // 正常的 List<SysRole> 格式（如果有的话），虽然通常用于展示
            this.roles = (java.util.List<SysRole>) roles;
        }
    }

    @Schema(description = "用户权限列表", accessMode = Schema.AccessMode.READ_ONLY)
    private java.util.List<String> permissions;
}
