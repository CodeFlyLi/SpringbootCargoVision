package com.app.springbootcargovision.service.impl;

import com.app.springbootcargovision.mapper.SysPermissionMapper;
import com.app.springbootcargovision.mapper.SysUserMapper;
import com.app.springbootcargovision.model.LoginUser;
import com.app.springbootcargovision.model.SysPermission;
import com.app.springbootcargovision.model.SysRole;
import com.app.springbootcargovision.model.SysUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper sysUserMapper;
    private final SysPermissionMapper sysPermissionMapper;

    public UserDetailsServiceImpl(SysUserMapper sysUserMapper, SysPermissionMapper sysPermissionMapper) {
        this.sysUserMapper = sysUserMapper;
        this.sysPermissionMapper = sysPermissionMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        SysUser sysUser = sysUserMapper.selectLoginUser(username);
        if (sysUser == null) {
            throw new UsernameNotFoundException("用户不存在");
        }

        List<GrantedAuthority> authorities = new ArrayList<>();

        if (sysUser.getRoles() != null) {

            for (SysRole role : sysUser.getRoles()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getRoleCode()));
            }
        }

        List<SysPermission> permissions = sysPermissionMapper.selectByUserId(sysUser.getId());
        for (SysPermission perm : permissions) {
            if (perm.getCode() != null && !perm.getCode().isEmpty()) {
                authorities.add(new SimpleGrantedAuthority(perm.getCode()));
            }
        }

        return new LoginUser(sysUser, authorities);
    }
}
