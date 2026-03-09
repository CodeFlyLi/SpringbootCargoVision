import http from '@/utils/request'

/**
 * 获取运输任务列表
 * @param {Object} params - 查询参数 { page, size, transportNo, status }
 */
export const getTransports = (params) => http.get('/transports', params)

/**
 * 获取运输任务详情
 * @param {string|number} id - 运输任务ID
 */
export const getTransportDetail = (id) => http.get(`/transports/${id}`)

/**
 * 创建运输任务
 * @param {Object} data - 运输任务信息
 */
export const createTransport = (data) => http.post('/transports', data)

/**
 * 更新运输任务
 * @param {string|number} id - 运输任务ID
 * @param {Object} data - 更新的信息
 */
export const updateTransport = (id, data) => http.put(`/transports/${id}`, data)

/**
 * 删除运输任务
 * @param {string|number} id - 运输任务ID
 */
export const deleteTransport = (id) => http.delete(`/transports/${id}`)

/**
 * 开始运输
 * @param {string|number} id - 运输任务ID
 */
export const startTransport = (id) => http.post(`/transports/${id}/start`)

/**
 * 完成运输
 * @param {string|number} id - 运输任务ID
 */
export const completeTransport = (id) => http.post(`/transports/${id}/complete`)

/**
 * 取消运单
 * @param {string|number} id - 运输任务ID
 */
export const cancelTransport = (id) => http.post(`/transports/${id}/cancel`)
