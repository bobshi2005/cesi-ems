package com.cesi.pcf.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cesi.common.annotation.Excel;
import com.cesi.common.core.domain.BaseEntity;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("ems_pcf_green_cert_factor")
public class EmspcfGreenCertFactor extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    @Excel(name = "绿证类型")
    private String certType;

    @Excel(name = "发电类型")
    private String powerType;

    @Excel(name = "减排系数")
    private BigDecimal emissionFactor;

    @Excel(name = "单位")
    private String factorUnit;

    @Excel(name = "参考标准")
    private String referenceStandard;

    @Excel(name = "适用电网/区域")
    private String applicableRegion;

    @Excel(name = "参考年份")
    private Integer referenceYear;

    @Excel(name = "状态")
    private String status;
}
