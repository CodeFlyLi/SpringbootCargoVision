package com.app.springbootcargovision.exception;

import com.app.springbootcargovision.common.Result;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 捕获并统一处理 Controller 层抛出的异常
 */
@RestControllerAdvice(basePackages = "com.app.springbootcargovision.controller")
public class GlobalExceptionHandler {

    /**
     * 处理账号禁用异常
     */
    @ExceptionHandler(DisabledException.class)
    public Result<String> handleDisabledException(DisabledException e) {
        return Result.error(403, "账号已被禁用，请联系管理员");
    }

    /**
     * 处理账号锁定异常
     */
    @ExceptionHandler(LockedException.class)
    public Result<String> handleLockedException(LockedException e) {
        return Result.error(403, "账号已被锁定，请联系管理员");
    }

    /**
     * 处理用户名或密码错误异常
     */
    @ExceptionHandler(BadCredentialsException.class)
    public Result<String> handleBadCredentialsException(BadCredentialsException e) {
        return Result.error(401, "用户名或密码错误");
    }

    /**
     * 处理所有未捕获的异常
     * 
     * @param e 异常对象
     * @return 包含错误信息的统一返回结果
     */
    // 限定在业务 Controller 范围内处理异常，避免影响 /v3/api-docs 等框架端点
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        // 打印堆栈信息以便排查
        e.printStackTrace();
        return Result.error(500, e.getMessage() != null ? e.getMessage() : "系统内部错误");
    }

    /**
     * 处理运行时异常
     * 通常用于捕获业务逻辑中抛出的 RuntimeException
     * 
     * @param e 运行时异常对象
     * @return 包含错误信息的统一返回结果
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<String> handleRuntimeException(RuntimeException e) {
        e.printStackTrace();
        return Result.error(500, e.getMessage());
    }
}
