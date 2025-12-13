package com.app.springbootcargovision.service;

import com.app.springbootcargovision.model.SysUser;

/**
 * 认证服务接口
 * 处理用户登录和获取当前用户信息的业务逻辑
 */
public interface AuthService {
    
    /**
     * 用户登录
     * 验证用户名和密码，登录成功后返回 JWT Token
     * 
     * @param username 用户名
     * @param password 密码
     * @return JWT Token 字符串
     */
    String login(String username, String password);

    /**
     * 获取当前登录用户信息
     * 基于当前请求上下文中的认证信息获取用户详情
     * 
     * @return 当前登录的用户实体对象
     */
    SysUser getCurrentUser();
}
