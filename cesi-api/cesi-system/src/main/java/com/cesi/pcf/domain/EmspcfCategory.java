package com.cesi.pcf.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.cesi.common.annotation.Excel;
import com.cesi.common.core.domain.BaseEntity;
import lombok.Data;
import java.util.List;

@Data
@TableName("ems_pcf_category")
public class EmspcfCategory extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    @Excel(name = "父级ID")
    private Long parentId;

    @Excel(name = "分类名称")
    private String categoryName;

    @Excel(name = "分类编码")
    private String categoryCode;

    @Excel(name = "排序号")
    private Integer orderNum;

    @Excel(name = "状态")
    private String status;

    /** 子分类（非DB字段） */
    @TableField(exist = false)
    private List<EmspcfCategory> children;

    /** 产品数量（非DB字段） */
    @TableField(exist = false)
    private Integer productCount;
}
