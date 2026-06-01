package com.cesi.carbonverification.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cesi.carbonverification.domain.EmsCarVerfActivity;
import com.cesi.carbonverification.domain.EmsCarVerfPackage;
import com.cesi.carbonverification.domain.EmsCarVerfReport;
import com.cesi.carbonverification.domain.EmsCarVerfVoucher;
import com.cesi.carbonverification.domain.vo.PackageCompletenessVO;
import com.cesi.carbonverification.mapper.EmsCarVerfActivityMapper;
import com.cesi.carbonverification.mapper.EmsCarVerfPackageMapper;
import com.cesi.carbonverification.mapper.EmsCarVerfReportMapper;
import com.cesi.carbonverification.mapper.EmsCarVerfVoucherMapper;
import com.cesi.carbonverification.service.IEmsCarVerfPackageService;
import com.cesi.common.config.BaseConfig;
import com.cesi.common.utils.SecurityUtils;
import com.cesi.common.utils.file.FileUtils;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@AllArgsConstructor
public class EmsCarVerfPackageServiceImpl implements IEmsCarVerfPackageService {

    private final EmsCarVerfPackageMapper packageMapper;
    private final EmsCarVerfActivityMapper activityMapper;
    private final EmsCarVerfVoucherMapper voucherMapper;
    private final EmsCarVerfReportMapper reportMapper;

    @Override
    public Page<EmsCarVerfPackage> listPage(Integer packageYear, long pageNum, long pageSize) {
        Page<EmsCarVerfPackage> page = new Page<>(pageNum, pageSize);
        return packageMapper.selectPage(page, packageYear);
    }

    @Override
    public PackageCompletenessVO getCompleteness(Integer year, String orgUnit) {
        List<EmsCarVerfActivity> activities = activityMapper.selectByYear(year, orgUnit);
        List<EmsCarVerfVoucher> vouchers = voucherMapper.selectUploadedMonths(year, orgUnit);

        int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int expectedMonths = Objects.equals(year, currentYear) ? currentMonth : 12;

        Set<String> sources = activities.stream().map(EmsCarVerfActivity::getEmissionSource)
                .collect(Collectors.toSet());
        int totalSlots = sources.size() * expectedMonths;

        Set<String> uploadedKeys = vouchers.stream()
                .map(v -> v.getEmissionSource() + "_" + v.getMonth())
                .collect(Collectors.toSet());
        int uploadedSlots = (int) uploadedKeys.stream()
                .filter(k -> Integer.parseInt(k.split("_")[1]) <= expectedMonths).count();

        boolean reportReady = !reportMapper.selectPage(
                new Page<EmsCarVerfReport>(1, 1), year, orgUnit).getRecords().isEmpty();

        PackageCompletenessVO vo = new PackageCompletenessVO();
        vo.setVoucherUploadedMonths(uploadedSlots);
        vo.setActivityDataCount(activities.size());
        vo.setActivityDataTotal(sources.size() * expectedMonths);
        vo.setFactorReady(true);
        vo.setReportReady(reportReady);

        // overall: voucher 50% + activity 30% + factor 10% + report 10%
        double voucherRate = totalSlots > 0 ? (double) uploadedSlots / totalSlots : 1.0;
        double activityRate = totalSlots > 0 ? (double) activities.size() / (sources.size() * expectedMonths) : 1.0;
        double overall = voucherRate * 50 + activityRate * 30 + 10 + (reportReady ? 10 : 0);
        vo.setOverallRate(BigDecimal.valueOf(overall).setScale(1, RoundingMode.HALF_UP));

        List<String> missing = new ArrayList<>();
        if (!reportReady) missing.add("排放报告未生成");
        if (uploadedSlots < totalSlots) missing.add("存在未上传凭证（" + (totalSlots - uploadedSlots) + " 个月份）");
        vo.setMissingDesc(String.join("；", missing));

        return vo;
    }

