package com.cesi.pcf.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cesi.common.annotation.Excel;
import com.cesi.common.core.domain.BaseEntity;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("ems_pcf_logistics")
public class EmspcfLogistics extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    private Long productId;

    @Excel(name = "年份")
    private Integer year;

    @Excel(name = "材料/货物名称")
    private String materialName;

    @Excel(name = "规格型号")
    private String specModel;

    @Excel(name = "运输距离")
    private BigDecimal transportDist;

    @Excel(name = "距离单位")
    private String distUnit;

    @Excel(name = "运输方式")
    private String transportMode;

    @Excel(name = "单位排放因数")
    private BigDecimal emissionFactor;

    @Excel(name = "因数单位")
    private String factorUnit;

    @Excel(name = "本项碳足迹")
    private BigDecimal carbonFootprint;

    @Excel(name = "碳足迹单位")
    private String carbonUnit;
}
