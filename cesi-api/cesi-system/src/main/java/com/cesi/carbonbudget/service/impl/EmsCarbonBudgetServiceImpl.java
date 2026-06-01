package com.cesi.carbonbudget.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cesi.carbonbudget.domain.EmsCarbonBudget;
import com.cesi.carbonbudget.mapper.EmsCarbonBudgetMapper;
import com.cesi.carbonbudget.service.IEmsCarbonBudgetService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;

@Service
@AllArgsConstructor
public class EmsCarbonBudgetServiceImpl
        extends ServiceImpl<EmsCarbonBudgetMapper, EmsCarbonBudget>
        implements IEmsCarbonBudgetService {

    @Override
    public Page<EmsCarbonBudget> listPage(String budgetTimeType, Integer budgetYear, Integer budgetMonth,
                                          String budgetType, String boundaryName,
                                          Long pageNum, Long pageSize) {
        return baseMapper.pageByCondition(new Page<>(pageNum, pageSize),
                budgetTimeType, budgetYear, budgetMonth, budgetType, boundaryName);
    }

    @Override
    public boolean add(EmsCarbonBudget entity) {
        calcBudgetEmission(entity);
        return save(entity);
    }

    @Override
    public boolean edit(EmsCarbonBudget entity) {
        calcBudgetEmission(entity);
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

    private void calcBudgetEmission(EmsCarbonBudget entity) {
        if ("TOTAL_CONTROL".equals(entity.getBudgetType())) {
            // budgetEmission is set directly by the user — no auto-calculation
            return;
        }
        BigDecimal intensity = entity.getEmissionIntensity();
        BigDecimal planValue = entity.getPlanValue();
        if (intensity != null && planValue != null) {
            entity.setBudgetEmission(intensity.multiply(planValue));
        }
    }
}
