package com.cesi.basicdata.domain.vo;

import com.cesi.common.annotation.Excel;
import lombok.Data;


/**
 * 能源类型信息类
 *
 */
@Data
public class EnergyTypeModel {

    /**
     * 能源名称
     */
    @Excel(name = "能源名称")
    private String enername;

    /**
     * 能源编号
     */
    @Excel(name = "能源编号")
    private String enersno;

}