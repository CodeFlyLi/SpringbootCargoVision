import http from '@/utils/request'

export const uploadFile = (file) => http.upload('/common/upload', file)

// 获取运输状态字典
export const getTransportStatusDict = () => http.get('/dict/transport/status')

// 获取检测破损等级字典 (简单版)
export const getDamageLevelDict = () => http.get('/dict/detection/damage-level')

// 获取破损等级字典 (详细版，带编号说明)
export const getDamageLevelsDict = () => http.get('/dict/damage/levels')

// 获取检测状态字典
export const getDetectionStatusDict = () => http.get('/dict/detection/status')

// 获取用户状态字典
export const getUserStatusDict = () => http.get('/dict/user/status')
