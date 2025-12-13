package com.app.springbootcargovision.service.impl;

import com.app.springbootcargovision.config.AiProperties;
import com.app.springbootcargovision.config.FileProperties;
import com.app.springbootcargovision.mapper.BizDetectionMapper;
import com.app.springbootcargovision.mapper.BizTransportMapper;
import com.app.springbootcargovision.model.BizDetection;
import com.app.springbootcargovision.model.BizTransport;
import com.app.springbootcargovision.service.BizDetectionService;
import com.app.springbootcargovision.utils.SecurityUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import java.util.HashMap;
import java.util.Map;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.nio.file.Paths;

import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.Base64;
import java.util.Collections;

/**
 * 货物检测记录服务实现类
 * 实现检测记录管理及 AI 检测功能 (OpenCV/YOLO/Cloud)
 */
@Service
public class BizDetectionServiceImpl implements BizDetectionService {

    private static final Logger logger = LoggerFactory.getLogger(BizDetectionServiceImpl.class);
    private final RestTemplate restTemplate = new RestTemplate();

    // 货物检测记录映射器
    private final BizDetectionMapper bizDetectionMapper;
    // 货物运输记录映射器
    private final BizTransportMapper bizTransportMapper;
    // AI 配置属性
    private final AiProperties aiProperties;
    // 文件配置属性
    private final FileProperties fileProperties;

    public BizDetectionServiceImpl(BizDetectionMapper bizDetectionMapper, BizTransportMapper bizTransportMapper,
            AiProperties aiProperties, FileProperties fileProperties) {
        this.bizDetectionMapper = bizDetectionMapper;
        this.bizTransportMapper = bizTransportMapper;
        this.aiProperties = aiProperties;
        this.fileProperties = fileProperties;
    }

    @Override
    public PageInfo<BizDetection> getDetectionList(Integer page, Integer size, String detectionNo, String transportNo,
            String goodsName, Integer damageLevel) {
        PageHelper.startPage(page, size);
        BizDetection query = new BizDetection();
        query.setDetectionNo(detectionNo);
        query.setTransportNo(transportNo);
        query.setGoodsName(goodsName);
        query.setDamageLevel(damageLevel);

        List<BizDetection> list = bizDetectionMapper.selectList(query);
        return new PageInfo<>(list);
    }

