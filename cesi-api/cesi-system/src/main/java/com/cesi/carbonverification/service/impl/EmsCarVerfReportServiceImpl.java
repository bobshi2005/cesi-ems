package com.cesi.carbonverification.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cesi.carbonverification.domain.EmsCarVerfActivity;
import com.cesi.carbonverification.domain.EmsCarVerfReport;
import com.cesi.carbonverification.domain.EmsCarVerfVoucher;
import com.cesi.carbonverification.domain.vo.ReportPreviewVO;
import com.cesi.carbonverification.mapper.EmsCarVerfActivityMapper;
import com.cesi.carbonverification.mapper.EmsCarVerfReportMapper;
import com.cesi.carbonverification.mapper.EmsCarVerfVoucherMapper;
import com.cesi.carbonverification.service.IEmsCarVerfReportService;
import com.cesi.common.config.BaseConfig;
import com.cesi.common.utils.SecurityUtils;
import com.cesi.common.utils.file.FileUtils;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class EmsCarVerfReportServiceImpl implements IEmsCarVerfReportService {

    private final EmsCarVerfReportMapper reportMapper;
    private final EmsCarVerfActivityMapper activityMapper;
    private final EmsCarVerfVoucherMapper voucherMapper;

    @Override
    public Page<EmsCarVerfReport> listPage(Integer reportYear, String orgUnit,
                                            long pageNum, long pageSize) {
        Page<EmsCarVerfReport> page = new Page<>(pageNum, pageSize);
        return reportMapper.selectPage(page, reportYear, orgUnit);
    }

    @Override
    public ReportPreviewVO checkStatus(Integer year, String orgUnit) {
        return buildPreviewVO(year, orgUnit, null);
    }

    @Override
    public EmsCarVerfReport generate(Integer year, String orgUnit, String standard,
                                      String templateType, String sections) {
        ReportPreviewVO preview = buildPreviewVO(year, orgUnit, null);

        String operator = SecurityUtils.getUsername();
        Date now = new Date();

        EmsCarVerfReport report = new EmsCarVerfReport();
        report.setReportName(year + "年度温室气体排放报告");
        report.setReportYear(year);
        report.setOrgUnit(orgUnit);
        report.setStandard(standard);
        report.setTemplateType(templateType);
        report.setSections(sections);
        report.setTotalEmission(preview.getTotalEmission());
        report.setDataCompleteness(preview.getDataCompleteness());
        report.setReportVersion("v0.1");
        report.setStatus("0");
        report.setGenerateTime(now);
        report.setCreateBy(operator);
        report.setCreateTime(now);
        report.setUpdateBy(operator);
        report.setUpdateTime(now);

        reportMapper.insert(report);
        return report;
    }

    @Override
    public ReportPreviewVO preview(Long id) {
        EmsCarVerfReport report = reportMapper.selectById(id);
        if (report == null) throw new RuntimeException("报告不存在");
        return buildPreviewVO(report.getReportYear(), report.getOrgUnit(), report.getSections());
    }

    @Override
    public void exportExcel(Long id, HttpServletResponse response) throws Exception {
        EmsCarVerfReport report = reportMapper.selectById(id);
        if (report == null) throw new RuntimeException("报告不存在");

        ReportPreviewVO vo = buildPreviewVO(report.getReportYear(), report.getOrgUnit(), report.getSections());
        Set<String> sec = vo.getSections();
        List<EmsCarVerfActivity> details = activityMapper.selectByYear(
                report.getReportYear(), report.getOrgUnit());

        // 按固定顺序计算每个选中章节的中文序号
        List<String> secOrderList = Arrays.asList(
                "basicInfo", "boundary", "activityData", "factor", "result", "uncertainty", "voucherList");
        String[] chNums = {"一", "二", "三", "四", "五", "六", "七"};
        Map<String, String> sectionNum = new LinkedHashMap<>();
        int chIdx = 0;
        for (String key : secOrderList) {
            if (sec.contains(key)) {
                sectionNum.put(key, chNums[chIdx++]);
            }
        }

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            CellStyle titleStyle = createTitleStyle(wb);
            CellStyle headerStyle = createHeaderStyle(wb);
            CellStyle dataStyle = createDataStyle(wb);

            // 企业基本信息
            if (sec.contains("basicInfo")) {
                Sheet s = wb.createSheet(sectionNum.get("basicInfo") + "、企业基本信息");
                s.setColumnWidth(0, 6000); s.setColumnWidth(1, 10000);
                createTextSheet(s, titleStyle, dataStyle,
                    report.getReportYear() + "年度温室气体排放报告",
                    new String[][]{
                        {"核查年度", report.getReportYear() + "年"},
                        {"组织范围", report.getOrgUnit()},
                        {"核算标准", report.getStandard()},
                        {"报告说明", "本报告依据国家发展和改革委员会发布的温室气体核算方法与报告指南，对本企业" +
                                report.getReportYear() + "年度温室气体排放情况进行核算与报告。"}
                    });
            }

            // 核算边界与排放源
            if (sec.contains("boundary")) {
                Sheet s = wb.createSheet(sectionNum.get("boundary") + "、核算边界与排放源");
                int r = 0;
                String[] h = {"排放源", "排放类型", "气体种类", "是否纳入核算"};
                Row hRow = s.createRow(r++);
                for (int i = 0; i < h.length; i++) {
                    Cell c = hRow.createCell(i); c.setCellValue(h[i]); c.setCellStyle(headerStyle);
                    s.setColumnWidth(i, 5500);
                }
                for (ReportPreviewVO.SourceItem item : vo.getSourceItems()) {
                    Row dRow = s.createRow(r++);
                    boolean direct = "DIRECT".equals(item.getEmissionType());
                    dRow.createCell(0).setCellValue(item.getEmissionSource());
                    dRow.createCell(1).setCellValue(direct ? "直接排放（范围一）" : "间接排放（范围二）");
                    dRow.createCell(2).setCellValue(direct ? "CO2, CH4, N2O" : "CO2");
                    dRow.createCell(3).setCellValue("是");
                    for (int i = 0; i < 4; i++) dRow.getCell(i).setCellStyle(dataStyle);
                }
            }

            // 活动数据明细
            if (sec.contains("activityData")) {
                Sheet s = wb.createSheet(sectionNum.get("activityData") + "、活动数据明细");
                int r = 0;
                String[] h = {"排放源", "月份", "活动量", "单位", "数据获取方式"};
                Row hRow = s.createRow(r++);
                for (int i = 0; i < h.length; i++) {
                    Cell c = hRow.createCell(i); c.setCellValue(h[i]); c.setCellStyle(headerStyle);
                    s.setColumnWidth(i, 4500);
                }
                for (EmsCarVerfActivity a : details) {
                    Row dRow = s.createRow(r++);
                    dRow.createCell(0).setCellValue(a.getEmissionSource());
                    dRow.createCell(1).setCellValue(a.getMonth() != null ? a.getMonth() + "月" : "");
                    dRow.createCell(2).setCellValue(a.getActivityValue() != null ? a.getActivityValue().doubleValue() : 0);
                    dRow.createCell(3).setCellValue(a.getUnit() != null ? a.getUnit() : "");
                    dRow.createCell(4).setCellValue(a.getDataMethod() != null ? a.getDataMethod() : "");
                    for (int i = 0; i < 5; i++) dRow.getCell(i).setCellStyle(dataStyle);
                }
            }

            // 排放因子
            if (sec.contains("factor")) {
                Sheet s = wb.createSheet(sectionNum.get("factor") + "、排放因子");
                int r = 0;
                String[] h = {"排放源", "排放因子", "单位", "因子来源"};
                Row hRow = s.createRow(r++);
                for (int i = 0; i < h.length; i++) {
                    Cell c = hRow.createCell(i); c.setCellValue(h[i]); c.setCellStyle(headerStyle);
                    s.setColumnWidth(i, 6000);
                }
                for (ReportPreviewVO.SourceItem item : vo.getSourceItems()) {
                    Row dRow = s.createRow(r++);
                    dRow.createCell(0).setCellValue(item.getEmissionSource());
                    dRow.createCell(1).setCellValue(item.getEmissionFactor() != null ? item.getEmissionFactor().doubleValue() : 0);
                    dRow.createCell(2).setCellValue("tCO2e/" + (item.getUnit() != null ? item.getUnit() : ""));
                    dRow.createCell(3).setCellValue(item.getFactorSource() != null ? item.getFactorSource() : "国家缺省值");
                    for (int i = 0; i < 4; i++) dRow.getCell(i).setCellStyle(dataStyle);
                }
            }

            // 排放量计算结果
            if (sec.contains("result")) {
                Sheet s = wb.createSheet(sectionNum.get("result") + "、排放量计算结果");
                int r = 0;
                Row tRow = s.createRow(r++);
                Cell tCell = tRow.createCell(0);
                tCell.setCellValue(report.getReportYear() + "年度温室气体排放报告");
                tCell.setCellStyle(titleStyle);
                s.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
                r++;
                String[] h = {"排放源", "排放类型", "年活动量", "单位", "排放因子", "年排放量(tCO2e)"};
                Row hRow = s.createRow(r++);
                for (int i = 0; i < h.length; i++) {
                    Cell c = hRow.createCell(i); c.setCellValue(h[i]); c.setCellStyle(headerStyle);
                    s.setColumnWidth(i, 5000);
                }
                for (ReportPreviewVO.SourceItem item : vo.getSourceItems()) {
                    Row dRow = s.createRow(r++);
                    dRow.createCell(0).setCellValue(item.getEmissionSource());
                    dRow.createCell(1).setCellValue("DIRECT".equals(item.getEmissionType()) ? "直接排放" : "间接排放");
                    dRow.createCell(2).setCellValue(item.getActivityValue() != null ? item.getActivityValue().doubleValue() : 0);
                    dRow.createCell(3).setCellValue(item.getUnit() != null ? item.getUnit() : "");
                    dRow.createCell(4).setCellValue(item.getEmissionFactor() != null ? item.getEmissionFactor().doubleValue() : 0);
                    dRow.createCell(5).setCellValue(item.getTotalEmission() != null ? item.getTotalEmission().doubleValue() : 0);
                    for (int i = 0; i < 6; i++) dRow.getCell(i).setCellStyle(dataStyle);
                }
                Row totalRow = s.createRow(r);
                totalRow.createCell(0).setCellValue("合计");
                totalRow.createCell(5).setCellValue(vo.getTotalEmission() != null ? vo.getTotalEmission().doubleValue() : 0);
            }

            // 不确定性分析
            if (sec.contains("uncertainty")) {
                Sheet s = wb.createSheet(sectionNum.get("uncertainty") + "、不确定性分析");
                s.setColumnWidth(0, 8000); s.setColumnWidth(1, 14000);
                createTextSheet(s, titleStyle, dataStyle,
                    "不确定性分析",
                    new String[][]{
                        {"分析类别", "说明"},
                        {"活动数据不确定性", "活动数据来源于能源管理系统实时采集，计量仪器经过定期检定，不确定性≤±2%"},
                        {"排放因子不确定性", "采用国家发改委公布的缺省排放因子，不确定性符合国家标准规定范围"},
                        {"综合不确定性", "综合不确定性评估结果满足温室气体核算与报告的质量要求"}
                    });
            }

            // 附件凭证清单
            if (sec.contains("voucherList")) {
                List<EmsCarVerfVoucher> vouchers = voucherMapper.selectUploadedMonths(
                        report.getReportYear(), report.getOrgUnit());
                Sheet s = wb.createSheet(sectionNum.get("voucherList") + "、附件凭证清单");
                int r = 0;
                String[] h = {"序号", "凭证月份", "排放源", "凭证类型", "凭证名称", "上传时间"};
                Row hRow = s.createRow(r++);
                for (int i = 0; i < h.length; i++) {
                    Cell c = hRow.createCell(i); c.setCellValue(h[i]); c.setCellStyle(headerStyle);
                    s.setColumnWidth(i, i == 4 ? 8000 : 4500);
                }
                int idx = 1;
                for (EmsCarVerfVoucher v : vouchers) {
                    Row dRow = s.createRow(r++);
                    dRow.createCell(0).setCellValue(idx++);
                    dRow.createCell(1).setCellValue(report.getReportYear() + "年" + v.getMonth() + "月");
                    dRow.createCell(2).setCellValue(v.getEmissionSource() != null ? v.getEmissionSource() : "");
                    dRow.createCell(3).setCellValue(v.getVoucherType() != null ? v.getVoucherType() : "");
                    dRow.createCell(4).setCellValue(v.getVoucherName() != null ? v.getVoucherName() : "");
                    dRow.createCell(5).setCellValue(v.getUploadTime() != null ? v.getUploadTime().toString() : "");
                    for (int i = 0; i < 6; i++) dRow.getCell(i).setCellStyle(dataStyle);
                }
            }

            String fileName = report.getReportYear() + "年度排放报告.xlsx";
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            FileUtils.setAttachmentResponseHeader(response, fileName);
            wb.write(response.getOutputStream());
        }
    }

    @Override
    public void deleteById(Long id) {
        EmsCarVerfReport report = reportMapper.selectById(id);
        if (report == null) throw new RuntimeException("报告不存在");
        reportMapper.deleteById(id);
    }

    private void createTextSheet(Sheet sheet, CellStyle titleStyle, CellStyle dataStyle,
                                  String title, String[][] rows) {
        int r = 0;
        Row tRow = sheet.createRow(r++);
        Cell tCell = tRow.createCell(0);
        tCell.setCellValue(title);
        tCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 1));
        r++;
        for (String[] pair : rows) {
            Row dRow = sheet.createRow(r++);
            Cell k = dRow.createCell(0); k.setCellValue(pair[0]); k.setCellStyle(dataStyle);
            Cell v = dRow.createCell(1); v.setCellValue(pair[1]); v.setCellStyle(dataStyle);
        }
    }

    private ReportPreviewVO buildPreviewVO(Integer year, String orgUnit, String sectionsStr) {
        List<EmsCarVerfActivity> summary = activityMapper.selectAnnualSummary(year, orgUnit);
        List<EmsCarVerfVoucher> vouchers = voucherMapper.selectUploadedMonths(year, orgUnit);

        Set<String> uploadedKeys = vouchers.stream()
                .map(v -> v.getEmissionSource() + "_" + v.getMonth())
                .collect(Collectors.toSet());

        ReportPreviewVO vo = new ReportPreviewVO();
        vo.setReportYear(year);
        vo.setOrgUnit(orgUnit);

        BigDecimal total = BigDecimal.ZERO;
        List<ReportPreviewVO.SourceItem> items = new ArrayList<>();

        for (EmsCarVerfActivity a : summary) {
            ReportPreviewVO.SourceItem item = new ReportPreviewVO.SourceItem();
            item.setEmissionSource(a.getEmissionSource());
            item.setEmissionType(a.getEmissionType());
            item.setActivityValue(a.getActivityValue());
            item.setUnit(a.getUnit());
            item.setEmissionFactor(a.getEmissionFactor());
            item.setFactorSource(a.getFactorSource());
            item.setTotalEmission(a.getEmissionAmount());

            long voucherMonths = uploadedKeys.stream()
                    .filter(k -> k.startsWith(a.getEmissionSource() + "_"))
                    .count();
            item.setVoucherMonths((int) voucherMonths);

            if (a.getEmissionAmount() != null) total = total.add(a.getEmissionAmount());
            items.add(item);
        }

        vo.setTotalEmission(total.setScale(2, RoundingMode.HALF_UP));
        vo.setSourceItems(items);

        // 数据完整度
        int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int expectedMonths = Objects.equals(year, currentYear) ? currentMonth : 12;
        int totalSlots = items.size() * expectedMonths;
        int uploadedSlots = (int) uploadedKeys.stream()
                .filter(k -> {
                    String[] parts = k.split("_");
                    return parts.length == 2 && Integer.parseInt(parts[1]) <= expectedMonths;
                }).count();

        BigDecimal completeness = totalSlots > 0
                ? BigDecimal.valueOf(uploadedSlots * 100.0 / totalSlots).setScale(1, RoundingMode.HALF_UP)
                : BigDecimal.valueOf(100);
        vo.setDataCompleteness(completeness);
        vo.setCheckDataStatus(completeness.compareTo(BigDecimal.valueOf(90)) >= 0 ? "PASS" : "WARN");
        vo.setCheckFactorStatus("PASS");
        vo.setCheckVoucherStatus(completeness.compareTo(BigDecimal.valueOf(100)) >= 0 ? "PASS" : "WARN");

        // 解析章节列表
        Set<String> allSections = new java.util.LinkedHashSet<>(Arrays.asList(
                "basicInfo", "boundary", "activityData", "factor", "result", "uncertainty", "voucherList"));
        if (sectionsStr != null && !sectionsStr.isEmpty()) {
            Set<String> selected = new java.util.HashSet<>(Arrays.asList(sectionsStr.split(",")));
            allSections.retainAll(selected);
        }
        vo.setSections(allSections);

        return vo;
    }

    private CellStyle createTitleStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createDataStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }
}
