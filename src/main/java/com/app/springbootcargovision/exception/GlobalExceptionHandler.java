package com.app.springbootcargovision.exception;

import com.app.springbootcargovision.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.validation.FieldError;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 
 * 作用：
 * 1. 统一捕获 Controller 层抛出的各类异常，避免将原生错误堆栈直接暴露给前端
 * 2. 将异常转换为标准的 API 响应格式 (Result 对象)
 * 3. 记录异常日志，便于后端排查问题
 * 
 * 原理：
 * 使用 Spring 的 @RestControllerAdvice 注解，基于 AOP 思想拦截 Controller 方法的执行
 */
@RestControllerAdvice(basePackages = "com.app.springbootcargovision.controller")
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理参数校验异常
     * 触发场景：前端传递的参数不满足 @Valid 或 @Validated 注解定义的规则 (如 @NotNull, @Size 等)
     * 
     * @param e 参数校验异常对象
     * @return 返回 400 状态码及具体的校验错误信息
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> handleValidationExceptions(MethodArgumentNotValidException e) {
        // 提取所有字段的校验错误信息，并拼接成字符串
        String message = e.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    if (error instanceof FieldError) {
                        return ((FieldError) error).getField() + ": " + error.getDefaultMessage();
                    }
                    return error.getDefaultMessage();
                })
                .collect(Collectors.joining("; "));

        log.warn("参数校验失败: {}", message);
        return Result.error(400, "参数校验失败: " + message);
    }

    /**
     * 处理文件上传大小超限异常
     * 触发场景：上传的文件大小超过了 application.yml 中配置的 multipart.max-file-size 限制
     * 
     * @param e 文件大小超限异常
     * @return 返回 400 状态码及友好提示
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<String> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.warn("文件上传大小超限: {}", e.getMessage());
        return Result.error(400, "上传文件大小超过限制，请上传更小的文件");
    }

    /**
     * 处理账号禁用异常
     * 触发场景：用户尝试登录或访问接口时，其账号状态为禁用
     */
    @ExceptionHandler(DisabledException.class)
    public Result<String> handleDisabledException(DisabledException e) {
        return Result.error(403, "账号已被禁用，请联系管理员");
    }

    /**
     * 处理账号锁定异常
     * 触发场景：用户多次输错密码导致账号被暂时锁定
     */
    @ExceptionHandler(LockedException.class)
    public Result<String> handleLockedException(LockedException e) {
        return Result.error(403, "账号已被锁定，请联系管理员");
    }

    /**
     * 处理认证失败异常
     * 触发场景：登录时用户名或密码错误
     */
    @ExceptionHandler(BadCredentialsException.class)
    public Result<String> handleBadCredentialsException(BadCredentialsException e) {
        return Result.error(401, "用户名或密码错误");
    }

    /**
     * 处理业务逻辑运行时异常
     * 触发场景：Service 或 Controller 层抛出的 RuntimeException
     * 
     * @param e 运行时异常对象
     * @return 返回 500 状态码及错误信息
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<String> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常: ", e);
        return Result.error(500, e.getMessage() != null ? e.getMessage() : "系统运行时错误");
    }

    /**
     * 兜底异常处理
     * 作用：捕获所有未被上述方法匹配的异常，防止未处理的异常导致服务器 500 错误页面暴露
     * 
     * @param e 未知异常对象
     * @return 返回 500 状态码及"系统内部错误"提示
     */
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        log.error("系统内部错误: ", e);
        return Result.error(500, e.getMessage() != null ? e.getMessage() : "系统内部错误");
    }
}
