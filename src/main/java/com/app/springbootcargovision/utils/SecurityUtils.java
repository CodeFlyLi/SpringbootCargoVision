package com.app.springbootcargovision.utils;

import com.app.springbootcargovision.model.LoginUser;
import com.app.springbootcargovision.model.SysUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全服务工具类
 * 提供获取当前登录用户信息的方法
 */
public class SecurityUtils {

    /**
     * 获取当前登录用户
     */
    public static LoginUser getLoginUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
                return (LoginUser) authentication.getPrincipal();
            }
        } catch (Exception e) {
            throw new RuntimeException("获取用户信息异常", e);
        }
        return null;
    }

    /**
     * 获取当前登录用户ID
     */
    public static Long getUserId() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? loginUser.getSysUser().getId() : null;
    }

    /**
     * 获取当前登录用户信息
     */
    public static SysUser getSysUser() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? loginUser.getSysUser() : null;
    }

    /**
     * 判断当前用户是否为管理员
     * 假设管理员角色的 code 为 'admin'
     */
    public static boolean isAdmin() {
        LoginUser loginUser = getLoginUser();
        if (loginUser == null) {
            return false;
        }
        return loginUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_admin"));
    }
}
