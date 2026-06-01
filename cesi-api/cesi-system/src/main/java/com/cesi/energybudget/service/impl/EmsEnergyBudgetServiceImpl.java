package com.cesi.energybudget.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cesi.energybudget.domain.EmsEnergyBudget;
import com.cesi.energybudget.mapper.EmsEnergyBudgetMapper;
import com.cesi.energybudget.service.IEmsEnergyBudgetService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;

@Service
@AllArgsConstructor
public class EmsEnergyBudgetServiceImpl
        extends ServiceImpl<EmsEnergyBudgetMapper, EmsEnergyBudget>
        implements IEmsEnergyBudgetService {

    @Override
    public Page<EmsEnergyBudget> listPage(String budgetType, Integer budgetYear, Integer budgetMonth,
                                           String regionType, String regionName,
                                           Long pageNum, Long pageSize) {
        return baseMapper.pageByCondition(new Page<>(pageNum, pageSize),
                budgetType, budgetYear, budgetMonth, regionType, regionName);
    }

    @Override
    public boolean add(EmsEnergyBudget entity) {
        calcBudgetEnergy(entity);
        return save(entity);
    }

    @Override
    public boolean edit(EmsEnergyBudget entity) {
        calcBudgetEnergy(entity);
        return updateById(entity);
    }

    @Override
    public boolean deleteById(Long id) {
        return removeById(id);
    }

    @Override
    public boolean deleteBatchByIds(Long[] ids) {
        return removeByIds(Arrays.asList(ids));
    }

    private void calcBudgetEnergy(EmsEnergyBudget entity) {
        BigDecimal uc  = entity.getUnitConsumption();
        BigDecimal qty = entity.getPlanQuantity();
        if (uc != null && qty != null) {
            entity.setBudgetEnergy(uc.multiply(qty));
        }
    }
}
