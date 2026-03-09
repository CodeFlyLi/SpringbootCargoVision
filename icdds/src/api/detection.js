import http from '@/utils/request'

/**
 * 货物破损检测 API 接口定义 (api/detection.js)
 *
 * 作用：
 * 1. 统一管理与货物检测相关的后端接口调用。
 * 2. 封装参数传递，简化 Vue 组件中的调用代码。
 * 3. 对应后端控制器：BizDetectionController
 */

/**
 * 分页查询检测记录
 *
 * 对应后端接口：GET /api/v1/detections
 *
 * @param {Object} params - 查询参数对象
 * @param {number} params.page - 当前页码 (默认 1)
 * @param {number} params.size - 每页条数 (默认 10)
 * @param {string} [params.detectionNo] - 检测编号 (支持模糊查询)
 * @param {string} [params.transportNo] - 关联运单号
 * @param {string} [params.goodsName] - 货物名称
 * @param {number} [params.damageLevel] - 破损等级 (1:正常, 2:轻微, 3:严重)
 * @returns {Promise} 包含分页数据和记录列表的 Promise 对象
 */
export const getDetections = (params) => http.get('/detections', params)

/**
 * 获取单条检测记录详情
 *
 * 对应后端接口：GET /api/v1/detections/{id}
 *
 * @param {string|number} id - 检测记录的主键 ID
 * @returns {Promise} 返回检测记录的详细信息 (包含子图片列表)
 */
export const getDetectionDetail = (id) => http.get(`/detections/${id}`)

/**
 * 批量上传图片进行 AI 智能检测 (核心接口)
 *
 * 对应后端接口：POST /api/v1/detections/detect/batch
 *
 * @param {File[]} files - 图片文件数组 (支持多张图片同时上传)
 * @param {Object} data - 额外的业务参数
 * @param {number} data.transportId - 关联的运输单 ID (必填)
 * @param {number} data.detectionMode - 检测模式 (1:本地上传, 2:摄像头拍摄)
 * @param {string} [data.detectionLocation] - 检测地点
 * @param {string} [data.responsibilitySubject] - 责任主体
 * @returns {Promise} 返回 AI 检测结果
 */
export const detectBatchImages = (files, data = {}) =>
  http.uploadBatch('/detections/detect/batch', files, data)

/**
 * 导出检测记录 (Excel)
 *
 * 对应后端接口：GET /api/v1/detections/export
 * 注意：此接口返回的是二进制文件流 (Blob)，request.js 中的 download 方法会自动处理
 *
 * @param {Object} params - 筛选条件，与 getDetections 参数一致
 */
export const exportDetections = (params) => http.download('/detections/export', params)

/**
 * 导出选中的检测记录 (Excel)
 *
 * 对应后端接口：POST /api/v1/detections/export/selected
 *
 * @param {Array<number>} ids - 选中的检测记录 ID 列表
 */
export const exportSelectedDetections = (ids) =>
  http.downloadPost('/detections/export/selected', ids)

/**
 * 生成检测报告 (PDF)
 *
 * 对应后端接口：GET /api/v1/detections/{id}/report
 *
 * @param {string|number} id - 检测记录 ID
 */
export const generateDetectionReport = (id) => http.download(`/detections/${id}/report`)

/**
 * 更新检测记录信息
 *
 * 对应后端接口：PUT /api/v1/detections/{id}
 * 场景：AI 识别结果可能有误，允许人工修正破损等级或备注
 *
 * @param {string|number} id - 检测记录 ID
 * @param {Object} data - 更新的数据对象
 * @param {number} [data.damageLevel] - 修正后的破损等级
 * @param {string} [data.remark] - 备注信息
 */
export const updateDetection = (id, data) => http.put(`/detections/${id}`, data)

/**
 * 删除检测记录
 *
 * 对应后端接口：DELETE /api/v1/detections/{id}
 *
 * @param {string|number} id - 检测记录 ID
 */
export const deleteDetection = (id) => http.delete(`/detections/${id}`)

/**
 * 更新/修正单张图片的检测结果
 *
 * 对应后端接口：PUT /api/v1/detections/image
 * 场景：AI 识别结果有误，人工对单张子图进行修正
 *
 * @param {Object} data - 更新的数据对象
 * @param {number} data.id - 图片记录 ID (biz_detection_images 的 ID)
 * @param {number} data.damageLevel - 破损等级
 * @param {string} data.damageType - 破损类型
 * @param {string} data.damageDescription - 描述/备注
 * @param {string} [data.boundingBoxes] - 修正后的边框位置信息 (JSON 格式)
 */
export const updateDetectionImage = (data) => http.put('/detections/image', data)
