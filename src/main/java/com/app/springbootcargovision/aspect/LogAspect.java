package com.app.springbootcargovision.aspect;

import com.alibaba.fastjson2.JSON;
import com.app.springbootcargovision.annotation.Log;
import com.app.springbootcargovision.model.LoginUser;
import com.app.springbootcargovision.model.SysLog;
import com.app.springbootcargovision.model.SysPermission;
import com.app.springbootcargovision.service.SysLogService;
import com.app.springbootcargovision.service.SysPermissionService;
import com.app.springbootcargovision.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;
import java.util.List;
import java.net.URL;

/**
 * 操作日志记录处理
 */
@Aspect
@Component
public class LogAspect {
    private static final Logger log = LoggerFactory.getLogger(LogAspect.class);

    private final SysLogService sysLogService;
    private final SysPermissionService sysPermissionService;

    public LogAspect(SysLogService sysLogService, SysPermissionService sysPermissionService) {
        this.sysLogService = sysLogService;
        this.sysPermissionService = sysPermissionService;
    }

    /**
     * 处理完请求后执行
     *
     * @param joinPoint 切点
     */
    @AfterReturning(pointcut = "@annotation(controllerLog)", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, Log controllerLog, Object jsonResult) {
        handleLog(joinPoint, controllerLog, null, jsonResult);
    }

    /**
     * 拦截异常操作
     * 
     * @param joinPoint 切点
     * @param e         异常
     */
    @AfterThrowing(value = "@annotation(controllerLog)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Log controllerLog, Exception e) {
        handleLog(joinPoint, controllerLog, e, null);
    }

    protected void handleLog(final JoinPoint joinPoint, Log controllerLog, final Exception e, Object jsonResult) {
        try {
            // 获取当前登录用户
            String username = "Unknown";
            try {
                LoginUser currentUser = SecurityUtils.getLoginUser();
                if (currentUser != null) {
                    username = currentUser.getUsername();
                }
            } catch (Exception ex) {
                // 忽略未登录情况
            }

            SysLog sysLog = new SysLog();
            sysLog.setStatus("success"); // 默认成功
            sysLog.setCreateTime(new Date());
            sysLog.setOperator(username);

            StringBuilder descBuilder = new StringBuilder();

            // 请求IP
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                sysLog.setIp(getClientIp(request));
                descBuilder.append("URL: ").append(request.getRequestURI()).append("; ");
                descBuilder.append("HTTP Method: ").append(request.getMethod()).append("; ");
            }

            if (e != null) {
                sysLog.setStatus("fail");
                sysLog.setErrorMsg(e.getMessage());
            }

            // 设置方法名称
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();
            descBuilder.append("Class Method: ").append(className).append(".").append(methodName).append("; ");

            // 处理注解上的参数
            if (controllerLog != null) {
                String moduleName = controllerLog.module();

                if (attributes != null) {
                    try {
                        HttpServletRequest request = attributes.getRequest();
                        String referer = request.getHeader("Referer");
                        if (referer != null) {
                            // 解析 Referer 获取路径
                            URL url = new URL(referer);
                            String path = url.getPath();

                            // 查询对应的权限/菜单
                            // 尝试1: 精确匹配 (e.g. /transports)
                            SysPermission query = new SysPermission();
                            query.setPath(path);
                            List<SysPermission> permissions = sysPermissionService.getPermissionList(query);

                            // 尝试2: 去除开头的斜杠匹配 (e.g. transports)
                            if (permissions.isEmpty() && path.startsWith("/") && path.length() > 1) {
                                query.setPath(path.substring(1));
                                permissions = sysPermissionService.getPermissionList(query);
                            }

                            if (!permissions.isEmpty()) {
                                // 找到对应的菜单，使用数据库中的名称
                                moduleName = permissions.get(0).getName();
                            } else {
                                log.debug("No permission found for path: {}", path);
                            }
                        }
                    } catch (Exception ex) {
                        // 忽略
                    }
                }

                sysLog.setModule(moduleName);
                sysLog.setType(controllerLog.type());

                if (controllerLog.isSaveRequestData()) {
                    Object[] args = joinPoint.getArgs();
                    try {
                        String params = JSON.toJSONString(args);
                        // 简单的截断，防止description过长
                        if (params != null && params.length() > 200) {
                            params = params.substring(0, 200) + "...";
                        }
                        descBuilder.append("Params: ").append(params);
                    } catch (Exception ex) {
                        // 忽略参数序列化错误
                    }
                }
            }

            // 设置描述，确保不超过数据库长度限制
            String description = descBuilder.toString();
            if (description.length() > 490) {
                description = description.substring(0, 490) + "...";
            }
            sysLog.setDescription(description);

            // 保存数据库
            sysLogService.saveLog(sysLog);
        } catch (Exception exp) {
            // 记录本地异常日志
            log.error("==前置通知异常==");
            log.error("异常信息:{}", exp.getMessage());
            exp.printStackTrace();
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
