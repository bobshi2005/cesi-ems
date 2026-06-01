package com.cesi.budget.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@ApiModel("预算执行分析结果")
public class BudgetAnalysisVO {

    @ApiModelProperty("预算总量")
    private BigDecimal budgetTotal;

    @ApiModelProperty("实际总量（待集成，暂为null）")
    private BigDecimal actualTotal;

    @ApiModelProperty("执行率（%）")
    private BigDecimal executionRate;

    @ApiModelProperty("超标单元/边界数量")
    private Integer overCount;

    @ApiModelProperty("单位（KWh 或 tCO₂e）")
    private String unit;

    @ApiModelProperty("各单元进度明细")
    private List<UnitItem> units;

    @ApiModelProperty("趋势数据（按时间分组）")
    private TrendData trend;

    @Data
    public static class UnitItem {
        private String name;
        private BigDecimal budget;
        private BigDecimal actual;
        private Integer rate;
        private Boolean over;
        private String deviation;
        private String deviationRate;
        private String statusText;
    }

    @Data
    public static class TrendData {
        private List<String> labels;
        private List<BigDecimal> budget;
        private List<BigDecimal> actual;
    }
}
