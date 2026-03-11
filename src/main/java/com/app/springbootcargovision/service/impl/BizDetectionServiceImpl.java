package com.app.springbootcargovision.service.impl;

import com.app.springbootcargovision.config.AiProperties;
import com.app.springbootcargovision.config.FileProperties;
import com.app.springbootcargovision.mapper.BizDetectionImageMapper;
import com.app.springbootcargovision.mapper.BizDetectionMapper;
import com.app.springbootcargovision.mapper.BizTransportMapper;
import com.app.springbootcargovision.model.BizDetection;
import com.app.springbootcargovision.model.BizDetectionImage;
import com.app.springbootcargovision.model.BizTransport;
import com.app.springbootcargovision.service.BizDetectionService;
import com.app.springbootcargovision.service.SysConfigService;
import com.app.springbootcargovision.utils.SecurityUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.Base64;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 货物检测业务服务实现类 (Service Implementation)
 *
 * 【核心职责】
 * 本类实现了 BizDetectionService 接口，承载了系统的核心业务逻辑：
 * 1. 处理前端上传的货物图片（支持批量/单张）。
 * 2. 对接百度云 AI 接口（支持通用物体识别和 EasyDL 定制化破损检测模型）。
 * 3. 解析 AI 返回结果，将其映射为系统内部的破损等级 (0-完好, 1-轻微, 2-中度, 3-严重)。
 * 4. 维护数据库事务一致性，同时保存检测主记录 (BizDetection) 和子图片记录 (BizDetectionImage)。
 * 5. 业务联动：自动更新关联运输单的备注信息，实现物流全链路状态追踪。
 */
@Service
public class BizDetectionServiceImpl implements BizDetectionService {

    private static final Logger logger = LoggerFactory.getLogger(BizDetectionServiceImpl.class);

    /** HTTP 请求工具，用于调用外部 AI API */
    private final RestTemplate restTemplate;

    /** 检测主记录数据库操作接口 */
    private final BizDetectionMapper bizDetectionMapper;
    /** 运输单数据库操作接口 */
    private final BizTransportMapper bizTransportMapper;
    /** 检测图片明细数据库操作接口 */
    private final BizDetectionImageMapper bizDetectionImageMapper;
    /** AI 静态配置参数 (读取 application.yml) */
    private final AiProperties aiProperties;
    /** 文件存储配置参数 */
    private final FileProperties fileProperties;
    /** 系统动态配置服务 (读取数据库配置表) */
    private final SysConfigService sysConfigService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 构造器注入所有依赖
    public BizDetectionServiceImpl(BizDetectionMapper bizDetectionMapper, BizTransportMapper bizTransportMapper,
            BizDetectionImageMapper bizDetectionImageMapper, AiProperties aiProperties, FileProperties fileProperties,
            SysConfigService sysConfigService, RestTemplate restTemplate) {
        this.bizDetectionMapper = bizDetectionMapper;
        this.bizTransportMapper = bizTransportMapper;
        this.bizDetectionImageMapper = bizDetectionImageMapper;
        this.aiProperties = aiProperties;
        this.fileProperties = fileProperties;
        this.sysConfigService = sysConfigService;
        this.restTemplate = restTemplate;
    }

