package com.app.springbootcargovision.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Date;

/**
 * 系统备份实体 (MyBatis 版)
 */
@Data
@Schema(description = "系统备份")
public class SysBackup implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键 ID")
    private Long id;

    /** 备份名称 */
    @Schema(description = "备份名称")
    private String name;

    /** 文件路径 */
    @Schema(description = "文件路径")
    private String path;

    /** 文件大小 (字节) */
    @Schema(description = "文件大小 (字节)")
    private Long size;

    /** 创建时间 */
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /** 状态 (SUCCESS/FAIL) */
    @Schema(description = "状态 (SUCCESS/FAIL)")
    private String status;

    /** 错误信息 */
    @Schema(description = "错误信息")
    private String errorMsg;

    /** 版本号 */
    @Schema(description = "版本号")
    private String version;

    /** 操作人 */
    @Schema(description = "操作人")
    private String operator;

    /** 删除标志 (0代表存在 1代表删除) */
    @Schema(description = "删除标志 (0代表存在 1代表删除)")
    private Integer delFlag;
}
