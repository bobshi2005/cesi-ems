package com.cesi.web.controller.carbonverification;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cesi.carbonverification.domain.EmsCarVerfReport;
import com.cesi.carbonverification.service.IEmsCarVerfReportService;
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
@Api(tags = "碳核查-排放报告")
@RequestMapping("/carbonVerification/report")
public class EmsCarVerfReportController extends BaseController {

    private final IEmsCarVerfReportService reportService;

    @ApiOperation("查询历史报告列表")
    @PreAuthorize("@ss.hasPermi('carbonVerification:report:list')")
    @GetMapping("/page")
    public TableDataInfo page(@RequestParam(required = false) Integer reportYear,
                               @RequestParam(required = false) String orgUnit,
                               @RequestParam(defaultValue = "1")  Long pageNum,
                               @RequestParam(defaultValue = "10") Long pageSize) {
        Page<EmsCarVerfReport> page = reportService.listPage(reportYear, orgUnit, pageNum, pageSize);
        return getDataTable(page);
    }

    @ApiOperation("生成前预检查")
    @PreAuthorize("@ss.hasPermi('carbonVerification:report:list')")
    @GetMapping("/checkStatus")
    public AjaxResult checkStatus(@RequestParam Integer year,
                                   @RequestParam(defaultValue = "全厂") String orgUnit) {
        return AjaxResult.success(reportService.checkStatus(year, orgUnit));
    }

    @ApiOperation("生成排放报告")
    @PreAuthorize("@ss.hasPermi('carbonVerification:report:generate')")
    @Log(title = "碳核查-排放报告", businessType = BusinessType.INSERT)
    @PostMapping("/generate")
    public AjaxResult generate(@RequestParam Integer year,
                                @RequestParam(defaultValue = "全厂") String orgUnit,
                                @RequestParam(defaultValue = "发改委指南（2015版）") String standard,
                                @RequestParam(defaultValue = "通用行业模板") String templateType,
                                @RequestParam(defaultValue = "basicInfo,boundary,activityData,factor,result,uncertainty,voucherList") String sections) {
        EmsCarVerfReport report = reportService.generate(year, orgUnit, standard, templateType, sections);
        return AjaxResult.success("生成成功", report);
    }

    @ApiOperation("报告预览")
    @PreAuthorize("@ss.hasPermi('carbonVerification:report:list')")
    @GetMapping("/preview/{id}")
    public AjaxResult preview(@PathVariable Long id) {
        return AjaxResult.success(reportService.preview(id));
    }

    @ApiOperation("导出报告Excel")
    @PreAuthorize("@ss.hasPermi('carbonVerification:report:export')")
    @Log(title = "碳核查-排放报告", businessType = BusinessType.EXPORT)
    @GetMapping("/export/{id}")
    public void export(@PathVariable Long id, HttpServletResponse response) {
        try {
            reportService.exportExcel(id, response);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @ApiOperation("删除报告")
    @PreAuthorize("@ss.hasPermi('carbonVerification:report:remove')")
    @Log(title = "碳核查-排放报告", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id) {
        reportService.deleteById(id);
        return AjaxResult.success();
    }
}
