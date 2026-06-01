package com.cesi.supplychaincarbon.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cesi.common.annotation.Excel;
import com.cesi.common.core.domain.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("supply_chain_carbon")
public class SupplyChainCarbon extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    @Excel(name = "供应商名称")
    private String supplierName;

    @Excel(name = "物料分类")
    private String materialCategory;

    @Excel(name = "供应物料")
    private String supplyMaterial;

    @Excel(name = "规格型号")
    private String specModel;

    @Excel(name = "系统边界")
    private String systemBoundary;

    @Excel(name = "物料总量")
    private BigDecimal materialTotal;

    @Excel(name = "物料单位")
    private String materialUnit;

    @Excel(name = "碳足迹总量")
    private BigDecimal carbonFootprint;

    @Excel(name = "碳足迹单位")
    private String carbonFootprintUnit;

    @Excel(name = "联系人")
    private String contactPerson;

    @Excel(name = "联系方式")
    private String contactWay;

    @Excel(name = "供应商地址")
    private String supplierAddress;
}
