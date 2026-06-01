package com.cesi.budget.service;

import com.cesi.budget.domain.vo.BudgetAnalysisVO;

public interface IBudgetAnalysisService {
    /**
     * @param dimension  energy / carbon
     * @param period     year / month / custom
     * @param year       年份（period=year 或 month 时）
     * @param month      月份（period=month 时）
     * @param startDate  起始日期 yyyy-MM-dd（period=custom 时）
     * @param endDate    结束日期 yyyy-MM-dd（period=custom 时）
     * @param scope      核算范围/预算边界名称，null 表示全部
     */
    BudgetAnalysisVO analyze(String dimension, String period,
                              Integer year, Integer month,
                              String startDate, String endDate,
                              String scope);
}
