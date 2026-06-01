package com.cesi.pcf.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cesi.common.annotation.Excel;
import com.cesi.common.core.domain.BaseEntity;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("ems_pcf_green_cert")
public class EmspcfGreenCert extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    private Long productId;

    @Excel(name = "年份")
    private Integer year;

    @Excel(name = "绿证类型")
    private String certType;

    @Excel(name = "证书编号")
    private String certNo;

    @Excel(name = "发电类型")
    private String powerType;

    @Excel(name = "采购量(MWh)")
    private BigDecimal purchaseVolume;

    @Excel(name = "折算电量(KWh)")
    private BigDecimal convertedPower;

    @Excel(name = "减排系数")
    private BigDecimal emissionFactor;

    @Excel(name = "折算减排量(tCO₂e)")
    private BigDecimal emissionReduction;

    @Excel(name = "有效期开始")
    private String validityStart;

    @Excel(name = "有效期结束")
    private String validityEnd;

    @Excel(name = "状态")
    private String status;
}
