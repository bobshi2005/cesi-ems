package com.cesi.web.controller.statisticalAnalysis;

import org.springframework.beans.factory.annotation.Autowired;
import com.cesi.common.annotation.Log;
import com.cesi.common.constant.CommonConst;
import com.cesi.common.core.domain.AjaxResult;
import com.cesi.energyUsed.service.IEnergyUsedService;
import com.cesi.statisticalAnalysis.domain.dto.FlowChartsDTO;
import com.cesi.statisticalAnalysis.domain.vo.QueryCompareRequest;
import com.cesi.statisticalAnalysis.service.IEnergyConsumeDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * 能耗统计分析
 * @author cesi
 */
@Slf4j
@RestController
@Api(tags = "能耗统计分析")
@RequestMapping("/statisticsAnalysis")
public class StatisticsAnalysisController {

    @Autowired
    private IEnergyUsedService energyUsedService;
    @Autowired
    private IEnergyConsumeDataService energyConsumeDataService;

    /**
     * 获取能流图形分析
     *
     * @param dto 请求参数
     * @return 结果
     */
    @Log(title = "获取能流图形分析")
    @ApiOperation(value = "获取能流图形分析", notes = "获取能流图形分析")
    @GetMapping(value = "/getFlowCharts")
    public AjaxResult getFlowCharts(@Validated FlowChartsDTO dto) {
        return AjaxResult.success(energyUsedService.getFlowCharts(dto));
    }

    @Log(title = "能耗统计分析-获取同比分析列表数据")
    @ApiOperation(value = "能耗统计分析-获取同比分析列表数据", notes = "能耗统计分析-获取同比分析列表数据")
    @GetMapping(value = "/querySameCompareList")
    public AjaxResult querySameCompareList(@Validated QueryCompareRequest queryCompareRequest) {
        return AjaxResult.success(energyConsumeDataService.listEnergyTypeYoyInfo(queryCompareRequest, CommonConst.ENERGY_COMPARISON_YOY));
    }

    /**
     * 获取环比分析数据
     * <p>
     * 通过自己的服务访问地址：http://localhost:7005/fengniao/energyDataItem/queryLoopCompare?timeType=1
     * 通过网关访问地址：http://localhost:9999/jeecg-fengniao/fengniao/energyDataItem/queryLoopCompare?timeType=1
     *
     * @param queryCompareRequest
     * @return
     */
    @Log(title = "能耗统计分析-获取环比分析列表数据")
    @ApiOperation(value = "能耗统计分析-获取环比分析列表数据", notes = "能耗统计分析-获取环比分析列表数据")
    @GetMapping(value = "/queryLoopCompareList")
    public AjaxResult queryLoopCompareList(@Validated QueryCompareRequest queryCompareRequest) {

        return AjaxResult.success(energyConsumeDataService.listEnergyTypeYoyInfo(queryCompareRequest, CommonConst.ENERGY_COMPARISON_MOM));
    }
}
