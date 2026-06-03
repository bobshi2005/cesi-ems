package com.cesi.web.controller.energybudget;

import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cesi.common.annotation.Log;
import com.cesi.common.core.controller.BaseController;
import com.cesi.common.core.domain.AjaxResult;
import com.cesi.common.core.page.TableDataInfo;
import com.cesi.common.enums.BusinessType;
import com.cesi.energybudget.domain.EmsEnergyBudget;
import com.cesi.energybudget.service.IEmsEnergyBudgetService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@Api(tags = "用能预算管理")
@RequestMapping("/budget/energy")
public class EmsEnergyBudgetController extends BaseController {

    private final IEmsEnergyBudgetService service;

    @ApiOperation("分页查询用能预算列表")
    @PreAuthorize("@ss.hasPermi('budget:energy:list')")
    @GetMapping("/page")
    public TableDataInfo page(
            @RequestParam(required = false) String budgetType,
            @RequestParam(required = false) Integer budgetYear,
            @RequestParam(required = false) Integer budgetMonth,
            @RequestParam(required = false) String regionType,
            @RequestParam(required = false) String regionName,
            @RequestParam(defaultValue = "1")  Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize) {
        Page<EmsEnergyBudget> page = service.listPage(
                budgetType, budgetYear, budgetMonth, regionType, regionName, pageNum, pageSize);
        return getDataTable(page);
    }

    @ApiOperation("新增用能预算")
    @PreAuthorize("@ss.hasPermi('budget:energy:add')")
    @Log(title = "用能预算", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public AjaxResult add(@RequestBody EmsEnergyBudget entity) {
        return toAjax(service.add(entity));
    }

    @ApiOperation("编辑用能预算")
    @PreAuthorize("@ss.hasPermi('budget:energy:edit')")
    @Log(title = "用能预算", businessType = BusinessType.UPDATE)
    @PutMapping("/edit")
    public AjaxResult edit(@RequestBody EmsEnergyBudget entity) {
        return toAjax(service.edit(entity));
    }

    @ApiOperation("删除用能预算")
    @PreAuthorize("@ss.hasPermi('budget:energy:delete')")
    @Log(title = "用能预算", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public AjaxResult delete(@PathVariable Long id) {
        return toAjax(service.deleteById(id));
    }

    @ApiOperation("批量删除用能预算")
    @PreAuthorize("@ss.hasPermi('budget:energy:delete')")
    @Log(title = "用能预算", businessType = BusinessType.DELETE)
    @DeleteMapping("/batch")
    public AjaxResult deleteBatch(@RequestBody Long[] ids) {
        return toAjax(service.deleteBatchByIds(ids));
    }
}