    /**
     * 更新/修正检测子图详情
     * 用于人工复核时修正 AI 的识别结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizDetectionImage updateDetectionImage(BizDetectionImage image) {
        if (image == null || image.getId() == null) {
            throw new IllegalArgumentException("图片 ID 不能为空");
        }

        // 处理手动标注的 Base64 图片
        if (image.getBase64Image() != null && !image.getBase64Image().isEmpty()) {
            try {
                String base64Data = image.getBase64Image();
                if (base64Data.contains(",")) {
                    base64Data = base64Data.split(",")[1];
                }
                byte[] decodedBytes = Base64.getDecoder().decode(base64Data);

                String uploadDir = fileProperties.getUploadDir();
                File dir = new File(uploadDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                String fileName = "manual_" + UUID.randomUUID() + ".jpg";
                File dest = new File(dir, fileName);
                Files.write(dest.toPath(), decodedBytes);

                image.setProcessedUrl("/uploads/" + fileName);
            } catch (Exception e) {
                logger.error("保存人工标注图片失败", e);
                // 不中断流程，仅记录日志
            }
        } else if (image.getBoundingBoxes() != null && !image.getBoundingBoxes().isEmpty()) {
            // 处理手动修正的坐标信息，重新生成标注图片
            try {
                BizDetectionImage originalImage = bizDetectionImageMapper.selectById(image.getId());
                if (originalImage != null && originalImage.getOriginalUrl() != null) {
                    String originalUrl = originalImage.getOriginalUrl();
                    String uploadDir = fileProperties.getUploadDir();
                    // 将 URL 转换为本地文件路径
                    String fileName = originalUrl.substring(originalUrl.lastIndexOf("/") + 1);
                    File originalFile = new File(uploadDir, fileName);

                    if (originalFile.exists()) {
                        // 修正：使用 TypeReference 或者明确类型转换
                        List<Map<String, Object>> results = objectMapper.readValue(image.getBoundingBoxes(),
                                List.class);
                        // 注意：这里 drawBoundingBoxes 方法需要在本类中存在，或者是一个工具方法
                        // 假设它是一个私有方法
                        BufferedImage bi = drawBoundingBoxes(originalFile, results);

                        String processedFileName = "manual_box_" + UUID.randomUUID() + ".jpg";
                        File processedFile = new File(uploadDir, processedFileName);
                        ImageIO.write(bi, "jpg", processedFile);

                        image.setProcessedUrl("/uploads/" + processedFileName);
                        // 同时更新破损等级和类型
                        updateImageSummaryFromResults(image, results);
                    }
                }
            } catch (Exception e) {
                logger.error("根据手动标注坐标重新生成图片失败", e);
            }
        }

        // 1. 更新图片子表
        bizDetectionImageMapper.update(image);

        // 2. 重新计算主记录的最高破损等级 (可选，保持主从数据一致性)
        // 获取该图片所属的检测主记录 ID
        BizDetectionImage originalImage = bizDetectionImageMapper.selectById(image.getId());
        if (originalImage != null && originalImage.getDetectionId() != null) {
            Long detectionId = originalImage.getDetectionId();

            // 查询该检测单下的所有图片
            BizDetectionImage query = new BizDetectionImage();
            query.setDetectionId(detectionId);
            List<BizDetectionImage> allImages = bizDetectionImageMapper.selectList(query);

            int maxLevel = 0;
            String mainType = "人工修正";

            for (BizDetectionImage img : allImages) {
                if (img.getDamageLevel() != null && img.getDamageLevel() > maxLevel) {
                    maxLevel = img.getDamageLevel();
                    mainType = img.getDamageType();
                }
            }

            // 更新主记录
            BizDetection detection = new BizDetection();
            detection.setId(detectionId);
            detection.setDamageLevel(maxLevel);
            // 只有当新的等级更高时才更新类型，或者保留原来的
            if (maxLevel > 0) {
                detection.setDamageType(mainType);
            }
            bizDetectionMapper.update(detection);
        }

        return image;
    }

    /**
     * 分页查询检测记录列表
     * 支持多种条件组合筛选：检测单号、运输单号、货物名称、破损等级等。
     */
    @Override
    public PageInfo<BizDetection> getDetectionList(Integer page, Integer size, String detectionNo, String transportNo,
            String goodsName, Integer damageLevel, String sceneType, String nodeName) {
        // 开启 PageHelper 分页插件
        PageHelper.startPage(page, size);

        // 构建查询条件对象
        BizDetection query = new BizDetection();
        query.setDetectionNo(detectionNo);
        query.setTransportNo(transportNo);
        query.setGoodsName(goodsName);
        query.setDamageLevel(damageLevel);
        query.setSceneType(sceneType);
        query.setNodeName(nodeName);

        // 执行查询
        List<BizDetection> list = bizDetectionMapper.selectList(query);
        if (list != null && !list.isEmpty()) {
            // 填充每个检测记录关联的图片列表，以便前端展示缩略图
            for (BizDetection d : list) {
                d.setImageList(bizDetectionImageMapper.selectByDetectionId(d.getId()));
            }
        }
        return new PageInfo<>(list);
    }

