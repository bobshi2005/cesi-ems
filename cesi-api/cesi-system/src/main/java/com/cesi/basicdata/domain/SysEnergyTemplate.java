package com.cesi.basicdata.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.cesi.common.annotation.Excel;
import com.cesi.common.core.domain.BaseEntity;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 能源品种设置对象 sys_energy_template
 *
 * @author cesi
 */
@Data
@TableName("sys_energy_template")
public class SysEnergyTemplate extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 能源类别ID */
    private Integer id;

    /** 能源类别名称 */
    @Excel(name = "能源类别名称")
    private String name;
}
