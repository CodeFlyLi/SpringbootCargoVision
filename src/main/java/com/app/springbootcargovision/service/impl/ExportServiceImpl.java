package com.app.springbootcargovision.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.app.springbootcargovision.model.BizDetection;
import com.app.springbootcargovision.service.BizDetectionService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class ExportServiceImpl {

    private final BizDetectionService bizDetectionService;

    public ExportServiceImpl(BizDetectionService bizDetectionService) {
        this.bizDetectionService = bizDetectionService;
    }

    /**
     * 导出检测记录到 Excel
     */
    public void exportDetectionToExcel(HttpServletResponse response, String detectionNo, String transportNo,
            String goodsName, Integer damageLevel) throws IOException {
        // 1. 获取数据 (不分页，获取全部)
        List<BizDetection> list = bizDetectionService
                .getDetectionList(1, Integer.MAX_VALUE, detectionNo, transportNo, goodsName, damageLevel).getList();

        // 2. 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("检测记录报表", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        // 3. 写入 Excel
        EasyExcel.write(response.getOutputStream(), DetectionExportData.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .sheet("检测记录")
                .doWrite(() -> convertToExportData(list));
    }

    /**
     * 导出选中的检测记录到 Excel
     */
    public void exportSelectedDetectionToExcel(HttpServletResponse response, List<Long> ids) throws IOException {
        // 1. 获取选中数据
        List<BizDetection> list = bizDetectionService.getDetectionListByIds(ids);

        // 2. 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("检测记录报表(选中)", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        // 3. 写入 Excel
        EasyExcel.write(response.getOutputStream(), DetectionExportData.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .sheet("检测记录")
                .doWrite(() -> convertToExportData(list));
    }

    /**
     * 生成单个检测记录的 PDF 报告
     */
    public void generateDetectionReportPdf(HttpServletResponse response, Long id)
            throws IOException, DocumentException {
        // 1. 获取数据
        BizDetection detection = bizDetectionService.getDetectionById(id);
        if (detection == null) {
            throw new RuntimeException("检测记录不存在");
        }

        // 2. 设置响应头
        response.setContentType("application/pdf");
        String fileName = URLEncoder.encode("检测报告_" + detection.getDetectionNo(), "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".pdf");

        // 3. 创建 PDF 文档
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        // 4. 添加内容
        // 字体设置 (支持中文)
        BaseFont bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
        Font titleFont = new Font(bfChinese, 18, Font.BOLD);
        Font normalFont = new Font(bfChinese, 12, Font.NORMAL);
        Font labelFont = new Font(bfChinese, 12, Font.BOLD);

        // 标题
        Paragraph title = new Paragraph("货物破损检测报告", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // 基本信息表格
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(10);

        addTableCell(table, "检测编号:", labelFont);
        addTableCell(table, detection.getDetectionNo(), normalFont);

        addTableCell(table, "运输单号:", labelFont);
        addTableCell(table, detection.getTransportNo(), normalFont);

        addTableCell(table, "货物名称:", labelFont);
        addTableCell(table, detection.getGoodsName(), normalFont);

        addTableCell(table, "操作员:", labelFont);
        addTableCell(table, detection.getOperatorName(), normalFont);

        addTableCell(table, "检测时间:", labelFont);
        addTableCell(table,
                detection.getDetectionTime() != null
                        ? detection.getDetectionTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        : "",
                normalFont);

        document.add(table);

        // 检测结果
        Paragraph resultTitle = new Paragraph("检测结果", new Font(bfChinese, 14, Font.BOLD));
        resultTitle.setSpacingBefore(10);
        document.add(resultTitle);

        PdfPTable resultTable = new PdfPTable(2);
        resultTable.setWidthPercentage(100);
        resultTable.setSpacingBefore(10);

        addTableCell(resultTable, "破损等级:", labelFont);
        addTableCell(resultTable, getDamageLevelText(detection.getDamageLevel()), normalFont);

        addTableCell(resultTable, "置信度:", labelFont);
        addTableCell(resultTable, detection.getConfidence() != null ? detection.getConfidence().toString() : "",
                normalFont);

        addTableCell(resultTable, "破损类型:", labelFont);
        addTableCell(resultTable, detection.getDamageType(), normalFont);

        addTableCell(resultTable, "破损面积:", labelFont);
        addTableCell(resultTable, detection.getDamageArea() != null ? detection.getDamageArea() + " cm²" : "",
                normalFont);

        addTableCell(resultTable, "破损描述:", labelFont);
        addTableCell(resultTable, detection.getDamageDescription(), normalFont);

        addTableCell(resultTable, "处理建议:", labelFont);
        addTableCell(resultTable, detection.getSuggestion(), normalFont);

        document.add(resultTable);

        // 图片展示 (如果有)
        if (detection.getImageUrl() != null && !detection.getImageUrl().isEmpty()) {
            try {
                Paragraph p = new Paragraph("原始图片", labelFont);
                p.setAlignment(Element.ALIGN_CENTER);
                p.setSpacingBefore(10);
                document.add(p);

                Image img = Image.getInstance(detection.getImageUrl());
                img.scaleToFit(500, 300);
                img.setAlignment(Element.ALIGN_CENTER);
                img.setSpacingBefore(5);
                document.add(img);
            } catch (Exception e) {
                document.add(new Paragraph("原始图片加载失败: " + e.getMessage(), normalFont));
            }
        }

        if (detection.getProcessedImageUrl() != null && !detection.getProcessedImageUrl().isEmpty()) {
            try {
                Paragraph p = new Paragraph("处理后图片", labelFont);
                p.setAlignment(Element.ALIGN_CENTER);
                p.setSpacingBefore(10);
                document.add(p);

                Image img = Image.getInstance(detection.getProcessedImageUrl());
                img.scaleToFit(500, 300);
                img.setAlignment(Element.ALIGN_CENTER);
                img.setSpacingBefore(5);
                document.add(img);
            } catch (Exception e) {
                document.add(new Paragraph("处理后图片加载失败: " + e.getMessage(), normalFont));
            }
        }

        document.close();
    }

    private void addTableCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setPadding(5);
        table.addCell(cell);
    }

    private String getDamageLevelText(Integer level) {
        if (level == null)
            return "未知";
        switch (level) {
            case 0:
                return "无破损";
            case 1:
                return "轻微";
            case 2:
                return "中度";
            case 3:
                return "严重";
            default:
                return "未知";
        }
    }

    private List<DetectionExportData> convertToExportData(List<BizDetection> list) {
        return list.stream().map(item -> {
            DetectionExportData data = new DetectionExportData();
            data.setDetectionNo(item.getDetectionNo());
            data.setTransportNo(item.getTransportNo());
            data.setGoodsName(item.getGoodsName());
            data.setOperatorName(item.getOperatorName());
            data.setDamageLevel(getDamageLevelText(item.getDamageLevel()));
            data.setDamageType(item.getDamageType());
            data.setDamageArea(item.getDamageArea());
            data.setDamageDescription(item.getDamageDescription());
            data.setConfidence(item.getConfidence());
            data.setDetectionTime(item.getDetectionTime());
            data.setSuggestion(item.getSuggestion());
            data.setImageUrl(item.getImageUrl());
            data.setProcessedImageUrl(item.getProcessedImageUrl());
            return data;
        }).toList();
    }

    // 内部类用于 Excel 导出数据模型
    @com.alibaba.excel.annotation.ExcelIgnoreUnannotated
    public static class DetectionExportData {
        @com.alibaba.excel.annotation.ExcelProperty("检测编号")
        private String detectionNo;

        @com.alibaba.excel.annotation.ExcelProperty("运输单号")
        private String transportNo;

        @com.alibaba.excel.annotation.ExcelProperty("货物名称")
        private String goodsName;

        @com.alibaba.excel.annotation.ExcelProperty("操作员")
        private String operatorName;

        @com.alibaba.excel.annotation.ExcelProperty("破损等级")
        private String damageLevel;

        @com.alibaba.excel.annotation.ExcelProperty("破损类型")
        private String damageType;

        @com.alibaba.excel.annotation.ExcelProperty("破损面积(cm²)")
        private java.math.BigDecimal damageArea;

        @com.alibaba.excel.annotation.ExcelProperty("破损描述")
        private String damageDescription;

        @com.alibaba.excel.annotation.ExcelProperty("置信度")
        private java.math.BigDecimal confidence;

        @com.alibaba.excel.annotation.ExcelProperty("检测时间")
        private java.time.LocalDateTime detectionTime;

        @com.alibaba.excel.annotation.ExcelProperty("处理建议")
        private String suggestion;

        @com.alibaba.excel.annotation.ExcelProperty("原始图片URL")
        private String imageUrl;

        @com.alibaba.excel.annotation.ExcelProperty("处理图片URL")
        private String processedImageUrl;

        // Getters and Setters
        public String getDetectionNo() {
            return detectionNo;
        }

        public void setDetectionNo(String detectionNo) {
            this.detectionNo = detectionNo;
        }

        public String getTransportNo() {
            return transportNo;
        }

        public void setTransportNo(String transportNo) {
            this.transportNo = transportNo;
        }

        public String getGoodsName() {
            return goodsName;
        }

        public void setGoodsName(String goodsName) {
            this.goodsName = goodsName;
        }

        public String getOperatorName() {
            return operatorName;
        }

        public void setOperatorName(String operatorName) {
            this.operatorName = operatorName;
        }

        public String getDamageLevel() {
            return damageLevel;
        }

        public void setDamageLevel(String damageLevel) {
            this.damageLevel = damageLevel;
        }

        public String getDamageType() {
            return damageType;
        }

        public void setDamageType(String damageType) {
            this.damageType = damageType;
        }

        public java.math.BigDecimal getDamageArea() {
            return damageArea;
        }

        public void setDamageArea(java.math.BigDecimal damageArea) {
            this.damageArea = damageArea;
        }

        public String getDamageDescription() {
            return damageDescription;
        }

        public void setDamageDescription(String damageDescription) {
            this.damageDescription = damageDescription;
        }

        public java.math.BigDecimal getConfidence() {
            return confidence;
        }

        public void setConfidence(java.math.BigDecimal confidence) {
            this.confidence = confidence;
        }

        public java.time.LocalDateTime getDetectionTime() {
            return detectionTime;
        }

        public void setDetectionTime(java.time.LocalDateTime detectionTime) {
            this.detectionTime = detectionTime;
        }

        public String getSuggestion() {
            return suggestion;
        }

        public void setSuggestion(String suggestion) {
            this.suggestion = suggestion;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getProcessedImageUrl() {
            return processedImageUrl;
        }

        public void setProcessedImageUrl(String processedImageUrl) {
            this.processedImageUrl = processedImageUrl;
        }
    }
}