    /**
     * 根据 ID 列表批量获取检测记录
     * 通常用于导出或批量操作场景
     */
    @Override
    public List<BizDetection> getDetectionListByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        List<BizDetection> list = bizDetectionMapper.selectByIds(ids);
        if (list != null && !list.isEmpty()) {
            for (BizDetection d : list) {
                d.setImageList(bizDetectionImageMapper.selectByDetectionId(d.getId()));
            }
        }
        return list;
    }

    /**
     * 根据 ID 获取单条检测详情
     * 包含主记录信息和所有关联的图片识别结果
     */
    @Override
    public BizDetection getDetectionById(Long id) {
        BizDetection detection = bizDetectionMapper.selectById(id);
        if (detection != null) {
            detection.setImageList(bizDetectionImageMapper.selectByDetectionId(id));
        }
        return detection;
    }

    /**
     * 创建新的检测记录 (手动创建模式)
     * 自动生成检测单号，并记录操作人
     */
    @Override
    @Transactional
    public void createDetection(BizDetection detection) {
        if (detection.getDetectionNo() == null) {
            // 生成唯一检测单号，格式：D + 8位随机字符
            detection.setDetectionNo("D" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }

        // 获取当前登录用户 ID
        Long currentUserId = SecurityUtils.getUserId();
        if (currentUserId != null) {
            detection.setOperatorId(currentUserId);
        }

        bizDetectionMapper.insert(detection);
    }

    /**
     * 更新检测记录
     * 通常用于人工审核后修正检测结果或处理建议
     */
    @Override
    @Transactional
    public void updateDetection(BizDetection detection) {
        // 同步 V2 字段到 V1 字段，确保列表显示一致
        if (detection.getDetectionLocation() != null) {
            detection.setSceneType(detection.getDetectionLocation());
        }
        if (detection.getResponsibilitySubject() != null) {
            detection.setNodeName(detection.getResponsibilitySubject());
        }
        bizDetectionMapper.update(detection);
    }

    /**
     * 删除检测记录及其关联的图片记录和物理文件
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDetection(Long id) {
        // 1. 查询所有关联图片，准备删除物理文件
        List<BizDetectionImage> images = bizDetectionImageMapper.selectByDetectionId(id);
        String uploadDir = fileProperties.getUploadDir();

        for (BizDetectionImage img : images) {
            try {
                // 删除原始图片
                if (img.getOriginalUrl() != null) {
                    String fileName = img.getOriginalUrl().substring(img.getOriginalUrl().lastIndexOf("/") + 1);
                    File file = new File(uploadDir, fileName);
                    if (file.exists()) {
                        file.delete();
                    }
                }
                // 删除处理后的图片
                if (img.getProcessedUrl() != null) {
                    String fileName = img.getProcessedUrl().substring(img.getProcessedUrl().lastIndexOf("/") + 1);
                    File file = new File(uploadDir, fileName);
                    if (file.exists()) {
                        file.delete();
                    }
                }
            } catch (Exception e) {
                logger.error("删除检测记录关联文件失败, ID: " + img.getId(), e);
            }
        }

        // 2. 删除数据库子表记录 (图片)
        bizDetectionImageMapper.deleteByDetectionId(id);

        // 3. 删除数据库主表记录 (检测记录)
        bizDetectionMapper.deleteById(id);

        logger.info("已成功删除检测记录 ID: {}", id);
    }

    /**
     * 核心业务方法：批量图片检测
     *
     * 【处理流程】
     * 1. 校验输入文件和 AI 配置。
     * 2. 初始化检测主记录（关联运输单、操作人等）。
     * 3. 遍历图片列表，逐张处理：
     * - 图片质量检查
     * - 保存本地文件
     * - 调用百度云 API 进行推理
     * - 解析结果并按置信度过滤
     * 4. 汇总所有图片的检测结果，计算最高破损等级。
     * 5. 保存主记录和子记录到数据库。
     * 6. 自动更新关联运输单的备注信息。
     *
     * @param files                 图片文件列表
     * @param transportId           关联运输单ID
     * @param detectionMode         检测模式
     * @param detectionLocation     检测地点
     * @param responsibilitySubject 责任主体
     */
    @Override
    @Transactional
    public BizDetection batchDetect(List<MultipartFile> files, Long transportId, Integer detectionMode,
            String detectionLocation, String responsibilitySubject) throws IOException {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        // 步骤 0: 获取 AI 配置参数
        // 逻辑：优先读取数据库动态配置，未找到或为空则降级使用 application.yml 中的静态配置
        String ak = sysConfigService.getValue("ai.baidu.api_key", null);
        if (ak == null || ak.isEmpty()) {
            ak = aiProperties.getModel().getCloud() != null ? aiProperties.getModel().getCloud().getApiKey() : null;
        }

        String sk = sysConfigService.getValue("ai.baidu.secret_key", null);
        if (sk == null || sk.isEmpty()) {
            sk = aiProperties.getModel().getCloud() != null ? aiProperties.getModel().getCloud().getSecretKey() : null;
        }

        // 默认使用通用物体识别接口
        String defaultEndpoint = aiProperties.getModel().getCloud() != null
                ? aiProperties.getModel().getCloud().getEndpoint()
                : "https://aip.baidubce.com/rest/2.0/image-classify/v2/advanced_general";

        String endpoint = sysConfigService.getValue("ai.baidu.endpoint", null);
        if (endpoint == null || endpoint.isEmpty()) {
            endpoint = defaultEndpoint;
        }

        // 如果配置了 EasyDL 定制化模型接口，则优先使用定制接口
        String customEndpoint = sysConfigService.getValue("ai.baidu.custom_endpoint", null);
        if (customEndpoint == null || customEndpoint.isEmpty()) {
            customEndpoint = aiProperties.getModel().getCloud() != null
                    ? aiProperties.getModel().getCloud().getCustomEndpoint()
                    : null;
        }

        if (customEndpoint != null && !customEndpoint.isEmpty()) {
            endpoint = customEndpoint;
        }

        if (ak == null || ak.isEmpty() || sk == null || sk.isEmpty()) {
            throw new IOException(
                    "云平台检测未配置，请在系统配置或 application.yml 中设置");
        }

        // 步骤 1: 准备检测主记录对象
        BizDetection detection = new BizDetection();
        if (detection.getDetectionNo() == null) {
            detection.setDetectionNo("D" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        if (transportId != null) {
            detection.setTransportId(transportId);
            BizTransport transport = bizTransportMapper.selectById(transportId);
            if (transport != null) {
                detection.setTransportNo(transport.getTransportNo());
                detection.setGoodsName(transport.getGoodsName());
            }
        }
        detection.setDetectionTime(LocalDateTime.now());

        // 填充 V2 版本业务字段
        if (detectionLocation != null) {
            detection.setDetectionLocation(detectionLocation);
            detection.setSceneType(detectionLocation); // 同步填充到场景字段，确保列表显示
        }
        if (responsibilitySubject != null) {
            detection.setResponsibilitySubject(responsibilitySubject);
            detection.setNodeName(responsibilitySubject); // 同步填充到物流节点字段，确保列表显示
        }
        if (detectionMode != null) {
            detection.setDetectionMode(detectionMode);
        }
        detection.setIsVerified(0); // 默认为未核实状态

        Long uid = SecurityUtils.getUserId();
        if (uid != null) {
            detection.setOperatorId(uid);
        }

        // 步骤 2: 循环处理每一张图片
        List<BizDetectionImage> imageList = new ArrayList<>();
        int maxDamageLevel = 0;
        BigDecimal maxConfidence = BigDecimal.ZERO;
        String mainDamageType = "无";
        StringBuilder suggestionBuilder = new StringBuilder();

        // 获取置信度阈值配置 (默认 0.5)
        String thresholdStr = sysConfigService.getValue("ai.confidence.threshold", "0.5");
        BigDecimal confidenceThreshold = new BigDecimal(thresholdStr);

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);

            // 前置检查：图片质量校验
            if (!validateImageQuality(file)) {
                logger.warn("图片质量校验失败: {}", file.getOriginalFilename());
                continue;
            }

            try {
                // 调用核心处理方法：保存文件 + AI 推理
                BizDetectionImage image = processImage(file, ak, sk, endpoint);
                image.setSortOrder(i);

                // 后置处理：置信度过滤
                // 修改：将硬编码的阈值过滤逻辑改为仅记录日志，不修改结果，交由前端展示或人工判断
                // 原逻辑会将低置信度结果直接置为 0，导致用户认为 AI 无效
                if (image.getConfidence().compareTo(confidenceThreshold) < 0) {
                    logger.info("图片 {} 置信度 {} 低于系统配置阈值 {}，但保留原始结果供人工复核",
                            file.getOriginalFilename(), image.getConfidence(), confidenceThreshold);
                    // 可以在 damageType 中增加标记，但不要覆盖原始识别结果
                    // image.setDamageType(image.getDamageType() + " (低置信度)");
                }

                imageList.add(image);

                // 统计逻辑：记录最高破损等级和最高置信度
                if (image.getDamageLevel() > maxDamageLevel) {
                    maxDamageLevel = image.getDamageLevel();
                    mainDamageType = image.getDamageType();
                }
                if (image.getConfidence().compareTo(maxConfidence) > 0) {
                    maxConfidence = image.getConfidence();
                }
                // 收集破损描述用于生成建议
                if (image.getDamageLevel() > 0 && suggestionBuilder.length() < 200) {
                    suggestionBuilder.append(String.format("图%d:%s; ", i + 1, image.getDamageType()));
                }

            } catch (Exception e) {
                logger.error("处理文件失败: " + file.getOriginalFilename(), e);
            }
        }

        if (imageList.isEmpty()) {
            throw new IOException("没有有效的图片被成功处理");
        }

        // 步骤 3: 完善主记录汇总信息
        detection.setDamageLevel(maxDamageLevel);
        detection.setDamageType(mainDamageType);
        detection.setConfidence(maxConfidence);

        // 自动生成处置建议
        String suggestionPrefix = "";
        if (maxConfidence.compareTo(confidenceThreshold) < 0) {
            suggestionPrefix = "【置信度低】";
        }

        if (maxDamageLevel == 0) {
            detection.setSuggestion(suggestionPrefix + "AI 检测结果：货物完好");
        } else {
            detection.setSuggestion(
                    suggestionPrefix + "AI 检测发现异常：" + suggestionBuilder.toString() + "建议人工复核");
        }

        detection.setDamageDescription(
                String.format("共检测 %d 张图片，最高破损等级: %d", imageList.size(), maxDamageLevel));
        detection.setStatus(1); // 状态标记为已处理

        // 步骤 4: 保存主记录
        bizDetectionMapper.insert(detection);

        // 步骤 5: 批量保存子图片记录
        for (BizDetectionImage img : imageList) {
            img.setDetectionId(detection.getId());
            img.setCreatedAt(LocalDateTime.now());
        }
        bizDetectionImageMapper.insertBatch(imageList);
        detection.setImageList(imageList);

        // 步骤 6: 业务联动，更新运输单备注
        updateTransportRemarks(transportId, detection);

        return detection;
    }

    /**
     * 图片质量校验
     * 简单的文件存在性和大小检查，可扩展分辨率检查等
     */
    private boolean validateImageQuality(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        if (file.getSize() < 1024) { // 忽略小于 1KB 的文件
            return false;
        }
        return true;
    }

    /**
     * 单张图片处理流程
     * 1. 保存文件到本地磁盘
     * 2. 对文件内容进行 Base64 编码
     * 3. 发送 HTTP 请求调用百度云 API
     * 4. 解析 API 响应结果
     */
    private BizDetectionImage processImage(MultipartFile file, String ak, String sk, String endpoint)
            throws IOException {
        BizDetectionImage image = new BizDetectionImage();
        // 初始化默认值，防止后续 NPE
        image.setDamageLevel(0);
        image.setConfidence(BigDecimal.ZERO);
        image.setDamageType("未检测到异常");

        // 1. 本地持久化保存
        byte[] imageBytes = file.getBytes();
        String uploadDir = fileProperties.getUploadDir();
        File dir = new File(uploadDir);
        if (!dir.exists())
            dir.mkdirs();

        String fileName = "cloud_" + UUID.randomUUID() + ".jpg";
        File dest = new File(dir, fileName);
        Files.write(dest.toPath(), imageBytes);

        String fileUrl = "/uploads/" + fileName;
        image.setOriginalUrl(fileUrl);
        // 如果需要 OpenCV 预处理（如画框），在此处更新 processedUrl
        image.setProcessedUrl(fileUrl);

        // 2. 调用云端 API
        String imageBase64 = Base64.getEncoder().encodeToString(imageBytes);

        try {
            String accessToken = getBaiduAccessToken(ak, sk);

            if (accessToken == null) {
                throw new RuntimeException("无法获取 Access Token");
            }

            String url = endpoint + "?access_token=" + accessToken;
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<?> request;

            // 智能判断接口类型：EasyDL (ai_custom) 通常要求 JSON 格式
            if (endpoint.contains("ai_custom")) {
                headers.setContentType(MediaType.APPLICATION_JSON);
                Map<String, String> jsonBody = new HashMap<>();
                jsonBody.put("image", imageBase64);
                request = new HttpEntity<>(jsonBody, headers);
            } else {
                // 通用接口默认使用 Form 表单
                headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
                map.add("image", imageBase64);
                map.add("image_type", "BASE64");
                request = new HttpEntity<>(map, headers);
            }

            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> respBody = response.getBody();
                // 传入原始文件对象，以便在需要时（如物体检测）进行画框处理
                parseCloudResponse(respBody, image, dest);
            } else {
                image.setDamageType("API 错误");
                image.setDamageLevel(0);
                image.setConfidence(BigDecimal.ZERO);
            }
        } catch (Exception e) {
            logger.error("云端 API 调用失败", e);
            image.setDamageType("错误");
            image.setDamageLevel(0);
            image.setConfidence(BigDecimal.ZERO);
        }

        return image;
    }

    /**
     * 解析云端 API 响应
     * 兼容 通用物体识别接口、EasyDL 定制化分类接口
     * 目前主要使用分类模型，不涉及 AI 自动画框（如需画框请使用手动修正功能）
     */
    private void parseCloudResponse(Map<String, Object> respBody, BizDetectionImage image, File originalFile) {
        if (respBody.containsKey("error_code")) {
            image.setDamageType("错误: " + respBody.get("error_msg"));
            image.setDamageLevel(0);
            image.setConfidence(BigDecimal.ZERO);
            return;
        }

        Object resultObj = null;
        if (respBody.containsKey("results")) {
            resultObj = respBody.get("results");
        } else if (respBody.containsKey("result")) {
            resultObj = respBody.get("result");
        }

        if (resultObj instanceof List) {
            List<Map<String, Object>> results = (List<Map<String, Object>>) resultObj;
            if (!results.isEmpty()) {
                // 目前统一按分类结果处理，取置信度最高的一项
                processClassificationResults(results, image);
            }
        }
    }

    /**
     * 处理分类结果 (原有逻辑)
     */
    private void processClassificationResults(List<Map<String, Object>> results, BizDetectionImage image) {
        // 获取置信度最高的第一个结果
        Map<String, Object> top = results.get(0);
        String keyword = null;
        if (top.containsKey("name"))
            keyword = (String) top.get("name");
        else if (top.containsKey("keyword"))
            keyword = (String) top.get("keyword");

        Double score = 0.0;
        Object scoreObj = top.get("score");
        if (scoreObj instanceof Number)
            score = ((Number) scoreObj).doubleValue();

        image.setDamageType(keyword);
        image.setConfidence(BigDecimal.valueOf(score));

        // 映射破损等级
        image.setDamageLevel(mapDamageLevel(keyword, score));
    }

    /**
     * 根据检测结果列表更新图片对象的汇总信息 (最高等级、类型、置信度)
     */
    private void updateImageSummaryFromResults(BizDetectionImage image, List<Map<String, Object>> results) {
        int maxLevel = 0;
        double maxScore = 0.0;
        String mainType = "无";

        for (Map<String, Object> item : results) {
            String name = (String) item.get("name");
            Double score = 0.0;
            Object scoreObj = item.get("score");
            if (scoreObj instanceof Number) {
                score = ((Number) scoreObj).doubleValue();
            }

            int level = mapDamageLevel(name, score);

            if (level > maxLevel) {
                maxLevel = level;
                mainType = name;
                maxScore = score;
            } else if (level == maxLevel && score > maxScore) {
                mainType = name;
                maxScore = score;
            }
        }

        image.setDamageLevel(maxLevel);
        image.setDamageType(mainType);
        image.setConfidence(BigDecimal.valueOf(maxScore));
    }

    /**
     * 在图片上绘制标注框并返回 BufferedImage 对象
     */
    private BufferedImage drawBoundingBoxes(File imageFile, List<Map<String, Object>> results) throws IOException {
        BufferedImage bi = ImageIO.read(imageFile);
        Graphics2D g2d = bi.createGraphics();
        g2d.setStroke(new BasicStroke(3));
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));

        for (Map<String, Object> item : results) {
            String name = (String) item.get("name");
            Double score = ((Number) item.get("score")).doubleValue();

            if (score < 0.01)
                continue;

            Map<String, Object> loc = (Map<String, Object>) item.get("location");
            int left = ((Number) loc.get("left")).intValue();
            int top = ((Number) loc.get("top")).intValue();
            int width = ((Number) loc.get("width")).intValue();
            int height = ((Number) loc.get("height")).intValue();

            int level = mapDamageLevel(name, score);

            if (level == 0)
                g2d.setColor(Color.GREEN);
            else if (level == 1)
                g2d.setColor(Color.YELLOW);
            else if (level == 2)
                g2d.setColor(Color.ORANGE);
            else
                g2d.setColor(Color.RED);

            g2d.drawRect(left, top, width, height);

            String label = name + " " + String.format("%.2f", score);
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(label);
            int textHeight = fm.getHeight();

            g2d.setColor(new Color(0, 0, 0, 128));
            g2d.fillRect(left, top - textHeight - 5, textWidth + 10, textHeight + 5);

            g2d.setColor(Color.WHITE);
            g2d.drawString(label, left + 5, top - 5);
        }
        g2d.dispose();
        return bi;
    }

    /**
     * 辅助方法：根据标签映射破损等级
     */
    private int mapDamageLevel(String keyword, Double score) {
        int level = 0; // 默认为完好
        if (keyword != null) {
            String k = keyword.toLowerCase();
            logger.info("AI 返回标签: {}, 置信度: {}", keyword, score);

            // 1. 明确的破损关键字匹配
            if (k.contains("严重") || k.contains("severe") || k.contains("level_3")) {
                level = 3;
            } else if (k.contains("中度") || k.contains("moderate") || k.contains("level_2") || k.contains("凹陷")
                    || k.contains("变形") || k.contains("破洞")) {
                level = 2;
            } else if (k.contains("一般") || k.contains("轻微") || k.contains("slight") || k.contains("level_1")
                    || k.contains("划痕") || k.contains("磨损") || k.contains("scratch")) {
                level = 1;
            }
            // 2. 明确的完好关键字匹配
            else if (k.contains("完好") || k.contains("正常") || k.contains("intact") || k.contains("normal")
                    || k.contains("ok")) {
                level = 0;
            }
            // 3. 兜底逻辑：未知标签默认判定为轻微破损
            else {
                logger.warn("遇到未知标签 '{}'，默认判定为轻微破损", keyword);
                level = 1;
            }
        }
        return level;
    }

    /**
     * 获取百度 Access Token
     * 如果缓存中有则直接使用，否则请求 API 获取并刷新缓存
     */
    private String getBaiduAccessToken(String ak, String sk) {
        // 实际项目中应使用 Redis 缓存 Token，此处简化为直接调用
        // 百度官方 Token 有效期为 30 天
        String url = "https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials&client_id=" + ak
                + "&client_secret=" + sk;
        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.containsKey("access_token")) {
                return (String) response.get("access_token");
            }
        } catch (Exception e) {
            logger.error("获取 AccessToken 失败", e);
        }
        return null;
    }

    /**
     * 更新运输单备注
     * 当检测到破损时，自动在运输单备注中添加记录，方便调度人员查看
     */
    private void updateTransportRemarks(Long transportId, BizDetection detection) {
        if (transportId == null)
            return;
        try {
            BizTransport transport = bizTransportMapper.selectById(transportId);
            if (transport != null) {
                String oldRemark = transport.getRemarks();
                if (oldRemark == null)
                    oldRemark = "";
                String append = String.format(" [检测单%s: 等级%d]", detection.getDetectionNo(),
                        detection.getDamageLevel());

                // 防止备注字段过长
                if (oldRemark.length() + append.length() < 500) {
                    transport.setRemarks(oldRemark + append);
                    bizTransportMapper.update(transport);
                }
            }
        } catch (Exception e) {
            logger.error("更新运输单备注失败 (非关键错误)", e);
        }
    }
}
