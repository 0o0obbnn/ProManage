package com.promanage.service.strategy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import com.promanage.service.entity.TestCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 测试用例导入导出策略
 *
 * @author ProManage Team
 * @date 2025-10-22
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TestCaseImportExportStrategy {

    /**
     * 导出测试用例为Excel
     */
    public byte[] exportToExcel(List<TestCase> testCases) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("测试用例");
            
            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "标题", "描述", "前置条件", "测试步骤", "预期结果", 
                              "优先级", "类型", "状态", "创建时间", "更新时间"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            
            // 填充数据
            int rowNum = 1;
            
            for (TestCase testCase : testCases) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(testCase.getId());
                row.createCell(1).setCellValue(testCase.getTitle());
                row.createCell(2).setCellValue(testCase.getDescription());
                row.createCell(3).setCellValue(testCase.getPreconditions());
                row.createCell(4).setCellValue(testCase.getSteps());
                row.createCell(5).setCellValue(testCase.getExpectedResult());
                row.createCell(6).setCellValue(testCase.getPriority());
                row.createCell(7).setCellValue(testCase.getType());
                row.createCell(8).setCellValue(testCase.getStatus());
                row.createCell(9).setCellValue(testCase.getCreateTime() != null ? 
                    testCase.getCreateTime().toString() : "");
                row.createCell(10).setCellValue(testCase.getUpdateTime() != null ? 
                    testCase.getUpdateTime().toString() : "");
            }
            
            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * 导出测试用例为CSV
     */
    public byte[] exportToCsv(List<TestCase> testCases) throws IOException {
        StringBuilder csv = new StringBuilder();
        csv.append("ID,标题,描述,前置条件,测试步骤,预期结果,优先级,类型,状态,创建时间,更新时间\n");
        
        for (TestCase testCase : testCases) {
            csv.append(testCase.getId()).append(",");
            csv.append(escapeCsv(testCase.getTitle())).append(",");
            csv.append(escapeCsv(testCase.getDescription())).append(",");
            csv.append(escapeCsv(testCase.getPreconditions())).append(",");
            csv.append(escapeCsv(testCase.getSteps())).append(",");
            csv.append(escapeCsv(testCase.getExpectedResult())).append(",");
            csv.append(testCase.getPriority()).append(",");
            csv.append(testCase.getType()).append(",");
            csv.append(testCase.getStatus()).append(",");
            csv.append(testCase.getCreateTime() != null ? testCase.getCreateTime().toString() : "").append(",");
            csv.append(testCase.getUpdateTime() != null ? testCase.getUpdateTime().toString() : "").append("\n");
        }
        
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 导出测试用例为TSV
     */
    public byte[] exportToTsv(List<TestCase> testCases) throws IOException {
        StringBuilder tsv = new StringBuilder();
        tsv.append("ID\t标题\t描述\t前置条件\t测试步骤\t预期结果\t优先级\t类型\t状态\t创建时间\t更新时间\n");
        
        for (TestCase testCase : testCases) {
            tsv.append(testCase.getId()).append("\t");
            tsv.append(testCase.getTitle()).append("\t");
            tsv.append(testCase.getDescription()).append("\t");
            tsv.append(testCase.getPreconditions()).append("\t");
            tsv.append(testCase.getSteps()).append("\t");
            tsv.append(testCase.getExpectedResult()).append("\t");
            tsv.append(testCase.getPriority()).append("\t");
            tsv.append(testCase.getType()).append("\t");
            tsv.append(testCase.getStatus()).append("\t");
            tsv.append(testCase.getCreateTime() != null ? testCase.getCreateTime().toString() : "").append("\t");
            tsv.append(testCase.getUpdateTime() != null ? testCase.getUpdateTime().toString() : "").append("\n");
        }
        
        return tsv.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 从Excel导入测试用例
     */
    public List<TestCase> importFromExcel(byte[] fileData, Long projectId) throws IOException {
        List<TestCase> testCases = new ArrayList<>();
        
        try (Workbook workbook = new XSSFWorkbook(new java.io.ByteArrayInputStream(fileData))) {
            Sheet sheet = workbook.getSheetAt(0);
            
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                TestCase testCase = new TestCase();
                testCase.setProjectId(projectId);
                testCase.setTitle(getCellValue(row.getCell(1)));
                testCase.setDescription(getCellValue(row.getCell(2)));
                testCase.setPreconditions(getCellValue(row.getCell(3)));
                testCase.setSteps(getCellValue(row.getCell(4)));
                testCase.setExpectedResult(getCellValue(row.getCell(5)));
                testCase.setPriority(convertPriority(getCellValue(row.getCell(6))));
                testCase.setType(getCellValue(row.getCell(7)));
                testCase.setStatus(convertStatus(getCellValue(row.getCell(8))));
                testCase.setCreateTime(LocalDateTime.now());
                
                testCases.add(testCase);
            }
        }
        
        return testCases;
    }

    /**
     * 从CSV导入测试用例
     */
    public List<TestCase> importFromCsv(String csvContent, Long projectId) throws IOException {
        List<TestCase> testCases = new ArrayList<>();
        String[] lines = csvContent.split("\n");
        
        for (int i = 1; i < lines.length; i++) {
            String[] fields = lines[i].split(",");
            if (fields.length < 8) continue;
            
            TestCase testCase = new TestCase();
            testCase.setProjectId(projectId);
            testCase.setTitle(fields[1]);
            testCase.setDescription(fields[2]);
            testCase.setPreconditions(fields[3]);
            testCase.setSteps(fields[4]);
            testCase.setExpectedResult(fields[5]);
            testCase.setPriority(convertPriority(fields[6]));
            testCase.setType(fields[7]);
            testCase.setStatus(convertStatus(fields.length > 8 ? fields[8] : "DRAFT"));
            testCase.setCreateTime(LocalDateTime.now());
            
            testCases.add(testCase);
        }
        
        return testCases;
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * 转换优先级字符串为整数
     * 1-低，2-中，3-高，4-紧急
     */
    private Integer convertPriority(String priority) {
        if (priority == null || priority.trim().isEmpty()) {
            return 2; // 默认中优先级
        }

        try {
            // 尝试直接解析为数字
            int value = Integer.parseInt(priority.trim());
            if (value >= 1 && value <= 4) {
                return value;
            }
        } catch (NumberFormatException e) {
            // 尝试按名称匹配
            switch (priority.trim().toUpperCase()) {
                case "低":
                case "LOW":
                    return 1;
                case "中":
                case "MEDIUM":
                case "NORMAL":
                    return 2;
                case "高":
                case "HIGH":
                    return 3;
                case "紧急":
                case "URGENT":
                case "CRITICAL":
                    return 4;
            }
        }

        // 默认中优先级
        return 2;
    }

    /**
     * 转换状态字符串为整数
     * 0-草稿, 1-待执行, 2-执行中, 3-通过, 4-失败, 5-阻塞, 6-跳过
     */
    private Integer convertStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return 0; // 默认草稿状态
        }

        try {
            // 尝试直接解析为数字
            int value = Integer.parseInt(status.trim());
            if (value >= 0 && value <= 6) {
                return value;
            }
        } catch (NumberFormatException e) {
            // 尝试按名称匹配
            switch (status.trim().toUpperCase()) {
                case "草稿":
                case "DRAFT":
                    return 0;
                case "待执行":
                case "PENDING":
                case "TODO":
                    return 1;
                case "执行中":
                case "IN_PROGRESS":
                case "RUNNING":
                    return 2;
                case "通过":
                case "PASSED":
                case "PASS":
                    return 3;
                case "失败":
                case "FAILED":
                case "FAIL":
                    return 4;
                case "阻塞":
                case "BLOCKED":
                    return 5;
                case "跳过":
                case "SKIPPED":
                case "SKIP":
                    return 6;
            }
        }

        // 默认草稿状态
        return 0;
    }
}
