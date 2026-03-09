package com.app.springbootcargovision.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Date;

/**
 * 系统日志对象
 */
@Data
@Schema(description = "系统日志")
public class SysLog implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键 ID")
    private Long id;

    /** 功能模块 */
    @Schema(description = "功能模块")
    private String module;

    /** 操作类型 */
    @Schema(description = "操作类型")
    private String type;

    /** 操作描述 */
    @Schema(description = "操作描述")
    private String description;

    /** 操作人员 */
    @Schema(description = "操作人员")
    private String operator;

    /** 操作IP */
    @Schema(description = "操作IP")
    private String ip;

    /** 状态 */
    @Schema(description = "状态")
    private String status;

    /** 错误信息 */
    @Schema(description = "错误信息")
    private String errorMsg;

    /** 操作时间 */
    @Schema(description = "操作时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
