package com.cesi.carbonverification.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cesi.common.annotation.Excel;
import com.cesi.common.core.domain.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("ems_carbon_verification_report")
public class EmsCarVerfReport extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    @Excel(name = "报告名称")
    private String reportName;

    @Excel(name = "核查年度")
    private Integer reportYear;

    @Excel(name = "组织单元")
    private String orgUnit;

    private String standard;
    private String templateType;

    @Excel(name = "总排放量(tCO2e)")
    private BigDecimal totalEmission;

    @Excel(name = "数据完整度(%)")
    private BigDecimal dataCompleteness;

    private String reportVersion;
    private String filePath;
    /** 包含章节，逗号分隔：basicInfo,boundary,activityData,factor,result,uncertainty,voucherList */
    private String sections;

    @Excel(name = "状态")
    private String status;

    private Date generateTime;
}
