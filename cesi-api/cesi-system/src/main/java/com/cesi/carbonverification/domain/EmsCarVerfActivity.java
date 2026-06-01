package com.cesi.carbonverification.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cesi.common.annotation.Excel;
import com.cesi.common.core.domain.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("ems_carbon_verification_activity")
public class EmsCarVerfActivity extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    @Excel(name = "核查年度")
    private Integer year;

    @Excel(name = "月份")
    private Integer month;

    @Excel(name = "组织单元")
    private String orgUnit;

    @Excel(name = "排放源")
    private String emissionSource;

    @Excel(name = "排放类型")
    private String emissionType;

    @Excel(name = "数据获取方式")
    private String dataMethod;

    @Excel(name = "计量单位")
    private String unit;

    @Excel(name = "活动量数值")
    private BigDecimal activityValue;

    @Excel(name = "排放因子")
    private BigDecimal emissionFactor;

    @Excel(name = "因子来源")
    private String factorSource;

    @Excel(name = "月排放量(tCO2e)")
    private BigDecimal emissionAmount;

    private String isEstimated;
}
