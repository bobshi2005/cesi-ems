package com.cesi.model.domain;

import com.cesi.common.annotation.Excel;
import com.cesi.common.core.domain.BaseEntity;
import lombok.Data;

/**
 * 模型对象 model
 * 
 * @author cesi
 */
@Data
public class Model extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 模型编码 */
    @Excel(name = "模型编码")
    private String modelCode;

    /** 模型名称 */
    @Excel(name = "模型名称")
    private String modelName;

    /** 是否显示 */
    private Integer isShow;

    private String modelType;

}
