package com.cesi.carbonasset.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cesi.common.annotation.Excel;
import com.cesi.common.core.domain.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("carbon_asset")
public class CarbonAsset extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    @Excel(name = "年度")
    private Integer year;

    @Excel(name = "资产大类")
    private String assetType;

    @Excel(name = "细分类型")
    private String subType;

    @Excel(name = "类型名称")
    private String typeName;

    @Excel(name = "数量")
    private BigDecimal quantity;

    @Excel(name = "单位")
    private String unit;
}
