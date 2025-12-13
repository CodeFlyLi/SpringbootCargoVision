package com.app.springbootcargovision.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 统一 API 响应结果封装类
 * 所有的 Controller 接口都应该返回这个类的对象，以便前端统一处理
 * 
 * @param <T> 数据载荷的类型
 */
@Data
@Schema(description = "统一响应结果")
public class Result<T> {
    @Schema(description = "状态码: 200-成功, 其他-失败")
    private Integer code;

    @Schema(description = "提示信息")
    private String message;

    @Schema(description = "数据载荷")
    private T data;

    /**
     * 成功响应（无数据）
     */
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("成功");
        return result;
    }

    /**
     * 成功响应（带数据）
     * 
     * @param data 返回的数据
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("成功");
        result.setData(data);
        return result;
    }

    /**
     * 失败响应（自定义状态码和消息）
     * 
     * @param code    错误码
     * @param message 错误信息
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    /**
     * 失败响应（默认 500 错误）
     * 
     * @param message 错误信息
     */
    public static <T> Result<T> error(String message) {
        return error(500, message);
    }
}
