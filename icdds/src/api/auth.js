// 认证相关 API
import http from '@/utils/request'
// 登录
export const loginApi = (data) => http.post('/auth/login', data)
// 获取用户信息（包含角色、权限等）
export const getUserInfoApi = () => http.get('/auth/me')
// 获取动态路由（菜单）
export const getRouters = () => http.get('/auth/routers')
// 注销
export const logoutApi = () => http.post('/auth/logout')
