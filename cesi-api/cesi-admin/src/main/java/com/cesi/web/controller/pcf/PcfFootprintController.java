package com.cesi.web.controller.pcf;

import lombok.RequiredArgsConstructor;
import com.cesi.common.annotation.Log;
import com.cesi.common.core.controller.BaseController;
import com.cesi.common.core.domain.AjaxResult;
import com.cesi.common.enums.BusinessType;
import com.cesi.pcf.domain.*;
import com.cesi.pcf.service.IPcfFootprintService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@Api(tags = "产品碳足迹-核算数据")
@RequestMapping("/pcf/footprint")
public class PcfFootprintController extends BaseController {

    private final IPcfFootprintService footprintService;

    @ApiOperation("生命周期总览")
    @PreAuthorize("@ss.hasPermi('pcf:footprint:list')")
    @GetMapping("/lifecycle")
    public AjaxResult getLifecycle(@RequestParam Long productId, @RequestParam Integer year) {
        return AjaxResult.success(footprintService.getLifecycleOverview(productId, year));
    }

    /* ─── 原材料 ─── */

    @GetMapping("/raw/list")
    @PreAuthorize("@ss.hasPermi('pcf:footprint:list')")
    public AjaxResult listRaw(@RequestParam Long productId, @RequestParam Integer year) {
        return AjaxResult.success(footprintService.listRawMaterial(productId, year));
    }

    @PostMapping("/raw/save")
    @PreAuthorize("@ss.hasPermi('pcf:raw:add')")
    @Log(title = "原材料数据", businessType = BusinessType.INSERT)
    public AjaxResult saveRaw(@RequestBody EmspcfRawMaterial entity) {
        footprintService.saveRawMaterial(entity);
        return AjaxResult.success();
    }

    @DeleteMapping("/raw/{id}")
    @PreAuthorize("@ss.hasPermi('pcf:raw:remove')")
    @Log(title = "原材料数据", businessType = BusinessType.DELETE)
    public AjaxResult removeRaw(@PathVariable Long id) {
        footprintService.removeRawMaterial(id);
        return AjaxResult.success();
    }

    /* ─── 生产制造 ─── */

    @GetMapping("/mfg/list")
    @PreAuthorize("@ss.hasPermi('pcf:footprint:list')")
    public AjaxResult listMfg(@RequestParam Long productId, @RequestParam Integer year) {
        return AjaxResult.success(footprintService.listManufacturing(productId, year));
    }

    @PostMapping("/mfg/save")
    @PreAuthorize("@ss.hasPermi('pcf:mfg:add')")
    @Log(title = "生产制造数据", businessType = BusinessType.INSERT)
    public AjaxResult saveMfg(@RequestBody EmspcfManufacturing entity) {
        footprintService.saveManufacturing(entity);
        return AjaxResult.success();
    }

    @DeleteMapping("/mfg/{id}")
    @PreAuthorize("@ss.hasPermi('pcf:mfg:remove')")
    @Log(title = "生产制造数据", businessType = BusinessType.DELETE)
    public AjaxResult removeMfg(@PathVariable Long id) {
        footprintService.removeManufacturing(id);
        return AjaxResult.success();
    }

    /* ─── 物流运输 ─── */

    @GetMapping("/logistics/list")
    @PreAuthorize("@ss.hasPermi('pcf:footprint:list')")
    public AjaxResult listLogistics(@RequestParam Long productId, @RequestParam Integer year) {
        return AjaxResult.success(footprintService.listLogistics(productId, year));
    }

    @PostMapping("/logistics/save")
    @PreAuthorize("@ss.hasPermi('pcf:log:add')")
    @Log(title = "物流运输数据", businessType = BusinessType.INSERT)
    public AjaxResult saveLogistics(@RequestBody EmspcfLogistics entity) {
        footprintService.saveLogistics(entity);
        return AjaxResult.success();
    }

    @DeleteMapping("/logistics/{id}")
    @PreAuthorize("@ss.hasPermi('pcf:log:remove')")
    @Log(title = "物流运输数据", businessType = BusinessType.DELETE)
    public AjaxResult removeLogistics(@PathVariable Long id) {
        footprintService.removeLogistics(id);
        return AjaxResult.success();
    }

    /* ─── 产品使用 ─── */

    @GetMapping("/use/list")
    @PreAuthorize("@ss.hasPermi('pcf:footprint:list')")
    public AjaxResult listUse(@RequestParam Long productId, @RequestParam Integer year) {
        return AjaxResult.success(footprintService.listProductUse(productId, year));
    }

    @PostMapping("/use/save")
    @PreAuthorize("@ss.hasPermi('pcf:footprint:list')")
    @Log(title = "产品使用数据", businessType = BusinessType.INSERT)
    public AjaxResult saveUse(@RequestBody EmspcfProductUse entity) {
        footprintService.saveProductUse(entity);
        return AjaxResult.success();
    }

    @DeleteMapping("/use/{id}")
    @PreAuthorize("@ss.hasPermi('pcf:footprint:list')")
    @Log(title = "产品使用数据", businessType = BusinessType.DELETE)
    public AjaxResult removeUse(@PathVariable Long id) {
        footprintService.removeProductUse(id);
        return AjaxResult.success();
    }

    /* ─── 废弃回收 ─── */

    @GetMapping("/eol/list")
    @PreAuthorize("@ss.hasPermi('pcf:footprint:list')")
    public AjaxResult listEol(@RequestParam Long productId, @RequestParam Integer year) {
        return AjaxResult.success(footprintService.listEol(productId, year));
    }

    @PostMapping("/eol/save")
    @PreAuthorize("@ss.hasPermi('pcf:footprint:list')")
    @Log(title = "废弃回收数据", businessType = BusinessType.INSERT)
    public AjaxResult saveEol(@RequestBody EmspcfEol entity) {
        footprintService.saveEol(entity);
        return AjaxResult.success();
    }

    @DeleteMapping("/eol/{id}")
    @PreAuthorize("@ss.hasPermi('pcf:footprint:list')")
    @Log(title = "废弃回收数据", businessType = BusinessType.DELETE)
    public AjaxResult removeEol(@PathVariable Long id) {
        footprintService.removeEol(id);
        return AjaxResult.success();
    }

    /* ─── 绿电绿证 ─── */

    @GetMapping("/cert/list")
    @PreAuthorize("@ss.hasPermi('pcf:footprint:list')")
    public AjaxResult listCert(@RequestParam Long productId, @RequestParam Integer year) {
        return AjaxResult.success(footprintService.listGreenCert(productId, year));
    }

    @PostMapping("/cert/save")
    @PreAuthorize("@ss.hasPermi('pcf:cert:add')")
    @Log(title = "绿电绿证", businessType = BusinessType.INSERT)
    public AjaxResult saveCert(@RequestBody EmspcfGreenCert entity) {
        footprintService.saveGreenCert(entity);
        return AjaxResult.success();
    }

    @DeleteMapping("/cert/{id}")
    @PreAuthorize("@ss.hasPermi('pcf:cert:remove')")
    @Log(title = "绿电绿证", businessType = BusinessType.DELETE)
    public AjaxResult removeCert(@PathVariable Long id) {
        footprintService.removeGreenCert(id);
        return AjaxResult.success();
    }
}
