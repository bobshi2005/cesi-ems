package com.cesi.carbonverification.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cesi.common.annotation.Excel;
import com.cesi.common.core.domain.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("ems_carbon_verification_package")
public class EmsCarVerfPackage extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    @Excel(name = "包名称")
    private String packageName;

    @Excel(name = "核查年度")
    private Integer packageYear;

    @Excel(name = "核查机构")
    private String verifyOrg;

    private String includeReport;
    private String includeActivity;
    private String includeVoucher;
    private String includeFactor;

    @Excel(name = "文件数量")
    private Integer fileCount;

    private Long fileSize;
    private String filePath;

    @Excel(name = "完整度(%)")
    private BigDecimal completenessRate;

    private String missingDesc;

    @Excel(name = "状态")
    private String status;
}
