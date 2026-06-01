package com.cesi.carbonasset.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cesi.common.core.domain.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("carbon_monthly_quota")
public class CarbonMonthlyQuota extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    private Integer year;

    private Integer month;

    private BigDecimal monthlyQuota;

    private String unit;
}
