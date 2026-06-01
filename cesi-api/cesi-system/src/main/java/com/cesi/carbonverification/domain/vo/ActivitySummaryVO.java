package com.cesi.carbonverification.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class ActivitySummaryVO {

    private BigDecimal totalEmission;
    private Integer sourceCount;
    private Integer directCount;
    private Integer indirectCount;
    private Integer voucherUploadedMonths;
    private BigDecimal dataCompleteness;
    private String missingWarning;

    /** 月度凭证进度：key=排放源名称, value=12元素列表（0=未到期,1=已上传,2=缺失） */
    private Map<String, List<Integer>> monthlyProgress;
}
