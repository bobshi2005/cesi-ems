package com.cesi.web.controller.carbonbudget;

import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cesi.carbonbudget.domain.EmsCarbonBudget;
import com.cesi.carbonbudget.service.IEmsCarbonBudgetService;
import com.cesi.common.annotation.Log;
import com.cesi.common.core.controller.BaseController;
import com.cesi.common.core.domain.AjaxResult;
import com.cesi.common.core.page.TableDataInfo;
import com.cesi.common.enums.BusinessType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@Api(tags = "碳排放预算管理")
@RequestMapping("/budget/carbon")
public class EmsCarbonBudgetController extends BaseController {

    private final IEmsCarbonBudgetService service;

    @ApiOperation("分页查询碳排放预算列表")
    @PreAuthorize("@ss.hasPermi('budget:carbon:list')")
    @GetMapping("/page")
    public TableDataInfo page(
            @RequestParam(required = false) String budgetTimeType,
            @RequestParam(required = false) Integer budgetYear,
            @RequestParam(required = false) Integer budgetMonth,
            @RequestParam(required = false) String budgetType,
            @RequestParam(required = false) String boundaryName,
            @RequestParam(defaultValue = "1")  Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize) {
        Page<EmsCarbonBudget> page = service.listPage(
                budgetTimeType, budgetYear, budgetMonth, budgetType, boundaryName, pageNum, pageSize);
        return getDataTable(page);
    }

    @ApiOperation("新增碳排放预算")
    @PreAuthorize("@ss.hasPermi('budget:carbon:add')")
    @Log(title = "碳排放预算", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public AjaxResult add(@RequestBody EmsCarbonBudget entity) {
        return toAjax(service.add(entity));
    }

    @ApiOperation("编辑碳排放预算")
    @PreAuthorize("@ss.hasPermi('budget:carbon:edit')")
    @Log(title = "碳排放预算", businessType = BusinessType.UPDATE)
    @PutMapping("/edit")
    public AjaxResult edit(@RequestBody EmsCarbonBudget entity) {
        return toAjax(service.edit(entity));
    }

    @ApiOperation("删除碳排放预算")
    @PreAuthorize("@ss.hasPermi('budget:carbon:delete')")
    @Log(title = "碳排放预算", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public AjaxResult delete(@PathVariable Long id) {
        return toAjax(service.deleteById(id));
    }

    @ApiOperation("批量删除碳排放预算")
    @PreAuthorize("@ss.hasPermi('budget:carbon:delete')")
    @Log(title = "碳排放预算", businessType = BusinessType.DELETE)
    @DeleteMapping("/batch")
    public AjaxResult deleteBatch(@RequestBody Long[] ids) {
        return toAjax(service.deleteBatchByIds(ids));
    }
}
