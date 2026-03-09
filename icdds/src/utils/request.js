import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

/**
 * Axios HTTP 请求封装模块 (utils/request.js)
 * 
 * 作用：
 * 1. 统一创建 Axios 实例，配置基础 URL 和超时时间。
 * 2. 请求拦截器：自动在请求头中添加 JWT Token。
 * 3. 响应拦截器：统一处理后端返回的状态码和错误信息。
 * 4. 封装常用的 HTTP 方法 (get, post, upload, download 等)。
 */

// 创建 Axios 实例
const instance = axios.create({
  // 基础 URL，所有请求都会自动加上这个前缀
  // 例如：/auth/login -> /api/v1/auth/login
  // 这里的 /api/v1 通常在 vite.config.js 中配置了代理 (proxy) 转发到后端服务器
  baseURL: '/api/v1',
  // 请求超时时间 (毫秒)，超过这个时间请求会被中断
  timeout: 15000,
})

/**
 * 请求拦截器 (Request Interceptor)
 * 
 * 作用：在请求发送给服务器之前，对请求配置进行修改。
 * 主要任务：
 * 1. 检查本地是否有 Token。
 * 2. 如果有 Token 且不是登录请求，则将其添加到 Authorization 请求头中。
 */
instance.interceptors.request.use(
  (config) => {
    // 判断当前请求是否为登录接口 (登录接口不需要携带 Token)
    const isLogin = typeof config.url === 'string' && config.url.includes('/auth/login')
    
    // 从 localStorage 获取 Token
    const token = localStorage.getItem('Authorization')
    
    // 如果存在 Token 且不是登录请求，则注入 Header
    if (token && !isLogin) {
      // 规范格式：Bearer <token>
      config.headers.Authorization = token.startsWith('Bearer ') ? token : `Bearer ${token}`
    }
    return config
  },
  (error) => {
    // 请求配置出错时的处理
    return Promise.reject(error)
  },
)

/**
 * 响应拦截器 (Response Interceptor)
 * 
 * 作用：在接收到服务器响应后，对数据进行预处理。
 * 主要任务：
 * 1. 判断业务状态码 (code)。如果不是 200，视为业务失败，统一弹出错误提示。
 * 2. 处理 HTTP 状态码 (status)。如 401 (未授权)、403 (禁止访问)、500 (服务器错误)。
 * 3. 提取响应体中的有效数据 (data.data)，简化组件层的调用代码。
 */
instance.interceptors.response.use(
  (response) => {
    const data = response.data
    
    // 如果响应是对象（通常是 JSON），进行业务状态码检查
    if (data && typeof data === 'object') {
      // 假设后端约定的成功状态码为 200
      if (data.code && data.code !== 200) {
        // 业务失败，弹出后端返回的错误信息
        ElMessage.error(data.message || '请求失败')
        return Promise.reject(data)
      }
      // 业务成功，直接返回数据部分
      // 兼容后端可能直接返回 data 字段，或者把数据铺平在根对象的情况
      return data.data !== undefined ? data.data : data
    }
    // 如果响应不是 JSON（如文件流），直接返回整个响应对象
    return response
  },
  (error) => {
    // 处理 HTTP 层面错误 (状态码非 2xx)
    const status = error?.response?.status
    const data = error?.response?.data
    
    if (status === 401) {
      // 401 Unauthorized: Token 失效或未登录
      const message = data?.message || '未授权或登录已过期'
      ElMessage.error(message)
      
      // 清除本地存储的所有用户相关信息
      localStorage.removeItem('Authorization')
      localStorage.removeItem('user_info')
      localStorage.removeItem('user_roles')
      localStorage.removeItem('user_permissions')
      
      // 强制跳转回登录页，让用户重新登录
      router.push('/login')
    } else if (status === 403) {
      // 403 Forbidden: 已登录但无权限
      ElMessage.error(data?.message || '没有访问权限或账号已禁用')
    } else if (status >= 400) {
      // 其他 4xx/5xx 错误
      // 优先显示后端返回的具体错误信息
      const message = data?.message || (status >= 500 ? '服务器错误' : '请求错误')
      ElMessage.error(message)
    } else {
      // 网络错误 (如断网、超时)
      ElMessage.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  },
)

// 封装常用 HTTP 方法
const get = (url, params) => instance.get(url, { params })
const post = (url, data) => instance.post(url, data)
const put = (url, data) => instance.put(url, data)
const patch = (url, data) => instance.patch(url, data)
const del = (url, data) => instance.delete(url, { data })

/**
 * 文件上传方法
 * 
 * @param {string} url 上传接口地址
 * @param {File} file 文件对象
 * @param {Object} fields 额外的表单字段 (可选)
 */
const upload = (url, file, fields = {}) => {
  const formData = new FormData()
  // 添加文件字段
  formData.append('file', file)
  // 添加其他普通字段
  Object.entries(fields || {}).forEach(([k, v]) => {
    if (v !== undefined && v !== null) formData.append(k, v)
  })
  
  return instance.post(url, formData, {
    // 手动设置 Content-Type 为 multipart/form-data
    // 注意：虽然浏览器会自动设置 boundary，但显式声明 header 是个好习惯
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

/**
 * 批量文件上传方法
 * 
 * @param {string} url 上传接口地址
 * @param {Array<File>} files 文件对象数组
 * @param {Object} fields 额外的表单字段 (可选)
 */
const uploadBatch = (url, files, fields = {}) => {
  const formData = new FormData()
  
  // 遍历文件数组，使用相同的键名 'files' 添加所有文件
  // Spring Boot 后端可以使用 List<MultipartFile> files 接收
  if (Array.isArray(files)) {
    files.forEach((file) => {
      formData.append('files', file)
    })
  }

  // 添加其他字段
  Object.entries(fields || {}).forEach(([k, v]) => {
    if (v !== undefined && v !== null) formData.append(k, v)
  })

  return instance.post(url, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

/**
 * 文件下载方法 (GET)
 * 
 * 适用于后端直接返回文件流的接口
 * 自动设置 responseType: 'blob'
 */
const download = (url, params) =>
  instance.get(url, { params, responseType: 'blob' }).then((blob) => {
    return blob
  })

/**
 * 文件下载方法 (POST)
 * 
 * 适用于需要传递复杂参数 (如 JSON Body) 才能下载文件的接口
 */
const downloadPost = (url, data) =>
  instance.post(url, data, { responseType: 'blob' }).then((blob) => {
    return blob
  })

// 导出所有封装的方法供组件使用
export default { get, post, put, patch, delete: del, upload, uploadBatch, download, downloadPost }
