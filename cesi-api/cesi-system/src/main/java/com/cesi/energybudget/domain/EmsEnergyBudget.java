package com.cesi.energybudget.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cesi.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ems_energy_budget")
@ApiModel("用能预算")
public class EmsEnergyBudget extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("预算名称")
    private String budgetName;

    @ApiModelProperty("预算类型：YEAR/MONTH/CUSTOM")
    private String budgetType;

    @ApiModelProperty("预算年份")
    private Integer budgetYear;

    @ApiModelProperty("预算月份，月类型时有值")
    private Integer budgetMonth;

    @ApiModelProperty("自定义起始日期")
    private LocalDate budgetStart;

    @ApiModelProperty("自定义结束日期")
    private LocalDate budgetEnd;

    @ApiModelProperty("能源类型")
    private String energyType;

    @ApiModelProperty("能源单位")
    private String energyUnit;

    @ApiModelProperty("地区类型")
    private String regionType;

    @ApiModelProperty("对应地区名称")
    private String regionName;

    @ApiModelProperty("产品单耗")
    private BigDecimal unitConsumption;

    @ApiModelProperty("单耗来源")
    private String unitConsumptionSource;

    @ApiModelProperty("计划产量")
    private BigDecimal planQuantity;

    @ApiModelProperty("产量单位")
    private String planQuantityUnit;

    @ApiModelProperty("预算能耗（自动计算）")
    private BigDecimal budgetEnergy;

    @ApiModelProperty("预警阈值百分比")
    private Integer warningThreshold;

    @ApiModelProperty("状态：0正常 1停用")
    private String status;

    @ApiModelProperty("关联模型节点ID，用于查询实际能耗")
    private String nodeId;
}
