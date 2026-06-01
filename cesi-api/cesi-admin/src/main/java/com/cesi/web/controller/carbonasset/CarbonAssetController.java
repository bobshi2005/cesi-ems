package com.cesi.web.controller.carbonasset;

import com.cesi.carbonasset.domain.vo.CarbonAssetPageVO;
import com.cesi.carbonasset.domain.vo.CarbonAssetSaveDTO;
import com.cesi.carbonasset.service.ICarbonAssetService;
import com.cesi.common.annotation.Log;
import com.cesi.common.core.controller.BaseController;
import com.cesi.common.core.domain.AjaxResult;
import com.cesi.common.enums.BusinessType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@Api(tags = "碳资产管理")
@RequestMapping("/carbonAsset")
public class CarbonAssetController extends BaseController {

    private final ICarbonAssetService carbonAssetService;

    @ApiOperation("查询年度碳资产页面数据")
    @PreAuthorize("@ss.hasPermi('carbonAsset:asset:query')")
    @GetMapping("/data")
    public AjaxResult getData(@RequestParam Integer year) {
        CarbonAssetPageVO vo = carbonAssetService.getPageData(year);
        return AjaxResult.success(vo);
    }

    @ApiOperation("保存碳资产数据")
    @PreAuthorize("@ss.hasPermi('carbonAsset:asset:save')")
    @Log(title = "碳资产管理", businessType = BusinessType.UPDATE)
    @PostMapping("/save")
    public AjaxResult saveData(@RequestBody CarbonAssetSaveDTO dto) {
        carbonAssetService.saveData(dto);
        return AjaxResult.success("保存成功");
    }
}
