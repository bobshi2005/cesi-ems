package com.cesi.web.controller.carbonverification;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cesi.carbonverification.domain.EmsCarVerfActivity;
import com.cesi.carbonverification.service.IEmsCarVerfActivityService;
import com.cesi.common.annotation.Log;
import com.cesi.common.core.controller.BaseController;
import com.cesi.common.core.domain.AjaxResult;
import com.cesi.common.core.page.TableDataInfo;
import com.cesi.common.enums.BusinessType;
import com.cesi.common.utils.poi.ExcelUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@Api(tags = "碳核查-活动数据追踪")
@RequestMapping("/carbonVerification/activity")
public class EmsCarVerfActivityController extends BaseController {

    private final IEmsCarVerfActivityService activityService;

    @ApiOperation("分页查询活动数据")
    @PreAuthorize("@ss.hasPermi('carbonVerification:activity:list')")
    @GetMapping("/page")
    public TableDataInfo page(@RequestParam(required = false) Integer year,
                               @RequestParam(required = false) String orgUnit,
                               @RequestParam(required = false) String emissionSource,
                               @RequestParam(defaultValue = "1")  Long pageNum,
                               @RequestParam(defaultValue = "20") Long pageSize) {
        Page<EmsCarVerfActivity> page = activityService.listPage(year, orgUnit, emissionSource, pageNum, pageSize);
        return getDataTable(page);
    }

    @ApiOperation("按排放源聚合列表（主列表页，含年总量+月度样本）")
    @PreAuthorize("@ss.hasPermi('carbonVerification:activity:list')")
    @GetMapping("/sourceList")
    public AjaxResult sourceList(@RequestParam Integer year,
                                  @RequestParam(defaultValue = "全厂") String orgUnit) {
        return AjaxResult.success(activityService.getSourceList(year, orgUnit));
    }

    @ApiOperation("获取汇总统计（卡片+热力矩阵）")
    @PreAuthorize("@ss.hasPermi('carbonVerification:activity:list')")
    @GetMapping("/summary")
    public AjaxResult summary(@RequestParam Integer year,
                               @RequestParam(defaultValue = "全厂") String orgUnit) {
        return AjaxResult.success(activityService.getSummary(year, orgUnit));
    }

    @ApiOperation("获取排放源月度明细")
    @PreAuthorize("@ss.hasPermi('carbonVerification:activity:list')")
    @GetMapping("/detail")
    public AjaxResult detail(@RequestParam Integer year,
                              @RequestParam(defaultValue = "全厂") String orgUnit,
                              @RequestParam String emissionSource) {
        return AjaxResult.success(activityService.getDetail(year, orgUnit, emissionSource));
    }

    @ApiOperation("新增活动数据")
    @PreAuthorize("@ss.hasPermi('carbonVerification:activity:add')")
    @Log(title = "碳核查-活动数据", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public AjaxResult add(@RequestBody EmsCarVerfActivity entity) {
        return toAjax(activityService.add(entity));
    }

    @ApiOperation("修改活动数据")
    @PreAuthorize("@ss.hasPermi('carbonVerification:activity:edit')")
    @Log(title = "碳核查-活动数据", businessType = BusinessType.UPDATE)
    @PutMapping("/edit")
    public AjaxResult edit(@RequestBody EmsCarVerfActivity entity) {
        return toAjax(activityService.edit(entity));
    }

    @ApiOperation("删除活动数据")
    @PreAuthorize("@ss.hasPermi('carbonVerification:activity:remove')")
    @Log(title = "碳核查-活动数据", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public AjaxResult delete(@PathVariable Long id) {
        return toAjax(activityService.deleteById(id));
    }

    @ApiOperation("批量更新排放因子（按排放源全年更新，同时重算排放量）")
    @PreAuthorize("@ss.hasPermi('carbonVerification:activity:edit')")
    @Log(title = "碳核查-活动数据", businessType = BusinessType.UPDATE)
    @PutMapping("/updateFactor")
    public AjaxResult updateFactor(@RequestBody Map<String, Object> body) {
        Integer year = (Integer) body.get("year");
        String orgUnit = (String) body.getOrDefault("orgUnit", "全厂");
        String emissionSource = (String) body.get("emissionSource");
        BigDecimal emissionFactor = new BigDecimal(body.get("emissionFactor").toString());
        String factorSource = (String) body.getOrDefault("factorSource", "实测值");
        int updated = activityService.updateFactorBySource(year, orgUnit, emissionSource, emissionFactor, factorSource);
        return AjaxResult.success("已更新 " + updated + " 条记录", null);
    }

    @ApiOperation("导入Excel数据")
    @PreAuthorize("@ss.hasPermi('carbonVerification:activity:import')")
    @Log(title = "碳核查-活动数据", businessType = BusinessType.IMPORT)
    @PostMapping("/importData")
    public AjaxResult importData(@RequestParam MultipartFile file,
                                  @RequestParam Integer year,
                                  @RequestParam(defaultValue = "全厂") String orgUnit) {
        try {
            String result = activityService.importData(file, year, orgUnit);
            return AjaxResult.success(result);
        } catch (Exception e) {
            return AjaxResult.error("导入失败：" + e.getMessage());
        }
    }

    @ApiOperation("下载导入模板")
    @GetMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response) throws Exception {
        ExcelUtil<EmsCarVerfActivity> util = new ExcelUtil<>(EmsCarVerfActivity.class);
        util.importTemplateExcel(response, "活动数据导入模板");
    }

    @ApiOperation("导出Excel")
    @PreAuthorize("@ss.hasPermi('carbonVerification:activity:export')")
    @Log(title = "碳核查-活动数据", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response,
                        @RequestParam Integer year,
                        @RequestParam(defaultValue = "全厂") String orgUnit) throws Exception {
        List<EmsCarVerfActivity> list = activityService.exportList(year, orgUnit);
        ExcelUtil<EmsCarVerfActivity> util = new ExcelUtil<>(EmsCarVerfActivity.class);
        util.exportExcel(response, list, year + "年活动数据");
    }
}
