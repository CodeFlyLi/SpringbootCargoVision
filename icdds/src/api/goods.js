import http from '@/utils/request'

/**
 * 获取货物列表
 * @param {Object} params - 查询参数 { page, size, goodsNo, name, typeId }
 */
export const getGoods = (params) => http.get('/goods', params)

/**
 * 获取货物类型列表
 */
export const getGoodsTypes = () => http.get('/goods/types')

/**
 * 获取货物详情
 * @param {string|number} id - 货物ID
 */
export const getGoodsDetail = (id) => http.get(`/goods/${id}`)

/**
 * 创建货物
 * @param {Object} data - 货物信息
 */
export const createGoods = (data) => http.post('/goods', data)

/**
 * 更新货物信息
 * @param {string|number} id - 货物ID
 * @param {Object} data - 更新的货物信息
 */
export const updateGoods = (id, data) => http.put(`/goods/${id}`, data)

/**
 * 删除货物
 * @param {string|number} id - 货物ID
 */
export const deleteGoods = (id) => http.delete(`/goods/${id}`)
