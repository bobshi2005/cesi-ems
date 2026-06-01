package com.cesi.budget.domain.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class MonthlyActualVO {
    private Integer month;
    private BigDecimal actualValue;
}
