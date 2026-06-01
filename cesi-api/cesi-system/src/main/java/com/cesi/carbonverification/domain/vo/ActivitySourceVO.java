package com.cesi.carbonverification.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ActivitySourceVO {
    private String emissionSource;
    private String emissionType;
    private String dataPeriod;
    private String dataMethod;
    private String unit;
    private BigDecimal annualActivityValue;
    private BigDecimal emissionFactor;
    private String factorSource;
    private BigDecimal annualEmissionAmount;
    private BigDecimal month1Value;
    private BigDecimal month1Factor;
    private BigDecimal month2Value;
    private String voucherStatus;
}
