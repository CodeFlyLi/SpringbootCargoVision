package com.app.springbootcargovision.service;

import com.app.springbootcargovision.model.BizDetection;
import com.github.pagehelper.PageInfo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 货物检测记录服务接口
 * 处理货物检测、记录管理及文件上传识别业务
 */
public interface BizDetectionService {

    /**
     * 分页查询检测记录列表
     * 
     * @param page        页码
     * @param size        每页数量
     * @param detectionNo 检测编号（可选，模糊查询）
     * @param transportNo 运输单号（可选，模糊查询）
     * @param goodsName   货物名称（可选，模糊查询）
     * @param damageLevel 破损等级（可选，精确匹配）
     * @return 包含检测记录列表的分页信息对象
     */
    PageInfo<BizDetection> getDetectionList(Integer page, Integer size, String detectionNo, String transportNo,
            String goodsName, Integer damageLevel);

    /**
     * 根据ID列表获取检测记录列表
     * 
     * @param ids 检测记录ID列表
     * @return 检测记录列表
     */
    java.util.List<BizDetection> getDetectionListByIds(java.util.List<Long> ids);

    /**
     * 根据ID获取检测记录详情
     * 
     * @param id 检测记录ID
     * @return 检测记录实体对象
     */
    BizDetection getDetectionById(Long id);

    /**
     * 创建新检测记录
     * 
     * @param detection 检测记录实体对象
     */
    void createDetection(BizDetection detection);

    /**
     * 更新检测记录
     * 
     * @param detection 检测记录实体对象
     */
    void updateDetection(BizDetection detection);

    /**
     * 处理图片上传并进行货物检测
     * 上传图片文件，调用检测算法（模拟或实际），返回检测结果
     * 
     * @param file 上传的图片文件
     * @param transportId 关联的运输单ID (可选)
     * @return 包含检测结果的检测记录实体（未持久化）
     * @throws IOException 文件读取或处理异常
     */
    BizDetection detect(MultipartFile file, Long transportId) throws IOException;
}
