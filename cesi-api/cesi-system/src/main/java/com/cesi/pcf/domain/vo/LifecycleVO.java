package com.cesi.pcf.domain.vo;

import com.cesi.pcf.domain.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 生命周期总览 VO
 */
@Data
public class LifecycleVO {

    private Long productId;
    private Integer year;

    /** 各阶段合计 */
    private BigDecimal rawMaterialTotal = BigDecimal.ZERO;
    private BigDecimal manufacturingTotal = BigDecimal.ZERO;
    private BigDecimal logisticsTotal = BigDecimal.ZERO;
    private BigDecimal productUseTotal = BigDecimal.ZERO;
    private BigDecimal eolTotal = BigDecimal.ZERO;

    /** 绿证抵消总量 tCO₂e */
    private BigDecimal greenCertOffset = BigDecimal.ZERO;

    /** 总碳足迹（各阶段之和） */
    private BigDecimal totalFootprint = BigDecimal.ZERO;

    /** 净碳足迹（total - offset） */
    private BigDecimal netFootprint = BigDecimal.ZERO;

    /** 产品信息 */
    private EmspcfProduct product;

    /** 各阶段数据（用于前端概览图） */
    private List<StageItem> stages;

    @Data
    public static class StageItem {
        private String stageName;
        private BigDecimal amount;
        private BigDecimal percentage;
        private String badgeClass;
    }
}