    @Override
    public EmsCarVerfPackage generate(EmsCarVerfPackage param) throws Exception {
        int year = param.getPackageYear();
        String orgUnit = "全厂";

        List<EmsCarVerfActivity> activities = activityMapper.selectByYear(year, orgUnit);
        List<EmsCarVerfVoucher> vouchers = "1".equals(param.getIncludeVoucher())
                ? voucherMapper.selectByYear(year, orgUnit) : Collections.emptyList();

        String zipDir = BaseConfig.getUploadPath() + "/carbonVerification/packages";
        new File(zipDir).mkdirs();
        String zipPath = zipDir + "/" + year + "年核查材料包_" + System.currentTimeMillis() + ".zip";

        int fileCount = 0;
        List<String> missingList = new ArrayList<>();

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath))) {
            // 01 排放报告
            if ("1".equals(param.getIncludeReport())) {
                // 查询最新报告记录，生成 Excel 放入 ZIP
                Page<EmsCarVerfReport> reportPage = reportMapper.selectPage(
                        new Page<>(1, 1), year, orgUnit);
                if (!reportPage.getRecords().isEmpty()) {
                    EmsCarVerfReport report = reportPage.getRecords().get(0);
                    byte[] reportBytes = buildReportExcel(report, activities, vouchers, year);
                    zos.putNextEntry(new ZipEntry("01_排放报告/" + year + "年度温室气体排放报告.xlsx"));
                    zos.write(reportBytes);
                    zos.closeEntry();
                    fileCount++;
                } else {
                    missingList.add("排放报告（尚未生成，请先在「排放报告生成」页面生成报告）");
                }
            }

            // 02 活动数据
            if ("1".equals(param.getIncludeActivity()) && !activities.isEmpty()) {
                byte[] excelBytes = buildActivityExcel(activities);
                zos.putNextEntry(new ZipEntry("02_活动数据/活动数据汇总_" + year + ".xlsx"));
                zos.write(excelBytes);
                zos.closeEntry();
                fileCount++;
            }

            // 03 原始凭证
            if ("1".equals(param.getIncludeVoucher())) {
                Set<String> usedEntries = new HashSet<>();
                for (EmsCarVerfVoucher v : vouchers) {
                    if (v.getFilePath() == null) continue;
                    File f = new File(v.getFilePath());
                    if (!f.exists()) { missingList.add(v.getVoucherName()); continue; }

                    // 用凭证显示名作为文件基名，format 作为扩展名
                    String baseName = sanitizeFileName(v.getVoucherName());
                    String ext = v.getFileFormat() != null ? "." + v.getFileFormat().toLowerCase() : "";
                    // 按 月/排放源/ 分目录，避免不同排放源同名冲突
                    String dir = "03_原始凭证/" + String.format("%02d月", v.getMonth())
                            + "/" + sanitizeFileName(v.getEmissionSource()) + "/";

                    String entryName = uniqueEntry(usedEntries, dir, baseName, ext);
                    usedEntries.add(entryName);

                    zos.putNextEntry(new ZipEntry(entryName));
                    try (FileInputStream fis = new FileInputStream(f)) {
                        byte[] buf = new byte[4096]; int len;
                        while ((len = fis.read(buf)) > 0) zos.write(buf, 0, len);
                    }
                    zos.closeEntry();
                    fileCount++;
                }
            }

            // 03 因子说明
            if ("1".equals(param.getIncludeFactor())) {
                String factorText = buildFactorText(activities, year);
                zos.putNextEntry(new ZipEntry("04_补充说明/排放因子来源说明.txt"));
                zos.write(factorText.getBytes("UTF-8"));
                zos.closeEntry();
                fileCount++;
            }

            // 04 缺失清单
            if (!missingList.isEmpty()) {
                String missingText = "以下凭证文件缺失：\n" + String.join("\n", missingList);
                zos.putNextEntry(new ZipEntry("04_补充说明/凭证缺失清单.txt"));
                zos.write(missingText.getBytes("UTF-8"));
                zos.closeEntry();
            }
        }

        long fileSize = new File(zipPath).length();
        String operator = SecurityUtils.getUsername();
        Date now = new Date();

        param.setFileCount(fileCount);
        param.setFileSize(fileSize);
        param.setFilePath(zipPath);
        param.setStatus("1");
        if (!missingList.isEmpty()) {
            param.setMissingDesc(String.join("；", missingList));
        }
        param.setCreateBy(operator);
        param.setCreateTime(now);
        param.setUpdateBy(operator);
        param.setUpdateTime(now);

        PackageCompletenessVO completeness = getCompleteness(year, orgUnit);
        param.setCompletenessRate(completeness.getOverallRate());

        packageMapper.insert(param);
        return param;
    }

    @Override
    public void download(Long id, HttpServletResponse response) throws Exception {
        EmsCarVerfPackage pkg = packageMapper.selectById(id);
        if (pkg == null || pkg.getFilePath() == null) throw new RuntimeException("材料包不存在");
        response.setContentType("application/zip");
        FileUtils.setAttachmentResponseHeader(response,
                pkg.getPackageYear() + "年核查材料包.zip");
        FileUtils.writeBytes(pkg.getFilePath(), response.getOutputStream());
    }

    private byte[] buildReportExcel(EmsCarVerfReport report,
                                     List<EmsCarVerfActivity> activities,
                                     List<EmsCarVerfVoucher> vouchers,
                                     int year) throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            // 汇总 Sheet
            Sheet sheet = wb.createSheet("排放汇总");
            Row titleRow = sheet.createRow(0);
            titleRow.createCell(0).setCellValue(year + "年度温室气体排放报告");
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 5));

            String[] headers = {"排放源", "排放类型", "年活动量", "单位", "排放因子", "年排放量(tCO2e)"};
            Row hRow = sheet.createRow(2);
            for (int i = 0; i < headers.length; i++) {
                hRow.createCell(i).setCellValue(headers[i]);
                sheet.setColumnWidth(i, 4500);
            }

            Map<String, EmsCarVerfActivity> summaryMap = new java.util.LinkedHashMap<>();
            for (EmsCarVerfActivity a : activities) {
                summaryMap.merge(a.getEmissionSource(), a, (old, cur) -> {
                    if (cur.getActivityValue() != null && old.getActivityValue() != null)
                        old.setActivityValue(old.getActivityValue().add(cur.getActivityValue()));
                    if (cur.getEmissionAmount() != null && old.getEmissionAmount() != null)
                        old.setEmissionAmount(old.getEmissionAmount().add(cur.getEmissionAmount()));
                    return old;
                });
            }

            int r = 3;
            BigDecimal grandTotal = BigDecimal.ZERO;
            for (EmsCarVerfActivity a : summaryMap.values()) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(a.getEmissionSource());
                row.createCell(1).setCellValue("DIRECT".equals(a.getEmissionType()) ? "直接排放" : "间接排放");
                row.createCell(2).setCellValue(a.getActivityValue() != null ? a.getActivityValue().doubleValue() : 0);
                row.createCell(3).setCellValue(a.getUnit() != null ? a.getUnit() : "");
                row.createCell(4).setCellValue(a.getEmissionFactor() != null ? a.getEmissionFactor().doubleValue() : 0);
                row.createCell(5).setCellValue(a.getEmissionAmount() != null ? a.getEmissionAmount().doubleValue() : 0);
                if (a.getEmissionAmount() != null) grandTotal = grandTotal.add(a.getEmissionAmount());
            }
            Row totalRow = sheet.createRow(r);
            totalRow.createCell(0).setCellValue("合计");
            totalRow.createCell(5).setCellValue(grandTotal.doubleValue());

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }

    private byte[] buildActivityExcel(List<EmsCarVerfActivity> activities) throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("活动数据");
            String[] headers = {"排放源", "排放类型", "月份", "活动量", "单位", "排放因子", "月排放量(tCO2e)", "数据方式"};
            Row hRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                hRow.createCell(i).setCellValue(headers[i]);
                sheet.setColumnWidth(i, 4500);
            }
            int r = 1;
            for (EmsCarVerfActivity a : activities) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(a.getEmissionSource());
                row.createCell(1).setCellValue("DIRECT".equals(a.getEmissionType()) ? "直接排放" : "间接排放");
                row.createCell(2).setCellValue(a.getMonth());
                row.createCell(3).setCellValue(a.getActivityValue() != null ? a.getActivityValue().doubleValue() : 0);
                row.createCell(4).setCellValue(a.getUnit() != null ? a.getUnit() : "");
                row.createCell(5).setCellValue(a.getEmissionFactor() != null ? a.getEmissionFactor().doubleValue() : 0);
                row.createCell(6).setCellValue(a.getEmissionAmount() != null ? a.getEmissionAmount().doubleValue() : 0);
                row.createCell(7).setCellValue(a.getDataMethod() != null ? a.getDataMethod() : "");
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }

    @Override
    public void deleteById(Long id) {
        EmsCarVerfPackage pkg = packageMapper.selectById(id);
        if (pkg == null) throw new RuntimeException("材料包不存在");
        // 同步删除磁盘上的 ZIP 文件
        if (pkg.getFilePath() != null) {
            File f = new File(pkg.getFilePath());
            if (f.exists()) f.delete();
        }
        packageMapper.deleteById(id);
    }

    /** 去除文件名/路径段中不合法字符 */
    private String sanitizeFileName(String name) {
        if (name == null || name.isBlank()) return "未知";
        return name.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
    }

    /** 若 dir+baseName+ext 已存在则追加 _2、_3… 直到唯一 */
    private String uniqueEntry(Set<String> used, String dir, String baseName, String ext) {
        String candidate = dir + baseName + ext;
        if (!used.contains(candidate)) return candidate;
        int idx = 2;
        while (used.contains(dir + baseName + "_" + idx + ext)) idx++;
        return dir + baseName + "_" + idx + ext;
    }

    private String buildFactorText(List<EmsCarVerfActivity> activities, int year) {
        StringBuilder sb = new StringBuilder();
        sb.append(year).append("年度排放因子来源说明\n");
        sb.append("=".repeat(50)).append("\n\n");
        activities.stream()
                .collect(Collectors.groupingBy(EmsCarVerfActivity::getEmissionSource))
                .forEach((src, list) -> {
                    sb.append("排放源：").append(src).append("\n");
                    BigDecimal factor = list.stream().filter(a -> a.getEmissionFactor() != null)
                            .map(EmsCarVerfActivity::getEmissionFactor)
                            .findFirst().orElse(BigDecimal.ZERO);
                    String factorSrc = list.stream().filter(a -> a.getFactorSource() != null)
                            .map(EmsCarVerfActivity::getFactorSource).findFirst().orElse("国家缺省值");
                    sb.append("  排放因子：").append(factor).append("\n");
                    sb.append("  来源：").append(factorSrc).append("\n\n");
                });
        return sb.toString();
    }
}
