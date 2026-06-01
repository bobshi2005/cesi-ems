package com.cesi.pcf.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.cesi.common.annotation.Excel;
import com.cesi.common.core.domain.BaseEntity;
import lombok.Data;

@Data
@TableName("ems_pcf_product")
public class EmspcfProduct extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    @Excel(name = "分类ID")
    private Long categoryId;

    @Excel(name = "产品名称")
    private String productName;

    @Excel(name = "规格型号")
    private String specModel;

    @Excel(name = "计量单位")
    private String unit;

    @Excel(name = "生命周期边界")
    private String lifecycleBoundary;

    @Excel(name = "核算标准")
    private String accountingStandard;

    @Excel(name = "功能单位描述")
    private String functionalUnit;

    @Excel(name = "默认核算年份")
    private Integer defaultYear;

    @Excel(name = "状态")
    private String status;

    /** 分类名称（非DB字段） */
    @TableField(exist = false)
    private String categoryName;
}