    @Override
    public List<BizDetection> getDetectionListByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        return bizDetectionMapper.selectByIds(ids);
    }

    @Override
    public BizDetection getDetectionById(Long id) {
        return bizDetectionMapper.selectById(id);
    }

    @Override
    @Transactional
    public void createDetection(BizDetection detection) {
        if (detection.getDetectionNo() == null) {
            detection.setDetectionNo("D" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }

        // 设置操作员
        Long currentUserId = SecurityUtils.getUserId();
        if (currentUserId != null) {
            detection.setOperatorId(currentUserId);
        }

        bizDetectionMapper.insert(detection);
    }

    @Override
    @Transactional
    public void updateDetection(BizDetection detection) {

        bizDetectionMapper.update(detection);
    }

    /**
     * 货物 AI 检测实现
     * 仅支持云平台检测 ()
     */
    @Override
    public BizDetection detect(MultipartFile file, Long transportId) throws IOException {
        // 0. 检查是否启用了云平台检测
        if (aiProperties.getModel().getCloud() != null
                && aiProperties.getModel().getCloud().getProvider() != null
                && !aiProperties.getModel().getCloud().getProvider().isEmpty()) {
            return detectByCloud(file, transportId);
        } else {
            // 如果未配置云平台，则抛出异常或返回错误信息
            throw new IOException("云平台检测未配置，请在 application.yml 中配置 ai.model.cloud 相关参数");
        }
    }

    private BizDetection detectByCloud(MultipartFile file, Long transportId) throws IOException {
        BizDetection result = new BizDetection();

        // 1. 将 MultipartFile 转换为 Base64 字符串
        byte[] imageBytes = file.getBytes();
        String imageBase64 = Base64.getEncoder().encodeToString(imageBytes);

        try {
            // 2. 获取百度云平台的访问令牌
            String accessToken = getBaiduAccessToken(
                    aiProperties.getModel().getCloud().getApiKey(),
                    aiProperties.getModel().getCloud().getSecretKey());

            if (accessToken == null) {
                throw new RuntimeException("Failed to obtain Access Token");
            }

            // 3. 检查云平台的 API 端点
            String endpoint = aiProperties.getModel().getCloud().getEndpoint();
            if (endpoint == null || endpoint.isEmpty()) {
                endpoint = "https://aip.baidubce.com/rest/2.0/image-classify/v2/advanced_general";
            }

            String url = endpoint + "?access_token=" + accessToken;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            // org.springframework.util.MultiValueMap是一个键值对的集合，每个键可以对应多个值
            org.springframework.util.MultiValueMap<String, String> map = new org.springframework.util.LinkedMultiValueMap<>();
            map.add("image", imageBase64);
            map.add("image_type", "BASE64");

            HttpEntity<org.springframework.util.MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> respBody = response.getBody();
                if (respBody.containsKey("error_code")) {
                    result.setSuggestion("API Error: " + respBody.get("error_msg"));
                    result.setDamageLevel(0);
                    result.setDamageType("None");
                } else {
                    // 兼容处理：通用物体识别返回 'result'，EasyDL 定制训练返回 'results'
                    Object resultObj = null;
                    if (respBody.containsKey("results")) {
                        resultObj = respBody.get("results"); // EasyDL 格式
                    } else if (respBody.containsKey("result")) {
                        resultObj = respBody.get("result"); // 通用识别格式
                    }

                    if (resultObj instanceof List) {
                        List<Map<String, Object>> results = (List<Map<String, Object>>) resultObj;
                        if (!results.isEmpty()) {
                            Map<String, Object> top = results.get(0);

                            // 兼容处理：通用识别用 'keyword'，EasyDL 用 'name'
                            String keyword = null;
                            if (top.containsKey("name")) {
                                keyword = (String) top.get("name");
                            } else if (top.containsKey("keyword")) {
                                keyword = (String) top.get("keyword");
                            }

                            Double score = 0.0;
                            Object scoreObj = top.get("score");
                            if (scoreObj instanceof Number) {
                                score = ((Number) scoreObj).doubleValue();
                            }

                            // 直接使用 AI 返回的结果
                            result.setDamageType(keyword);
                            result.setConfidence(BigDecimal.valueOf(score));

                            // 智能判断逻辑
                            // 适配 4 级破损等级模型
                            // 标签建议命名: level_0 (无), level_1 (轻微), level_2 (中度), level_3 (严重)

                            int damageLevel = 0; // 默认无破损
                            String suggestion = "AI识别结果: " + keyword;

                            if (keyword != null) {
                                String k = keyword.toLowerCase();

                                // 1. 精确匹配训练标签 (推荐)
                                if (k.contains("level_3") || k.contains("严重") || k.contains("severe")) {
                                    damageLevel = 3;
                                    suggestion = "AI检测到严重破损，建议直接拒收";
                                } else if (k.contains("level_2") || k.contains("中度") || k.contains("moderate")) {
                                    damageLevel = 2;
                                    suggestion = "AI检测到中度破损，建议开箱验货";
                                } else if (k.contains("level_1") || k.contains("轻微") || k.contains("slight")) {
                                    damageLevel = 1;
                                    suggestion = "AI检测到轻微磨损，建议记录备案";
                                } else if (k.contains("level_0") || k.contains("无") || k.contains("intact")
                                        || k.contains("good")) {
                                    damageLevel = 0;
                                    suggestion = "AI检测货物完好";
                                }
                                // 2. 兜底逻辑：如果是通用模型返回的"破损"关键词，默认为中度
                                else if (k.contains("damaged") || k.contains("broken") || k.contains("破损")
                                        || k.contains("异常")) {
                                    damageLevel = 2;
                                    suggestion = "AI检测到异常 (" + keyword + ")，建议人工定级";
                                }
                            }

                            result.setDamageLevel(damageLevel);
                            result.setSuggestion(suggestion);
                            result.setDamageDescription(
                                    "AI识别标签: " + keyword + " (置信度: " + String.format("%.2f", score) + ")");
                        } else {
                            result.setDamageType("None");
                            result.setSuggestion("No object detected by Cloud AI");
                            result.setConfidence(BigDecimal.ZERO);
                        }
                    } else {
                        result.setDamageType("Unknown");
                        result.setSuggestion("Unknown response format");
                        result.setConfidence(BigDecimal.ZERO);
                    }
                }
            } else {
                result.setSuggestion("云端API返回错误状态码: " + response.getStatusCode());
            }

        } catch (Exception e) {
            logger.error("Cloud detection failed", e);
            result.setSuggestion("Cloud detection exception: " + e.getMessage());
            result.setDamageType("Error");
            result.setConfidence(BigDecimal.ZERO);
        }

        // Save original image locally
        try {
            String uploadDir = fileProperties.getUploadDir();
            File dir = new File(uploadDir);
            if (!dir.exists())
                dir.mkdirs();

            String fileName = "cloud_" + UUID.randomUUID() + ".jpg";
            File dest = new File(dir, fileName);
            java.nio.file.Files.write(dest.toPath(), imageBytes);

            String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/uploads/")
                    .path(fileName)
                    .toUriString();
            result.setImageUrl(fileUrl);
        } catch (Exception e) {
            logger.error("Failed to save cloud image", e);
        }

        // Fill other fields
        if (result.getDetectionNo() == null) {
            result.setDetectionNo("D" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        if (transportId != null) {
            result.setTransportId(transportId);
            BizTransport transport = bizTransportMapper.selectById(transportId);
            if (transport != null) {
                result.setTransportNo(transport.getTransportNo());
            }
        }
        result.setStatus(1); // Processed
        result.setDetectionTime(java.time.LocalDateTime.now());
        Long uid = SecurityUtils.getUserId();
        if (uid != null)
            result.setOperatorId(uid);

        bizDetectionMapper.insert(result);

        // 6. 联动更新运单备注 (如果检测出破损且关联了运单)
        if (transportId != null && result.getDamageLevel() != null && result.getDamageLevel() > 0) {
            try {
                BizTransport transport = bizTransportMapper.selectById(transportId);
                if (transport != null) {
                    String originalRemarks = transport.getRemarks();
                    if (originalRemarks == null) {
                        originalRemarks = "";
                    }
                    String appendMsg = String.format(" [AI检测警示 %s: 发现异常 %s (置信度 %.2f)]",
                            java.time.LocalDate.now(),
                            result.getDamageType(),
                            result.getConfidence());

                    // 避免备注过长
                    if (originalRemarks.length() + appendMsg.length() > 500) {
                        originalRemarks = originalRemarks.substring(0, 500 - appendMsg.length() - 3) + "...";
                    }

                    transport.setRemarks(originalRemarks + appendMsg);
                    bizTransportMapper.update(transport);
                }
            } catch (Exception e) {
                logger.error("Failed to update transport remarks based on detection", e);
            }
        }

        return result;
    }

    private String getBaiduAccessToken(String ak, String sk) {
        String url = "https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials&client_id=" + ak
                + "&client_secret=" + sk;
        try {
            Map resp = restTemplate.getForObject(url, Map.class);
            if (resp != null && resp.containsKey("access_token")) {
                return (String) resp.get("access_token");
            }
        } catch (Exception e) {
            logger.error("Failed to get Baidu Access Token", e);
        }
        return null;
    }
}
