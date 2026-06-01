package com.cesi.saving.domain.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.cesi.saving.domain.entity.EnergySavingProgram;
import lombok.Data;

/**
 * 节能项目管理 VO
 * @author cesi
 */
@Data
public class EnergySavingProgramVO extends EnergySavingProgram {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
}