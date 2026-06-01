package com.cesi.pcf.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cesi.common.annotation.Excel;
import com.cesi.common.core.domain.BaseEntity;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("ems_pcf_emission_factor")
public class EmspcfEmissionFactor extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    @Excel(name = "材料/服务名称")
    private String factorName;

    @Excel(name = "规格/类型")
    private String specType;

    /** raw / energy / transport / waste */
    @Excel(name = "因子类别")
    private String factorCategory;

    @Excel(name = "排放因数")
    private BigDecimal emissionFactor;

    @Excel(name = "单位")
    private String factorUnit;

    @Excel(name = "数据来源")
    private String dataSource;

    @Excel(name = "参考年份")
    private Integer referenceYear;

    @Excel(name = "适用范围")
    private String applicableScope;

    @Excel(name = "是否有回收抵消")
    private String isRecycled;

    @Excel(name = "是否系统内置")
    private String isBuiltin;

    @Excel(name = "状态")
    private String status;
}
