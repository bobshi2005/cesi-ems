package com.cesi.budget.mapper;

import com.cesi.budget.domain.vo.MonthlyActualVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface BudgetAnalysisMapper {

    /** 查询某节点全年实际能耗（汇总所有月份 MONTH 记录） */
    BigDecimal getYearActualByNode(@Param("nodeId") String nodeId,
                                   @Param("year")   int year);

    /** 查询某节点某月实际能耗 */
    BigDecimal getMonthActualByNode(@Param("nodeId") String nodeId,
                                    @Param("year")   int year,
                                    @Param("month")  int month);

    /** 查询某节点全年各月实际能耗明细（用于趋势图） */
    List<MonthlyActualVO> getMonthlyActualsByNode(@Param("nodeId") String nodeId,
                                                   @Param("year")   int year);
}
