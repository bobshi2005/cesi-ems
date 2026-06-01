package com.cesi.budget.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cesi.budget.domain.vo.BudgetAnalysisVO;
import com.cesi.budget.domain.vo.MonthlyActualVO;
import com.cesi.budget.mapper.BudgetAnalysisMapper;
import com.cesi.budget.service.IBudgetAnalysisService;
import com.cesi.carbonbudget.domain.EmsCarbonBudget;
import com.cesi.carbonbudget.mapper.EmsCarbonBudgetMapper;
import com.cesi.energybudget.domain.EmsEnergyBudget;
import com.cesi.energybudget.mapper.EmsEnergyBudgetMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class BudgetAnalysisServiceImpl implements IBudgetAnalysisService {

    private final EmsEnergyBudgetMapper energyMapper;
    private final EmsCarbonBudgetMapper carbonMapper;
    private final BudgetAnalysisMapper  analysisMapper;

    @Override
    public BudgetAnalysisVO analyze(String dimension, String period,
                                     Integer year, Integer month,
                                     String startDate, String endDate,
                                     String scope) {
        return "energy".equals(dimension)
                ? analyzeEnergy(period, year, month, scope)
                : analyzeCarbon(period, year, month, scope);
    }

    // ─── 用能预算执行分析 ──────────────────────────────────────────────────────
    private BudgetAnalysisVO analyzeEnergy(String period, Integer year, Integer month, String scope) {
        List<EmsEnergyBudget> records = energyMapper.selectList(
                new LambdaQueryWrapper<EmsEnergyBudget>()
                        .eq(year   != null, EmsEnergyBudget::getBudgetYear,   year)
                        .eq(month  != null, EmsEnergyBudget::getBudgetMonth,  month)
                        .like(StringUtils.hasText(scope), EmsEnergyBudget::getRegionName, scope));

        BudgetAnalysisVO vo = new BudgetAnalysisVO();
        vo.setUnit("KWh");

        BigDecimal budgetGrand  = BigDecimal.ZERO;
        BigDecimal actualGrand  = BigDecimal.ZERO;
        boolean    hasAnyActual = false;
        int        overCount    = 0;

        // 月度实际累计 map（nodeId维度，趋势图用）
        Map<Integer, BigDecimal> monthlyActualMap = new HashMap<>();

        List<BudgetAnalysisVO.UnitItem> units = new ArrayList<>();

        for (EmsEnergyBudget r : records) {
            budgetGrand = budgetGrand.add(nvl(r.getBudgetEnergy()));

            BudgetAnalysisVO.UnitItem item = new BudgetAnalysisVO.UnitItem();
            item.setName(r.getRegionName());
            item.setBudget(r.getBudgetEnergy());

            if (StringUtils.hasText(r.getNodeId())) {
                BigDecimal actual = queryActualEnergy(r.getNodeId(), period, year, month);
                item.setActual(actual);
                hasAnyActual = true;
                actualGrand  = actualGrand.add(actual);

                // 执行率 & 超标
                fillRateAndStatus(item, r.getBudgetEnergy(), actual, r.getWarningThreshold());
                if (Boolean.TRUE.equals(item.getOver())) overCount++;

                // 月度趋势累加（仅年视图）
                if ("year".equals(period) && year != null) {
                    List<MonthlyActualVO> ml = analysisMapper.getMonthlyActualsByNode(r.getNodeId(), year);
                    for (MonthlyActualVO m : ml) {
                        monthlyActualMap.merge(m.getMonth(),
                                nvl(m.getActualValue()), BigDecimal::add);
                    }
                }
            } else {
                item.setActual(null);
                item.setRate(null);
                item.setOver(false);
                item.setStatusText("待核算");
            }
            units.add(item);
        }

        vo.setBudgetTotal(budgetGrand);
        vo.setUnits(units);
        vo.setOverCount(overCount);

        if (hasAnyActual) {
            vo.setActualTotal(actualGrand);
            vo.setExecutionRate(safeRate(actualGrand, budgetGrand));
        }

        vo.setTrend(buildEnergyTrend(period, budgetGrand, monthlyActualMap));
        return vo;
    }

    // ─── 碳排放预算执行分析 ────────────────────────────────────────────────────
    private BudgetAnalysisVO analyzeCarbon(String period, Integer year, Integer month, String scope) {
        List<EmsCarbonBudget> records = carbonMapper.selectList(
                new LambdaQueryWrapper<EmsCarbonBudget>()
                        .eq(year   != null, EmsCarbonBudget::getBudgetYear,   year)
                        .eq(month  != null, EmsCarbonBudget::getBudgetMonth,  month)
                        .like(StringUtils.hasText(scope), EmsCarbonBudget::getBoundaryName, scope));

        BudgetAnalysisVO vo = new BudgetAnalysisVO();
        vo.setUnit("tCO₂e");

        BigDecimal budgetGrand = BigDecimal.ZERO;
        int        overCount   = 0;

        List<BudgetAnalysisVO.UnitItem> units = new ArrayList<>();

        for (EmsCarbonBudget r : records) {
            budgetGrand = budgetGrand.add(nvl(r.getBudgetEmission()));

            BudgetAnalysisVO.UnitItem item = new BudgetAnalysisVO.UnitItem();
            item.setName(r.getBoundaryName());
            item.setBudget(r.getBudgetEmission());
            // 碳排放实际数据待接入独立核算模块，暂返回 null
            item.setActual(null);
            item.setRate(null);
            item.setOver(false);
            item.setStatusText("待核算");
            units.add(item);
        }

        vo.setBudgetTotal(budgetGrand);
        vo.setActualTotal(null);
        vo.setExecutionRate(null);
        vo.setOverCount(overCount);
        vo.setUnits(units);
        vo.setTrend(buildEmptyTrend(period));
        return vo;
    }

    // ─── 工具方法 ─────────────────────────────────────────────────────────────

    /** 根据周期类型查询某节点的实际能耗 */
    private BigDecimal queryActualEnergy(String nodeId, String period,
                                          Integer year, Integer month) {
        if ("year".equals(period) && year != null) {
            return nvl(analysisMapper.getYearActualByNode(nodeId, year));
        }
        if ("month".equals(period) && year != null && month != null) {
            return nvl(analysisMapper.getMonthActualByNode(nodeId, year, month));
        }
        return BigDecimal.ZERO;
    }

    /** 计算执行率并填充状态 */
    private void fillRateAndStatus(BudgetAnalysisVO.UnitItem item,
                                    BigDecimal budget, BigDecimal actual,
                                    Integer warningThreshold) {
        if (budget == null || budget.compareTo(BigDecimal.ZERO) == 0) {
            item.setRate(0);
            item.setOver(false);
            item.setStatusText("执行中");
            return;
        }

        BigDecimal rate = actual.multiply(BigDecimal.valueOf(100))
                .divide(budget, 1, RoundingMode.HALF_UP);
        item.setRate(rate.intValue());

        boolean over = actual.compareTo(budget) > 0;
        item.setOver(over);

        BigDecimal deviation     = actual.subtract(budget);
        BigDecimal deviationRate = deviation.multiply(BigDecimal.valueOf(100))
                .divide(budget, 1, RoundingMode.HALF_UP);
        item.setDeviation(deviation.toPlainString());
        item.setDeviationRate((deviationRate.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "")
                + deviationRate.toPlainString() + "%");

        int threshold = warningThreshold != null ? warningThreshold : 90;
        if (over) {
            item.setStatusText("已超标");
        } else if (rate.intValue() >= threshold) {
            item.setStatusText("接近上限");
        } else if (actual.compareTo(BigDecimal.ZERO) == 0) {
            item.setStatusText("待核算");
        } else {
            item.setStatusText("执行中");
        }
    }

    /** 构建用能预算趋势（年视图：12 个月；月视图：单月汇总；自定义：空） */
    private BudgetAnalysisVO.TrendData buildEnergyTrend(String period,
                                                          BigDecimal budgetTotal,
                                                          Map<Integer, BigDecimal> monthlyActualMap) {
        BudgetAnalysisVO.TrendData td = new BudgetAnalysisVO.TrendData();
        List<String>     labels = new ArrayList<>();
        List<BigDecimal> budget = new ArrayList<>();
        List<BigDecimal> actual = new ArrayList<>();

        if ("year".equals(period)) {
            // 月度预算：年总量均分 12 个月
            BigDecimal monthlyBudget = budgetTotal.compareTo(BigDecimal.ZERO) > 0
                    ? budgetTotal.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            for (int m = 1; m <= 12; m++) {
                labels.add(m + "月");
                budget.add(monthlyBudget);
                // 有数据则返回实际值，否则 null（前端不渲染该月柱）
                actual.add(monthlyActualMap.getOrDefault(m, null));
            }
        }
        // month / custom 视图暂不提供趋势分解

        td.setLabels(labels);
        td.setBudget(budget);
        td.setActual(actual);
        return td;
    }

    /** 空趋势（碳排放暂未接入实际数据） */
    private BudgetAnalysisVO.TrendData buildEmptyTrend(String period) {
        BudgetAnalysisVO.TrendData td = new BudgetAnalysisVO.TrendData();
        List<String> labels = new ArrayList<>();
        if ("year".equals(period)) {
            for (int m = 1; m <= 12; m++) labels.add(m + "月");
        } else if ("month".equals(period)) {
            labels.add("第1周"); labels.add("第2周"); labels.add("第3周"); labels.add("第4周");
        }
        td.setLabels(labels);
        td.setBudget(new ArrayList<>());
        td.setActual(new ArrayList<>());
        return td;
    }

    private BigDecimal nvl(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private BigDecimal safeRate(BigDecimal actual, BigDecimal budget) {
        if (budget == null || budget.compareTo(BigDecimal.ZERO) == 0) return null;
        return actual.multiply(BigDecimal.valueOf(100))
                .divide(budget, 1, RoundingMode.HALF_UP);
    }
}
