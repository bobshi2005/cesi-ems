package com.cesi.web.controller.meter;


import com.cesi.common.annotation.Log;
import com.cesi.common.core.controller.BaseController;
import com.cesi.common.core.domain.AjaxResult;
import com.cesi.common.core.page.TableDataInfo;
import com.cesi.common.enums.BusinessType;
import com.cesi.common.utils.poi.ExcelUtil;
import com.cesi.meter.domain.MeterAnnex;
import com.cesi.meter.services.IMeterAnnexService;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 计量器具档案附件Controller
 *
 * @author cesi
 */
@Slf4j
@RestController
@AllArgsConstructor
@Api(tags = "计量器具档案附件管理")
@RequestMapping("/meter/annex")
public class MeterAnnexController extends BaseController {
    private IMeterAnnexService meterAnnexService;

    /**
     * 查询计量器具档案附件列表
     */
    @PreAuthorize("@ss.hasPermi('meter:annex:list')")
    @GetMapping("/list")
    public TableDataInfo list(MeterAnnex meterAnnex) {
        startPage();
        List<MeterAnnex> list = meterAnnexService.selectMeterAnnexList(meterAnnex);
        return getDataTable(list);
    }

    /**
     * 导出计量器具档案附件列表
     */
    @PreAuthorize("@ss.hasPermi('meter:annex:export')")
    @Log(title = "计量器具档案附件", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(MeterAnnex meterAnnex) {
        List<MeterAnnex> list = meterAnnexService.selectMeterAnnexList(meterAnnex);
        ExcelUtil<MeterAnnex> util = new ExcelUtil<MeterAnnex>(MeterAnnex.class);
        return util.exportExcel(list, "annex");
    }
}
