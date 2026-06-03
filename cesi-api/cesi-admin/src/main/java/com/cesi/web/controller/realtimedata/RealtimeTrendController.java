package com.cesi.web.controller.realtimedata;

import org.springframework.beans.factory.annotation.Autowired;
import com.cesi.common.annotation.Log;
import com.cesi.common.core.controller.BaseController;
import com.cesi.common.core.domain.AjaxResult;
import com.cesi.common.enums.BusinessType;
import com.cesi.common.utils.poi.ExcelUtil;
import com.cesi.realtimedata.domain.dto.EnergyIndexMonitorDTO;
import com.cesi.realtimedata.domain.vo.EquipmentPointParametersExcel;
import com.cesi.realtimedata.domain.vo.ExportrealtimeTrendVO;
import com.cesi.realtimedata.service.RealtimeDatabaseService;
import com.cesi.realtimedata.service.RealtimeTrendService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 实时监测控制类
 **/
@Slf4j
@RestController
@Api(value = "实时监控", tags = {"实时监控"})
@RequestMapping("rtdb/realtimeTrend")
public class RealtimeTrendController extends BaseController {

    @Autowired
    private RealtimeTrendService realtimeTrendService;
    @Autowired
    private RealtimeDatabaseService realtimeDatabaseService;

    /**
     * 获取模型节点关联采集指标
     */
    @GetMapping("/list")
    @ApiOperation(value = "获取模型节点关联采集指标")
    public AjaxResult list(@Validated EnergyIndexMonitorDTO energyIndexMonitorDTO) {
        return AjaxResult.success(realtimeTrendService.list(energyIndexMonitorDTO));
    }

    /**
     * 获取历史模型节点关联采集指标数据
     */
    @Log(title = "获取历史模型节点关联采集指标数据", businessType = BusinessType.UPDATE)
    @GetMapping("/chartByDay")
    @ApiOperation(value = "获取历史模型节点关联采集指标数据")
    public AjaxResult lineList(@RequestParam String tagCode, @RequestParam String dataTime) {
        return AjaxResult.success(realtimeTrendService.chartByDay(tagCode, dataTime));
    }

    /**
     * 导出实时监测Excel信息
     */
    @Log(title = "导出实时监测Excel信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ApiOperation(value = "导出实时监测Excel信息")
    public void export(HttpServletResponse response, ExportrealtimeTrendVO exportrealtimeTrendVO) {
        List<EquipmentPointParametersExcel> list = realtimeTrendService.export(exportrealtimeTrendVO);
        ExcelUtil<EquipmentPointParametersExcel> util = new ExcelUtil<EquipmentPointParametersExcel>(EquipmentPointParametersExcel.class);
        util.exportExcel(response, list, "实时监测");
    }
}
