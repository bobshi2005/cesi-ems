package com.cesi.web.controller.carbonverification;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cesi.carbonverification.domain.EmsCarVerfPackage;
import com.cesi.carbonverification.service.IEmsCarVerfPackageService;
import com.cesi.common.annotation.Log;
import com.cesi.common.core.controller.BaseController;
import com.cesi.common.core.domain.AjaxResult;
import com.cesi.common.core.page.TableDataInfo;
import com.cesi.common.enums.BusinessType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@AllArgsConstructor
@Api(tags = "碳核查-材料汇集")
@RequestMapping("/carbonVerification/package")
public class EmsCarVerfPackageController extends BaseController {

    private final IEmsCarVerfPackageService packageService;

    @ApiOperation("查询历史打包记录")
    @PreAuthorize("@ss.hasPermi('carbonVerification:package:list')")
    @GetMapping("/page")
    public TableDataInfo page(@RequestParam(required = false) Integer packageYear,
                               @RequestParam(defaultValue = "1")  Long pageNum,
                               @RequestParam(defaultValue = "10") Long pageSize) {
        Page<EmsCarVerfPackage> page = packageService.listPage(packageYear, pageNum, pageSize);
        return getDataTable(page);
    }

    @ApiOperation("材料完整度评估")
    @PreAuthorize("@ss.hasPermi('carbonVerification:package:list')")
    @GetMapping("/completeness")
    public AjaxResult completeness(@RequestParam Integer year,
                                    @RequestParam(defaultValue = "全厂") String orgUnit) {
        return AjaxResult.success(packageService.getCompleteness(year, orgUnit));
    }

    @ApiOperation("生成材料包")
    @PreAuthorize("@ss.hasPermi('carbonVerification:package:generate')")
    @Log(title = "碳核查-材料汇集", businessType = BusinessType.INSERT)
    @PostMapping("/generate")
    public AjaxResult generate(@RequestBody EmsCarVerfPackage param) {
        try {
            EmsCarVerfPackage pkg = packageService.generate(param);
            return AjaxResult.success("打包成功", pkg);
        } catch (Exception e) {
            return AjaxResult.error("打包失败：" + e.getMessage());
        }
    }

    @ApiOperation("下载材料包ZIP")
    @PreAuthorize("@ss.hasPermi('carbonVerification:package:download')")
    @GetMapping("/download/{id}")
    public void download(@PathVariable Long id, HttpServletResponse response) {
        try {
            packageService.download(id, response);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @ApiOperation("删除材料包")
    @PreAuthorize("@ss.hasPermi('carbonVerification:package:remove')")
    @Log(title = "碳核查-材料汇集", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id) {
        packageService.deleteById(id);
        return AjaxResult.success();
    }
}
