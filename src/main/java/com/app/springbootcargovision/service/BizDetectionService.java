package com.app.springbootcargovision.service;

import com.app.springbootcargovision.model.BizDetection;
import com.github.pagehelper.PageInfo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import com.app.springbootcargovision.model.BizDetectionImage;

/**
 * 货物检测业务服务接口 (BizDetectionService)
 * 
 * 作用：
 * 定义货物破损检测的核心业务契约。
 * 包括：检测记录的增删改查、批量图片上传与 AI 识别处理等。
 */
public interface BizDetectionService {

        /**
         * 更新/修正检测子图详情
         * 
         * @param image 包含更新信息的图片对象 (必须包含 id)
         */
        void updateDetectionImage(BizDetectionImage image);

        /**
         * 分页查询检测记录
         * 
         * @param page        当前页码 (1 开始)
         * @param size        每页显示数量
         * @param detectionNo 检测编号 (支持模糊查询)
         * @param transportNo 运输单号 (支持模糊查询)
         * @param goodsName   货物名称 (支持模糊查询)
         * @param damageLevel 破损等级 (精确匹配: 1-正常, 2-轻微, 3-严重)
         * @param sceneType   检测场景 (精确匹配)
         * @param nodeName    物流节点 (支持模糊查询)
         * @return 包含分页信息和数据列表的 PageInfo 对象
         */
        PageInfo<BizDetection> getDetectionList(Integer page, Integer size, String detectionNo, String transportNo,
                        String goodsName, Integer damageLevel, String sceneType, String nodeName);

        /**
         * 根据 ID 列表批量获取检测记录
         * 
         * @param ids 主键 ID 列表
         * @return 检测记录实体列表
         */
        List<BizDetection> getDetectionListByIds(List<Long> ids);

        /**
         * 获取单条检测记录详情
         * 
         * @param id 检测记录主键 ID
         * @return 检测记录实体 (应包含关联的子图片信息)
         */
        BizDetection getDetectionById(Long id);

        /**
         * 创建新的检测记录 (通常用于人工补录)
         * 
         * @param detection 检测记录实体对象
         */
        void createDetection(BizDetection detection);

        /**
         * 更新检测记录信息
         * 
         * @param detection 包含更新信息的实体对象 (必须包含 ID)
         */
        void updateDetection(BizDetection detection);

        /**
         * 删除检测记录及其关联的图片
         * 
         * @param id 检测记录主键 ID
         */
        void deleteDetection(Long id);

        /**
         * 核心业务：批量图片 AI 智能检测
         * 
         * 流程简述：
         * 1. 接收前端上传的多张图片。
         * 2. 保存图片到本地服务器。
         * 3. 调用 AI 引擎 (如百度 EasyDL) 进行破损识别。
         * 4. 解析 AI 返回结果，计算置信度和破损等级。
         * 5. 生成检测记录并入库。
         * 
         * @param files             上传的图片文件列表
         * @param transportId       关联的运输单 ID (必须)
         * @param detectionMode     检测模式 (1: 本地上传, 2: 摄像头拍摄)
         * @param detectionLocation 检测发生的地点
         *                          * @param responsibilitySubject 责任主体 (如: 承运商A)
         * @return 完整的检测结果记录 (包含聚合结果 and 每张图片的详情)
         */
        BizDetection batchDetect(List<MultipartFile> files, Long transportId, Integer detectionMode,
                        String detectionLocation, String responsibilitySubject) throws IOException;
}
