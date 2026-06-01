package com.cesi.carbonasset.service.impl;

import com.cesi.carbonasset.domain.CarbonAsset;
import com.cesi.carbonasset.domain.CarbonMonthlyQuota;
import com.cesi.carbonasset.domain.vo.CarbonAssetPageVO;
import com.cesi.carbonasset.domain.vo.CarbonAssetSaveDTO;
import com.cesi.carbonasset.mapper.CarbonAssetMapper;
import com.cesi.carbonasset.mapper.CarbonMonthlyQuotaMapper;
import com.cesi.carbonasset.service.ICarbonAssetService;
import com.cesi.common.utils.SecurityUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CarbonAssetServiceImpl implements ICarbonAssetService {

    private static final String QUOTA      = "QUOTA";
    private static final String CCER       = "CCER";
    private static final String GREEN_CERT = "GREEN_CERT";

    /**
     * 固定行定义：{subType, typeName, unit}
     * 决定了每种资产大类下有哪些行、显示名称和单位。
     */
    private static final Object[][] QUOTA_DEFS = {
        {"GOVT_ISSUE",   "政府发放配额", "tCO₂"},
        {"PREV_SURPLUS", "上年剩余配额", "tCO₂"},
        {"BUY_IN",       "本年买入配额", "tCO₂"},
        {"SELL_OUT",     "本年卖出配额", "tCO₂"},
    };
    private static final Object[][] CCER_DEFS = {
        {"OWN",      "自有CCER资产",  "tCO₂"},
        {"BUY_IN",   "本年买入CCER", "tCO₂"},
        {"SELL_OUT", "本年卖出CCER", "tCO₂"},
    };
    private static final Object[][] GREEN_CERT_DEFS = {
        {"OWN",      "自有绿证资产", "个"},
        {"BUY_IN",   "买入绿证资产", "个"},
        {"SELL_OUT", "卖出绿证资产", "个"},
    };

    private final CarbonAssetMapper        carbonAssetMapper;
    private final CarbonMonthlyQuotaMapper carbonMonthlyQuotaMapper;

    /**
     * 查询页面数据。
     * 若该年度尚无记录，自动插入初始行（quantity=0），再返回数据。
     * 使用事务保证初始化写入与最终查询的一致性。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CarbonAssetPageVO getPageData(Integer year) {
        autoInitIfAbsent(year);

        List<CarbonAsset> quotaList     = carbonAssetMapper.selectByYearAndType(year, QUOTA);
        List<CarbonAsset> ccerList      = carbonAssetMapper.selectByYearAndType(year, CCER);
        List<CarbonAsset> greenCertList = carbonAssetMapper.selectByYearAndType(year, GREEN_CERT);
        List<CarbonMonthlyQuota> monthlyList = carbonMonthlyQuotaMapper.selectByYear(year);

        CarbonAssetPageVO vo = new CarbonAssetPageVO();
        vo.setQuotaList(quotaList);
        vo.setCcerList(ccerList);
        vo.setGreenCertList(greenCertList);
        vo.setMonthlyQuotaList(monthlyList);
        vo.setTotalQuota(calcTotalQuota(quotaList));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveData(CarbonAssetSaveDTO dto) {
        String operator = SecurityUtils.getUsername();
        Date   now      = new Date();
        Map<String, String> nameMap = buildTypeNameMap();

        for (CarbonAssetSaveDTO.AssetItem item : dto.getAssets()) {
            CarbonAsset asset = new CarbonAsset();
            asset.setYear(dto.getYear());
            asset.setAssetType(item.getAssetType());
            asset.setSubType(item.getSubType());
            asset.setTypeName(nameMap.getOrDefault(item.getAssetType() + ":" + item.getSubType(), ""));
            asset.setQuantity(item.getQuantity() != null ? item.getQuantity() : BigDecimal.ZERO);
            asset.setUnit(GREEN_CERT.equals(item.getAssetType()) ? "个" : "tCO₂");
            asset.setCreateBy(operator);
            asset.setCreateTime(now);
            asset.setUpdateBy(operator);
            asset.setUpdateTime(now);
            carbonAssetMapper.upsertAsset(asset);   // ON CONFLICT DO UPDATE quantity
        }

        for (CarbonAssetSaveDTO.MonthlyItem item : dto.getMonthlyQuotas()) {
            CarbonMonthlyQuota quota = new CarbonMonthlyQuota();
            quota.setYear(dto.getYear());
            quota.setMonth(item.getMonth());
            quota.setMonthlyQuota(item.getMonthlyQuota() != null ? item.getMonthlyQuota() : BigDecimal.ZERO);
            quota.setUnit("tCO₂");
            quota.setCreateBy(operator);
            quota.setCreateTime(now);
            quota.setUpdateBy(operator);
            quota.setUpdateTime(now);
            carbonMonthlyQuotaMapper.upsertMonthlyQuota(quota);  // ON CONFLICT DO UPDATE monthly_quota
        }
    }

    // ---- 私有方法 ----

    /**
     * 自动初始化：仅当该年度对应类型的记录数不足时才插入缺失行。
     * 使用 initAsset / initMonthlyQuota（ON CONFLICT DO NOTHING），
     * 确保已有用户数据绝不被覆盖。
     */
    private void autoInitIfAbsent(Integer year) {
        String operator = SecurityUtils.getUsername();
        Date   now      = new Date();

        initAssetGroup(year, QUOTA,      QUOTA_DEFS,      operator, now);
        initAssetGroup(year, CCER,       CCER_DEFS,       operator, now);
        initAssetGroup(year, GREEN_CERT, GREEN_CERT_DEFS, operator, now);
        initMonthlyGroup(year, operator, now);
    }

    private void initAssetGroup(Integer year, String assetType, Object[][] defs,
                                 String operator, Date now) {
        long existCount = carbonAssetMapper.selectByYearAndType(year, assetType).size();
        if (existCount >= defs.length) {
            return; // 记录数已足，无需初始化
        }
        for (Object[] def : defs) {
            CarbonAsset asset = new CarbonAsset();
            asset.setYear(year);
            asset.setAssetType(assetType);
            asset.setSubType((String) def[0]);
            asset.setTypeName((String) def[1]);
            asset.setQuantity(BigDecimal.ZERO);
            asset.setUnit((String) def[2]);
            asset.setCreateBy(operator);
            asset.setCreateTime(now);
            asset.setUpdateBy(operator);
            asset.setUpdateTime(now);
            carbonAssetMapper.initAsset(asset);  // ON CONFLICT DO NOTHING，安全
        }
    }

    private void initMonthlyGroup(Integer year, String operator, Date now) {
        long existCount = carbonMonthlyQuotaMapper.selectByYear(year).size();
        if (existCount >= 12) {
            return; // 12 个月记录已足，无需初始化
        }
        for (int month = 1; month <= 12; month++) {
            CarbonMonthlyQuota quota = new CarbonMonthlyQuota();
            quota.setYear(year);
            quota.setMonth(month);
            quota.setMonthlyQuota(BigDecimal.ZERO);
            quota.setUnit("tCO₂");
            quota.setCreateBy(operator);
            quota.setCreateTime(now);
            quota.setUpdateBy(operator);
            quota.setUpdateTime(now);
            carbonMonthlyQuotaMapper.initMonthlyQuota(quota);  // ON CONFLICT DO NOTHING，安全
        }
    }

    /** 碳配额总量 = GOVT_ISSUE + PREV_SURPLUS + BUY_IN - SELL_OUT */
    private BigDecimal calcTotalQuota(List<CarbonAsset> quotaList) {
        Map<String, BigDecimal> map = quotaList.stream()
                .collect(Collectors.toMap(CarbonAsset::getSubType, CarbonAsset::getQuantity,
                        (a, b) -> a));
        return map.getOrDefault("GOVT_ISSUE",   BigDecimal.ZERO)
                  .add(map.getOrDefault("PREV_SURPLUS", BigDecimal.ZERO))
                  .add(map.getOrDefault("BUY_IN",       BigDecimal.ZERO))
                  .subtract(map.getOrDefault("SELL_OUT", BigDecimal.ZERO));
    }

    private Map<String, String> buildTypeNameMap() {
        Map<String, String> m = new HashMap<>();
        for (Object[] d : QUOTA_DEFS)      m.put(QUOTA      + ":" + d[0], (String) d[1]);
        for (Object[] d : CCER_DEFS)       m.put(CCER       + ":" + d[0], (String) d[1]);
        for (Object[] d : GREEN_CERT_DEFS) m.put(GREEN_CERT + ":" + d[0], (String) d[1]);
        return m;
    }
}
