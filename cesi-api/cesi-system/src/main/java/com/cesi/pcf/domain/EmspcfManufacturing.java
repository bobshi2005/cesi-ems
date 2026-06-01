package com.cesi.pcf.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cesi.common.annotation.Excel;
import com.cesi.common.core.domain.BaseEntity;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("ems_pcf_manufacturing")
public class EmspcfManufacturing extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    private Long productId;

    @Excel(name = "年份")
    private Integer year;

    @Excel(name = "排放源/活动设施")
    private String emissionSource;

    @Excel(name = "活动数据种类")
    private String activityType;

    @Excel(name = "排放数量")
    private BigDecimal activityValue;

    @Excel(name = "单位")
    private String unit;

    @Excel(name = "统计开始")
    private String statPeriodStart;

    @Excel(name = "统计结束")
    private String statPeriodEnd;

    @Excel(name = "产品产量")
    private BigDecimal productionVolume;

    @Excel(name = "产量单位")
    private String productionUnit;

    @Excel(name = "碳迹值")
    private BigDecimal carbonValue;

    @Excel(name = "碳排放单位")
    private String carbonUnit;
}
