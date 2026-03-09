package com.app.springbootcargovision.service;

import com.app.springbootcargovision.config.AiProperties;
import com.app.springbootcargovision.config.FileProperties;
import com.app.springbootcargovision.mapper.BizDetectionImageMapper;
import com.app.springbootcargovision.mapper.BizDetectionMapper;
import com.app.springbootcargovision.mapper.BizTransportMapper;
import com.app.springbootcargovision.model.BizDetection;
import com.app.springbootcargovision.service.impl.BizDetectionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BizDetectionServiceTest {

    @Mock
    private BizDetectionMapper bizDetectionMapper;
    @Mock
    private BizTransportMapper bizTransportMapper;
    @Mock
    private BizDetectionImageMapper bizDetectionImageMapper;
    @Mock
    private AiProperties aiProperties;
    @Mock
    private FileProperties fileProperties;
    @Mock
    private SysConfigService sysConfigService;
    @Mock
    private RestTemplate restTemplate;

    private BizDetectionServiceImpl bizDetectionService;

    @BeforeEach
    void setUp() {
        bizDetectionService = new BizDetectionServiceImpl(
                bizDetectionMapper,
                bizTransportMapper,
                bizDetectionImageMapper,
                aiProperties,
                fileProperties,
                sysConfigService,
                restTemplate);
    }

    @Test
    void batchDetect_shouldSetNewFields() throws IOException {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(2048L);
        when(file.getBytes()).thenReturn(new byte[] { 1, 2, 3 });

        List<MultipartFile> files = new ArrayList<>();
        files.add(file);

        Long transportId = 1L;
        Integer detectionMode = 2; // Camera
        String detectionLocation = "Warehouse A";
        String responsibilitySubject = "Carrier";

        // Mock File Config
        String tempDir = System.getProperty("java.io.tmpdir");
        when(fileProperties.getUploadDir()).thenReturn(tempDir);

        // Mock AI Config
        AiProperties.Model model = new AiProperties.Model();
        AiProperties.Cloud cloud = new AiProperties.Cloud();
        cloud.setApiKey("test-api-key");
        cloud.setSecretKey("test-secret-key");
        cloud.setEndpoint("http://test-endpoint");
        model.setCloud(cloud);
        when(aiProperties.getModel()).thenReturn(model);

        // Mock SysConfigService to return default values
        when(sysConfigService.getValue(anyString(), any())).thenAnswer(invocation -> invocation.getArgument(1));

        // Mock RestTemplate calls
        // 1. Token request
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenReturn(Map.of("access_token", "fake-token"));

        // 2. Detection request
        Map<String, Object> aiResult = Map.of(
                "result", List.of(Map.of("name", "broken", "score", 0.95)));
        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(aiResult, HttpStatus.OK));

        // Act
        bizDetectionService.batchDetect(files, transportId, detectionMode, detectionLocation, responsibilitySubject);

        // Assert
        ArgumentCaptor<BizDetection> captor = ArgumentCaptor.forClass(BizDetection.class);
        verify(bizDetectionMapper).insert(captor.capture());
        BizDetection savedDetection = captor.getValue();

        assertEquals(detectionMode, savedDetection.getDetectionMode());
        assertEquals(detectionLocation, savedDetection.getDetectionLocation());
        assertEquals(responsibilitySubject, savedDetection.getResponsibilitySubject());
        assertEquals(0, savedDetection.getIsVerified()); // Default value
    }

    @Test
    void deleteDetection_shouldDeleteFilesAndRecords() {
        // Arrange
        Long detectionId = 1L;
        com.app.springbootcargovision.model.BizDetectionImage img = new com.app.springbootcargovision.model.BizDetectionImage();
        img.setId(101L);
        img.setOriginalUrl("/uploads/test_orig.jpg");
        img.setProcessedUrl("/uploads/test_proc.jpg");
        
        List<com.app.springbootcargovision.model.BizDetectionImage> images = List.of(img);
        when(bizDetectionImageMapper.selectByDetectionId(detectionId)).thenReturn(images);
        
        String tempDir = System.getProperty("java.io.tmpdir");
        when(fileProperties.getUploadDir()).thenReturn(tempDir);
        
        // Create dummy files to be deleted
        try {
            new File(tempDir, "test_orig.jpg").createNewFile();
            new File(tempDir, "test_proc.jpg").createNewFile();
        } catch (IOException e) {}

        // Act
        bizDetectionService.deleteDetection(detectionId);

        // Assert
        verify(bizDetectionImageMapper).deleteByDetectionId(detectionId);
        verify(bizDetectionMapper).deleteById(detectionId);
    }
}
