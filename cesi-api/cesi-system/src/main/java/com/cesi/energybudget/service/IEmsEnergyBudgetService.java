package com.cesi.energybudget.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cesi.energybudget.domain.EmsEnergyBudget;

public interface IEmsEnergyBudgetService {
    Page<EmsEnergyBudget> listPage(String budgetType, Integer budgetYear, Integer budgetMonth,
                                    String regionType, String regionName, Long pageNum, Long pageSize);
    boolean add(EmsEnergyBudget entity);
    boolean edit(EmsEnergyBudget entity);
    boolean deleteById(Long id);
    boolean deleteBatchByIds(Long[] ids);
}
