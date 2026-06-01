package com.cesi.pcf.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cesi.common.core.domain.BaseEntity;
import lombok.Data;

@Data
@TableName("ems_pcf_accounting_params")
public class EmspcfAccountingParams extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    private String paramKey;
    private String paramValue;
    private String paramType;
    private String paramLabel;
}
