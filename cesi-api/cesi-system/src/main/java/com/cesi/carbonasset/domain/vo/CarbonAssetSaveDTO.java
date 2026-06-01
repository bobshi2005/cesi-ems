package com.cesi.carbonasset.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CarbonAssetSaveDTO {

    private Integer year;

    private List<AssetItem> assets;

    private List<MonthlyItem> monthlyQuotas;

    @Data
    public static class AssetItem {
        private String assetType;
        private String subType;
        private BigDecimal quantity;
    }

    @Data
    public static class MonthlyItem {
        private Integer month;
        private BigDecimal monthlyQuota;
    }
}
