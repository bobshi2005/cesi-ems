package com.cesi.carbonasset.domain.vo;

import com.cesi.carbonasset.domain.CarbonAsset;
import com.cesi.carbonasset.domain.CarbonMonthlyQuota;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CarbonAssetPageVO {

    private List<CarbonAsset> quotaList;

    private List<CarbonAsset> ccerList;

    private List<CarbonAsset> greenCertList;

    private List<CarbonMonthlyQuota> monthlyQuotaList;

    /** 碳配额总量 = 政府发放 + 上年剩余 + 本年买入 - 本年卖出 */
    private BigDecimal totalQuota;
}
