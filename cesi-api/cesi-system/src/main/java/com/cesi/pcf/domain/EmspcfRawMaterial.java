package com.cesi.pcf.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cesi.common.annotation.Excel;
import com.cesi.common.core.domain.BaseEntity;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("ems_pcf_raw_material")
public class EmspcfRawMaterial extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    private Long productId;

    @Excel(name = "年份")
    private Integer year;

    @Excel(name = "材料/零件/服务名称")
    private String materialName;

    @Excel(name = "规格型号")
    private String specModel;

    @Excel(name = "数量")
    private BigDecimal quantity;

    @Excel(name = "单位")
    private String unit;

    @Excel(name = "单位排放因数")
    private BigDecimal emissionFactor;

    @Excel(name = "因数单位")
    private String factorUnit;

    @Excel(name = "本项碳足迹")
    private BigDecimal emissionAmount;

    @Excel(name = "碳足迹单位")
    private String emissionUnit;

    @Excel(name = "数据来源")
    private String dataSource;
}
