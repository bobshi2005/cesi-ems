package com.cesi.carbonbudget.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cesi.carbonbudget.domain.EmsCarbonBudget;

public interface IEmsCarbonBudgetService {

    Page<EmsCarbonBudget> listPage(String budgetTimeType, Integer budgetYear, Integer budgetMonth,
                                   String budgetType, String boundaryName,
                                   Long pageNum, Long pageSize);

    boolean add(EmsCarbonBudget entity);

    boolean edit(EmsCarbonBudget entity);

    boolean deleteById(Long id);

    boolean deleteBatchByIds(Long[] ids);
}
