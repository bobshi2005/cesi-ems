package com.cesi.carbonverification.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PackageCompletenessVO {

    private BigDecimal overallRate;
    private Integer voucherUploadedMonths;
    private Integer activityDataCount;
    private Integer activityDataTotal;
    private Boolean factorReady;
    private Boolean reportReady;
    private String missingDesc;
}
