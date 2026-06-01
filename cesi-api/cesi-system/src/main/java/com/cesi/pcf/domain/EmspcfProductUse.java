package com.cesi.pcf.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cesi.common.annotation.Excel;
import com.cesi.common.core.domain.BaseEntity;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("ems_pcf_product_use")
public class EmspcfProductUse extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    private Long productId;

    @Excel(name = "年份")
    private Integer year;

    @Excel(name = "能耗/材料名称")
    private String materialName;

    @Excel(name = "规格型号")
    private String specModel;

    @Excel(name = "使用年限(年)")
    private Integer usefulLife;

    @Excel(name = "年消耗量")
    private BigDecimal annualConsume;

    @Excel(name = "单位")
    private String unit;

    @Excel(name = "单位排放因数")
    private BigDecimal emissionFactor;

    @Excel(name = "因数单位")
    private String factorUnit;

    @Excel(name = "本项碳足迹")
    private BigDecimal carbonFootprint;

    @Excel(name = "碳足迹单位")
    private String carbonUnit;
}
