package com.cesi.carbonverification.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cesi.carbonverification.domain.EmsCarVerfActivity;
import com.cesi.carbonverification.domain.EmsCarVerfVoucher;
import com.cesi.carbonverification.domain.vo.ActivitySourceVO;
import com.cesi.carbonverification.domain.vo.ActivitySummaryVO;
import com.cesi.carbonverification.mapper.EmsCarVerfActivityMapper;
import com.cesi.carbonverification.mapper.EmsCarVerfVoucherMapper;
import com.cesi.carbonverification.service.IEmsCarVerfActivityService;
import com.cesi.common.utils.SecurityUtils;
import com.cesi.common.utils.poi.ExcelUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class EmsCarVerfActivityServiceImpl implements IEmsCarVerfActivityService {

    private final EmsCarVerfActivityMapper activityMapper;
    private final EmsCarVerfVoucherMapper voucherMapper;

    @Override
    public Page<EmsCarVerfActivity> listPage(Integer year, String orgUnit, String emissionSource,
                                              long pageNum, long pageSize) {
        Page<EmsCarVerfActivity> page = new Page<>(pageNum, pageSize);
        return activityMapper.selectPage(page, year, orgUnit, emissionSource);
    }

    @Override
    public ActivitySummaryVO getSummary(Integer year, String orgUnit) {
        List<EmsCarVerfActivity> all = activityMapper.selectByYear(year, orgUnit);
        List<EmsCarVerfVoucher> vouchers = voucherMapper.selectUploadedMonths(year, orgUnit);

        ActivitySummaryVO vo = new ActivitySummaryVO();

        BigDecimal totalEmission = all.stream()
                .filter(a -> a.getEmissionAmount() != null)
                .map(EmsCarVerfActivity::getEmissionAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setTotalEmission(totalEmission.setScale(2, RoundingMode.HALF_UP));

        Set<String> sources = all.stream().map(EmsCarVerfActivity::getEmissionSource)
                .collect(Collectors.toSet());
        vo.setSourceCount(sources.size());

        // 统计直接/间接排放源数量（按排放源去重后计数）
        Map<String, String> sourceTypeMap = new LinkedHashMap<>();
        for (EmsCarVerfActivity a : all) {
            sourceTypeMap.putIfAbsent(a.getEmissionSource(), a.getEmissionType());
        }
        long directCount = sourceTypeMap.values().stream().filter("DIRECT"::equals).count();
        long indirectCount = sourceTypeMap.values().stream().filter("INDIRECT"::equals).count();
        vo.setDirectCount((int) directCount);
        vo.setIndirectCount((int) indirectCount);

        // 月度凭证热力矩阵
        int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        boolean isSameYear = Objects.equals(year, currentYear);

        // 已上传凭证：源 -> 月份集合
        Map<String, Set<Integer>> uploadedMap = new HashMap<>();
        for (EmsCarVerfVoucher v : vouchers) {
            uploadedMap.computeIfAbsent(v.getEmissionSource(), k -> new HashSet<>())
                    .add(v.getMonth());
        }

        Map<String, List<Integer>> monthlyProgress = new LinkedHashMap<>();
        int totalSlots = 0, uploadedSlots = 0, missingSlots = 0;
        List<String> missingSources = new ArrayList<>();

        for (String src : sources) {
            List<Integer> progress = new ArrayList<>();
            Set<Integer> uploaded = uploadedMap.getOrDefault(src, Collections.emptySet());
            for (int m = 1; m <= 12; m++) {
                if (isSameYear && m > currentMonth) {
                    progress.add(0); // 未到期
                } else {
                    totalSlots++;
                    if (uploaded.contains(m)) {
                        progress.add(1);
                        uploadedSlots++;
                    } else {
                        progress.add(2);
                        missingSlots++;
                        if (!missingSources.contains(src)) missingSources.add(src);
                    }
                }
            }
            monthlyProgress.put(src, progress);
        }

        vo.setMonthlyProgress(monthlyProgress);
        vo.setVoucherUploadedMonths(uploadedSlots);

        BigDecimal completeness = totalSlots > 0
                ? BigDecimal.valueOf(uploadedSlots * 100.0 / totalSlots).setScale(1, RoundingMode.HALF_UP)
                : BigDecimal.valueOf(100);
        vo.setDataCompleteness(completeness);

        if (!missingSources.isEmpty()) {
            vo.setMissingWarning(String.join("、", missingSources) + " 存在未上传凭证，请及时补充");
        }

        return vo;
    }

    @Override
    public List<ActivitySourceVO> getSourceList(Integer year, String orgUnit) {
        List<ActivitySourceVO> sources = activityMapper.selectSourceList(year, orgUnit);
        List<EmsCarVerfVoucher> vouchers = voucherMapper.selectUploadedMonths(year, orgUnit);

        int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int expectedMonths = Objects.equals(year, currentYear) ? currentMonth : 12;

        Map<String, Set<Integer>> uploadedMap = new HashMap<>();
        for (EmsCarVerfVoucher v : vouchers) {
            uploadedMap.computeIfAbsent(v.getEmissionSource(), k -> new HashSet<>()).add(v.getMonth());
        }

        for (ActivitySourceVO src : sources) {
            Set<Integer> uploaded = uploadedMap.getOrDefault(src.getEmissionSource(), Collections.emptySet());
            long uploadedCount = uploaded.stream().filter(m -> m <= expectedMonths).count();
            if (uploadedCount == 0) src.setVoucherStatus("NONE");
            else if (uploadedCount >= expectedMonths) src.setVoucherStatus("COMPLETE");
            else src.setVoucherStatus("PARTIAL");
            src.setDataPeriod("月度值");
        }
        return sources;
    }

    @Override
    public List<EmsCarVerfActivity> getDetail(Integer year, String orgUnit, String emissionSource) {
        return activityMapper.selectByYearAndSource(year, orgUnit, emissionSource);
    }

    @Override
    public int add(EmsCarVerfActivity entity) {
        String operator = SecurityUtils.getUsername();
        Date now = new Date();
        entity.setCreateBy(operator);
        entity.setCreateTime(now);
        entity.setUpdateBy(operator);
        entity.setUpdateTime(now);
        calculateEmission(entity);
        return activityMapper.insert(entity);
    }

    @Override
    public int edit(EmsCarVerfActivity entity) {
        entity.setUpdateBy(SecurityUtils.getUsername());
        entity.setUpdateTime(new Date());
        calculateEmission(entity);
        return activityMapper.updateById(entity);
    }

    @Override
    public int deleteById(Long id) {
        return activityMapper.deleteById(id);
    }

    @Override
    public String importData(MultipartFile file, Integer year, String orgUnit) throws Exception {
        ExcelUtil<EmsCarVerfActivity> util = new ExcelUtil<>(EmsCarVerfActivity.class);
        List<EmsCarVerfActivity> list = util.importExcel(file.getInputStream());
        if (list == null || list.isEmpty()) {
            return "导入数据为空";
        }
        String operator = SecurityUtils.getUsername();
        Date now = new Date();
        int success = 0, fail = 0;
        for (EmsCarVerfActivity item : list) {
            try {
                if (item.getYear() == null) item.setYear(year);
                if (item.getOrgUnit() == null || item.getOrgUnit().isEmpty()) item.setOrgUnit(orgUnit);
                if (item.getDataMethod() == null) item.setDataMethod("Excel导入");
                item.setUpdateBy(operator);
                item.setUpdateTime(now);
                calculateEmission(item);
                // upsert：已存在则更新，不存在则插入
                EmsCarVerfActivity existing = activityMapper.selectOneByKey(
                        item.getYear(), item.getMonth(), item.getEmissionSource(), item.getOrgUnit());
                if (existing != null) {
                    item.setId(existing.getId());
                    activityMapper.updateById(item);
                } else {
                    item.setCreateBy(operator);
                    item.setCreateTime(now);
                    activityMapper.insert(item);
                }
                success++;
            } catch (Exception e) {
                fail++;
            }
        }
        return String.format("导入完成：成功 %d 条，失败 %d 条", success, fail);
    }

    @Override
    public int updateFactorBySource(Integer year, String orgUnit, String emissionSource,
                                    BigDecimal emissionFactor, String factorSource) {
        List<EmsCarVerfActivity> records = activityMapper.selectByYearAndSource(year, orgUnit, emissionSource);
        if (records.isEmpty()) return 0;
        String operator = SecurityUtils.getUsername();
        Date now = new Date();
        for (EmsCarVerfActivity record : records) {
            record.setEmissionFactor(emissionFactor);
            if (factorSource != null && !factorSource.isEmpty()) {
                record.setFactorSource(factorSource);
            }
            record.setUpdateBy(operator);
            record.setUpdateTime(now);
            calculateEmission(record);
            activityMapper.updateById(record);
        }
        return records.size();
    }

    @Override
    public List<EmsCarVerfActivity> exportList(Integer year, String orgUnit) {
        return activityMapper.selectByYear(year, orgUnit);
    }

    private void calculateEmission(EmsCarVerfActivity entity) {
        if (entity.getActivityValue() != null && entity.getEmissionFactor() != null) {
            // emission_amount(tCO2e) = activity_value × emission_factor
            // 排放因子单位已为 tCO2/计量单位，直接相乘
            entity.setEmissionAmount(
                    entity.getActivityValue().multiply(entity.getEmissionFactor())
                            .setScale(4, RoundingMode.HALF_UP));
        }
    }
}
