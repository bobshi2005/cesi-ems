package com.cesi.carbonbudget.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cesi.carbonbudget.domain.EmsCarbonBudget;
import org.apache.ibatis.annotations.Param;

public interface EmsCarbonBudgetMapper extends BaseMapper<EmsCarbonBudget> {

    Page<EmsCarbonBudget> pageByCondition(
        Page<EmsCarbonBudget> page,
        @Param("budgetTimeType") String budgetTimeType,
        @Param("budgetYear")     Integer budgetYear,
        @Param("budgetMonth")    Integer budgetMonth,
        @Param("budgetType")     String budgetType,
        @Param("boundaryName")   String boundaryName
    );
}
