package com.cesi.costmanagement.domain.vo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.cesi.common.annotation.Excel;
import com.cesi.common.core.domain.BaseEntity;
import com.cesi.costmanagement.domain.CostPriceRelevancy;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 单价关联对象 cost_price_relevancy
 *
 * @author Cesi
 */
@Data
public class CostPriceRelevancyVo extends CostPriceRelevancy {
    private BigDecimal price;
    private String electricityType;

}
