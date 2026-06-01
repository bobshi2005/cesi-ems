package com.cesi.energybudget.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cesi.energybudget.domain.EmsEnergyBudget;
import org.apache.ibatis.annotations.Param;

public interface EmsEnergyBudgetMapper extends BaseMapper<EmsEnergyBudget> {

    Page<EmsEnergyBudget> pageByCondition(
        Page<EmsEnergyBudget> page,
        @Param("budgetType") String budgetType,
        @Param("budgetYear")  Integer budgetYear,
        @Param("budgetMonth") Integer budgetMonth,
        @Param("regionType")  String regionType,
        @Param("regionName")  String regionName
    );
}
