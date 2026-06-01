package com.cesi.web.controller.supplychaincarbon;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cesi.common.annotation.Log;
import com.cesi.common.core.controller.BaseController;
import com.cesi.common.core.domain.AjaxResult;
import com.cesi.common.core.page.TableDataInfo;
import com.cesi.common.enums.BusinessType;
import com.cesi.supplychaincarbon.domain.SupplyChainCarbon;
import com.cesi.supplychaincarbon.service.ISupplyChainCarbonService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@Api(tags = "供应链碳管理")
@RequestMapping("/supplyChainCarbon")
public class SupplyChainCarbonController extends BaseController {

    private final ISupplyChainCarbonService supplyChainCarbonService;

    @ApiOperation("分页查询供应商列表")
    @PreAuthorize("@ss.hasPermi('supplyChainCarbon:supplier:list')")
    @GetMapping("/page")
    public TableDataInfo page(@RequestParam(required = false) String supplierName,
                              @RequestParam(required = false) String materialCategory,
                              @RequestParam(defaultValue = "1")  Long pageNum,
                              @RequestParam(defaultValue = "10") Long pageSize) {
        Page<SupplyChainCarbon> page = supplyChainCarbonService.listPage(supplierName, materialCategory, pageNum, pageSize);
        return getDataTable(page);
    }

    @ApiOperation("新增供应商")
    @PreAuthorize("@ss.hasPermi('supplyChainCarbon:supplier:add')")
    @Log(title = "供应链碳管理", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public AjaxResult add(@RequestBody SupplyChainCarbon entity) {
        return toAjax(supplyChainCarbonService.add(entity));
    }

    @ApiOperation("编辑供应商")
    @PreAuthorize("@ss.hasPermi('supplyChainCarbon:supplier:edit')")
    @Log(title = "供应链碳管理", businessType = BusinessType.UPDATE)
    @PutMapping("/edit")
    public AjaxResult edit(@RequestBody SupplyChainCarbon entity) {
        return toAjax(supplyChainCarbonService.edit(entity));
    }

    @ApiOperation("删除供应商")
    @PreAuthorize("@ss.hasPermi('supplyChainCarbon:supplier:delete')")
    @Log(title = "供应链碳管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public AjaxResult delete(@PathVariable Long id) {
        return toAjax(supplyChainCarbonService.deleteById(id));
    }
}
