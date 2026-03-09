package com.app.springbootcargovision.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 货物运输记录实体类 (Entity Layer)
 * 对应数据库表: biz_transport
 *
 * 【业务定位】
 * 运输单是系统的基础业务单据，记录了货物的流动过程。
 * 每一个“破损检测记录 (BizDetection)”都必须关联到一个具体的“运输单”。
 *
 * 【主要功能】
 * 1. 记录货物从起点到终点的运输详情 (车辆、司机、时间)。
 * 2. 关联具体的货物信息 (Goods)。
 * 3. 跟踪运输状态 (待运输 -> 运输中 -> 已送达)。
 */
@Data
@Schema(description = "货物运输记录 (基础业务单据)")
public class BizTransport {
    @Schema(description = "主键 ID")
    private Long id;

    @Schema(description = "运输单号 (唯一标识, 业务检索主键)")
    @NotBlank(message = "运输单号不能为空")
    @Size(max = 50, message = "运输单号不能超过50字符")
    private String transportNo;

    @Schema(description = "关联货物 ID (外键关联 biz_goods 表)")
    @NotNull(message = "货物ID不能为空")
    private Long goodsId;

    @Schema(description = "运输数量 (单位由货物信息决定)")
    @Min(value = 1, message = "运输数量至少为1")
    private Integer quantity;

    @Schema(description = "起始地 (发货地址)")
    @NotBlank(message = "起始地不能为空")
    @Size(max = 100, message = "起始地不能超过100字符")
    private String origin;

    @Schema(description = "目的地 (收货地址)")
    @NotBlank(message = "目的地不能为空")
    @Size(max = 100, message = "目的地不能超过100字符")
    private String destination;

    @Schema(description = "承运商 (物流公司名称)")
    @Size(max = 100, message = "承运商不能超过100字符")
    private String carrier;

    @Schema(description = "运输车辆车牌号")
    @Size(max = 50, message = "车牌号不能超过50字符")
    private String vehicleNo;

    @Schema(description = "司机姓名")
    @Size(max = 50, message = "司机姓名不能超过50字符")
    private String driverName;

    @Schema(description = "司机电话 (用于紧急联系)")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "司机电话格式不正确")
    private String driverPhone;

    @Schema(description = "预计/实际发货时间")
    private LocalDateTime startTime;

    @Schema(description = "预计/实际到达时间")
    private LocalDateTime endTime;

    @Schema(description = "运输状态: 0-待运输, 1-运输中, 2-已送达, 3-已取消")
    private Integer status;

    @Schema(description = "备注信息 (可存储异常情况简述)")
    private String remarks;

    @Schema(description = "逻辑删除标记: 0-未删除, 1-已删除", hidden = true)
    private Integer isDeleted;

    @Schema(description = "创建人ID", hidden = true)
    private Long createdBy;

    @Schema(description = "创建时间", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;

    @Schema(description = "客户名称/收货人")
    private String customerName;

    // --- 非数据库字段 (业务展示用) ---
    @Schema(description = "货物名称", accessMode = Schema.AccessMode.READ_ONLY)
    private String goodsName;

    @Schema(description = "货物编号", accessMode = Schema.AccessMode.READ_ONLY)
    private String goodsNo;
}
