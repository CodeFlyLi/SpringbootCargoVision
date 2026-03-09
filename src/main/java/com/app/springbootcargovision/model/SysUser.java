package com.app.springbootcargovision.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统用户实体类
 */
@Schema(description = "系统用户")
public class SysUser implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键 ID")
    private Long id;

    @Schema(description = "用户名 (登录账号)")
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 50, message = "用户名长度必须在2-50字符之间")
    private String username;

    @Schema(description = "身份证号 (登录账号)")
    private String idCard;

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
    private String phone;

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

    @Schema(description = "搜索条件: 角色ID", accessMode = Schema.AccessMode.WRITE_ONLY)
    private Long roleId;

    @Schema(description = "用户角色列表", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<SysRole> roles;

    @Schema(description = "用户权限列表", accessMode = Schema.AccessMode.READ_ONLY)
    private List<String> permissions;

    // --- Getter 和 Setter 方法 ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = (idCard != null && idCard.trim().isEmpty()) ? null : idCard;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = (phone != null && phone.trim().isEmpty()) ? null : phone;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    /** 兼容性 Setter：处理前端传来的字符串状态 */
    public void setStatus(String status) {
        if ("正常".equals(status))
            this.status = 1;
        else if ("禁用".equals(status))
            this.status = 0;
        else {
            try {
                this.status = Integer.parseInt(status);
            } catch (Exception e) {
            }
        }
    }

    public LocalDateTime getLastPasswordResetTime() {
        return lastPasswordResetTime;
    }

    public void setLastPasswordResetTime(LocalDateTime lastPasswordResetTime) {
        this.lastPasswordResetTime = lastPasswordResetTime;
    }

    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public String getLastLoginIp() {
        return lastLoginIp;
    }

    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    /** 兼容性 Setter：处理前端传来的字符串角色ID */
    public void setRoleId(String roleId) {
        try {
            this.roleId = Long.parseLong(roleId);
        } catch (Exception e) {
        }
    }

    public List<SysRole> getRoles() {
        return roles;
    }

    public void setRoles(List<SysRole> roles) {
        this.roles = roles;
    }

    /** 优化后的导包注解：处理复杂的反序列化逻辑 */
    @JsonSetter("roles")
    public void setRolesFromJson(Object roles) {
        if (roles instanceof String) {
            try {
                this.roleId = Long.parseLong((String) roles);
            } catch (Exception e) {
            }
        } else if (roles instanceof Number) {
            this.roleId = ((Number) roles).longValue();
        } else if (roles instanceof List) {
            this.roles = (List<SysRole>) roles;
        }
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    // --- 业务展示方法 ---
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
}
