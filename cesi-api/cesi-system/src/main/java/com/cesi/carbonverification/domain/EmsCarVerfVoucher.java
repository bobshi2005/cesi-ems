package com.cesi.carbonverification.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cesi.common.annotation.Excel;
import com.cesi.common.core.domain.BaseEntity;
import lombok.Data;

import java.util.Date;

@Data
@TableName("ems_carbon_verification_voucher")
public class EmsCarVerfVoucher extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    @Excel(name = "年度")
    private Integer year;

    @Excel(name = "月份")
    private Integer month;

    @Excel(name = "组织单元")
    private String orgUnit;

    @Excel(name = "排放源")
    private String emissionSource;

    @Excel(name = "凭证类型")
    private String voucherType;

    @Excel(name = "凭证名称")
    private String voucherName;

    private String fileName;
    private String filePath;
    private Long fileSize;

    @Excel(name = "文件格式")
    private String fileFormat;

    @Excel(name = "上传人")
    private String uploadBy;

    @Excel(name = "上传时间")
    private Date uploadTime;
}
