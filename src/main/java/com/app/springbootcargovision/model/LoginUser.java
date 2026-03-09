package com.app.springbootcargovision.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import lombok.Getter;

import java.util.Collection;

/**
 * Spring Security 登录用户对象
 * 扩展 UserDetails，包含系统用户信息
 */
public class LoginUser extends User {
    @Getter
    private SysUser sysUser;

    public LoginUser(SysUser sysUser, Collection<? extends GrantedAuthority> authorities) {
        // 调用父类全参构造函数
        // boolean enabled: status == 1 (正常)
        // boolean accountNonExpired: true
        // boolean credentialsNonExpired: true
        // boolean accountNonLocked: true
        super(sysUser.getUsername(),
                sysUser.getPassword(),
                sysUser.getStatus() != null && sysUser.getStatus() == 1,
                true,
                true,
                true,
                authorities);
        this.sysUser = sysUser;
    }
}
