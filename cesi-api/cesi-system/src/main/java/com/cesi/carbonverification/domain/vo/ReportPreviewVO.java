package com.cesi.carbonverification.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ReportPreviewVO {

    private Integer reportYear;
    private String orgUnit;
    private BigDecimal totalEmission;
    private BigDecimal dataCompleteness;
    private String checkDataStatus;
    private String checkFactorStatus;
    private String checkVoucherStatus;

    private List<SourceItem> sourceItems;
    /** 本次报告包含的章节 key 集合 */
    private java.util.Set<String> sections;

    @Data
    public static class SourceItem {
        private String emissionSource;
        private String emissionType;
        private BigDecimal activityValue;
        private String unit;
        private BigDecimal emissionFactor;
        private String factorSource;
        private BigDecimal totalEmission;
        private Integer voucherMonths;
    }
}
