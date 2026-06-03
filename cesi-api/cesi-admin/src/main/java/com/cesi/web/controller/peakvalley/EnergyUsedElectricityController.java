package com.cesi.web.controller.peakvalley;


import org.springframework.beans.factory.annotation.Autowired;
import com.cesi.common.core.controller.BaseController;
import com.cesi.common.core.domain.AjaxResult;
import com.cesi.common.utils.poi.ExcelUtil;
import com.cesi.peakvalley.domain.dto.PeakValleyDTO;
import com.cesi.peakvalley.domain.vo.peakvalley.PeakValleyHourDataVO;
import com.cesi.peakvalley.service.IEnergyUsedElectricityService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 尖峰平谷数据Controller
 *
 * @author cesi
 */
@Slf4j
@RestController
@Api(tags = "尖峰平谷数据")
@RequestMapping("/peakValley")
public class EnergyUsedElectricityController extends BaseController {

    @Autowired
    private IEnergyUsedElectricityService rulesService;


    /**
     * 获取尖峰平谷数据统计 - 按小时统计
     */
    @GetMapping("/segmentAnalysis/hour")
    @ApiOperation(value = "获取尖峰平谷分时统计")
    public AjaxResult segmentAnalysisHour(PeakValleyDTO dto) {
        return AjaxResult.success(rulesService.segmentAnalysisHour(dto));
    }

    /**
     * 获取尖峰平谷数据统计 - 按小时统计
     */
    @PostMapping("/segmentAnalysis/hour/export")
    @ApiOperation(value = "获取尖峰平谷分时统计")
    public void segmentAnalysisHourExport(HttpServletResponse response, PeakValleyDTO dto) {
        List<PeakValleyHourDataVO> list = rulesService.segmentAnalysisHourExport(dto);
        ExcelUtil<PeakValleyHourDataVO> util = new ExcelUtil<PeakValleyHourDataVO>(PeakValleyHourDataVO.class);
        util.exportExcel(response, list, "尖峰平谷分时统计数据");
    }

    /**
     * 获取尖峰平谷数据统计 - 按天统计
     */
    @GetMapping("/segmentAnalysis/day")
    @ApiOperation(value = "获取尖峰平谷分时统计")
    public AjaxResult segmentAnalysisDay(PeakValleyDTO dto) {
        return AjaxResult.success(rulesService.segmentAnalysisDay(dto));
    }

}
