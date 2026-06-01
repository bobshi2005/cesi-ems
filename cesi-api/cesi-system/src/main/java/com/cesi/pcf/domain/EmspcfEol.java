package com.cesi.pcf.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cesi.common.annotation.Excel;
import com.cesi.common.core.domain.BaseEntity;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("ems_pcf_eol")
public class EmspcfEol extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    private Long productId;

    @Excel(name = "年份")
    private Integer year;

    @Excel(name = "废弃物/回收材料")
    private String wasteMaterial;

    @Excel(name = "处理方式")
    private String disposalMethod;

    @Excel(name = "重量/数量")
    private BigDecimal weight;

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

    @Excel(name = "是否回收减排")
    private String isRecycled;
}
