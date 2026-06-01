package com.cesi.carbonbudget.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cesi.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ems_carbon_budget")
@ApiModel("碳排放预算")
public class EmsCarbonBudget extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("边界名称")
    private String boundaryName;

    @ApiModelProperty("预算类型：TOTAL_CONTROL / VALUE_INTENSITY / QTY_INTENSITY")
    private String budgetType;

    @ApiModelProperty("预算时间类型：YEAR / MONTH")
    private String budgetTimeType;

    @ApiModelProperty("预算年份")
    private Integer budgetYear;

    @ApiModelProperty("预算月份，月类型时有值")
    private Integer budgetMonth;

    @ApiModelProperty("监管标准")
    private String regulationStandard;

    @ApiModelProperty("排放强度")
    private BigDecimal emissionIntensity;

    @ApiModelProperty("计划产量/产值")
    private BigDecimal planValue;

    @ApiModelProperty("计划产量/产值单位")
    private String planValueUnit;

    @ApiModelProperty("预算排放量（自动计算或直接填写）")
    private BigDecimal budgetEmission;

    @ApiModelProperty("预警阈值百分比，默认90")
    private Integer warningThreshold;

    @ApiModelProperty("预警方式，默认SYSTEM")
    private String warningMethod;

    @ApiModelProperty("状态：0正常 1停用")
    private String status;

    @ApiModelProperty("关联模型节点ID，用于查询实际排放（待接入碳排放核算）")
    private String nodeId;
}
