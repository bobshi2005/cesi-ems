package com.cesi.web.controller.pcf;

import lombok.RequiredArgsConstructor;
import com.cesi.common.annotation.Log;
import com.cesi.common.core.controller.BaseController;
import com.cesi.common.core.domain.AjaxResult;
import com.cesi.common.enums.BusinessType;
import com.cesi.pcf.domain.EmspcfAccountingParams;
import com.cesi.pcf.domain.EmspcfEmissionFactor;
import com.cesi.pcf.domain.EmspcfGreenCertFactor;
import com.cesi.pcf.service.IPcfConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@Api(tags = "产品碳足迹-配置管理")
@RequestMapping("/pcf/config")
public class PcfConfigController extends BaseController {

    private final IPcfConfigService configService;

    /* ─── 排放因子库 ─── */

    @ApiOperation("排放因子列表")
    @PreAuthorize("@ss.hasPermi('pcf:config:list')")
    @GetMapping("/factor/list")
    public AjaxResult listFactors(@RequestParam(required = false) String factorCategory) {
        return AjaxResult.success(configService.listEmissionFactors(factorCategory));
    }

    @ApiOperation("保存排放因子")
    @PreAuthorize("@ss.hasPermi('pcf:factor:add')")
    @Log(title = "排放因子", businessType = BusinessType.INSERT)
    @PostMapping("/factor/save")
    public AjaxResult saveFactor(@RequestBody EmspcfEmissionFactor entity) {
        configService.saveEmissionFactor(entity);
        return AjaxResult.success();
    }

    @ApiOperation("删除排放因子")
    @PreAuthorize("@ss.hasPermi('pcf:factor:remove')")
    @Log(title = "排放因子", businessType = BusinessType.DELETE)
    @DeleteMapping("/factor/{id}")
    public AjaxResult removeFactor(@PathVariable Long id) {
        configService.removeEmissionFactor(id);
        return AjaxResult.success();
    }

    /* ─── 核算参数 ─── */

    @ApiOperation("获取核算参数")
    @PreAuthorize("@ss.hasPermi('pcf:config:list')")
    @GetMapping("/params")
    public AjaxResult getParams() {
        return AjaxResult.success(configService.getAccountingParams());
    }

    @ApiOperation("保存核算参数")
    @PreAuthorize("@ss.hasPermi('pcf:config:list')")
    @Log(title = "核算参数", businessType = BusinessType.UPDATE)
    @PostMapping("/params/save")
    public AjaxResult saveParams(@RequestBody List<EmspcfAccountingParams> params) {
        configService.saveAccountingParams(params);
        return AjaxResult.success();
    }

    /* ─── 绿证减排系数 ─── */

    @ApiOperation("绿证减排系数列表")
    @PreAuthorize("@ss.hasPermi('pcf:config:list')")
    @GetMapping("/greenFactor/list")
    public AjaxResult listGreenFactors() {
        return AjaxResult.success(configService.listGreenCertFactors());
    }

    @ApiOperation("保存绿证减排系数")
    @PreAuthorize("@ss.hasPermi('pcf:config:list')")
    @Log(title = "绿证减排系数", businessType = BusinessType.INSERT)
    @PostMapping("/greenFactor/save")
    public AjaxResult saveGreenFactor(@RequestBody EmspcfGreenCertFactor entity) {
        configService.saveGreenCertFactor(entity);
        return AjaxResult.success();
    }

    @ApiOperation("删除绿证减排系数")
    @PreAuthorize("@ss.hasPermi('pcf:config:list')")
    @Log(title = "绿证减排系数", businessType = BusinessType.DELETE)
    @DeleteMapping("/greenFactor/{id}")
    public AjaxResult removeGreenFactor(@PathVariable Long id) {
        configService.removeGreenCertFactor(id);
        return AjaxResult.success();
    }
}
