package com.cesi.web.controller.budget;

import lombok.RequiredArgsConstructor;
import com.cesi.budget.domain.vo.BudgetAnalysisVO;
import com.cesi.budget.service.IBudgetAnalysisService;
import com.cesi.common.core.controller.BaseController;
import com.cesi.common.core.domain.AjaxResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@Api(tags = "预算执行分析")
@RequestMapping("/budget/analysis")
public class BudgetAnalysisController extends BaseController {

    private final IBudgetAnalysisService service;

    @ApiOperation("预算执行分析查询")
    @PreAuthorize("@ss.hasPermi('budget:analysis:list')")
    @GetMapping
    public AjaxResult analyze(
            @RequestParam(defaultValue = "energy") String dimension,
            @RequestParam(defaultValue = "year")   String period,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) String  startDate,
            @RequestParam(required = false) String  endDate,
            @RequestParam(required = false) String  scope) {
        BudgetAnalysisVO vo = service.analyze(dimension, period, year, month, startDate, endDate, scope);
        return AjaxResult.success(vo);
    }
}
