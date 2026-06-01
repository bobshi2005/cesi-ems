package com.cesi.web.controller.processenergy;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.cesi.common.core.controller.BaseController;
import com.cesi.common.core.domain.AjaxResult;
import com.cesi.common.utils.DateTimeUtil;
import com.cesi.common.utils.TypeTime;
import com.cesi.model.domain.ModelNode;
import com.cesi.model.domain.vo.MeterPointVO;
import com.cesi.model.service.IModelNodeService;
import com.cesi.processenergy.domain.YearProcessEnergy;
import com.cesi.processenergy.service.IYearProcessEnergyService;
import com.cesi.realtimedata.domain.dto.DataItemQueryDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 工序能耗 年
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/processEnergy/YearProcessEnergy")
@Api(value = "工序能耗统计（年）", tags = {"工序能耗统计"})
public class YearProcessEnergyController extends BaseController {

    private IModelNodeService modelNodeService;
    private IYearProcessEnergyService yearProcessEnergyService;

    @GetMapping("/list")
    @ApiOperation(value = "工序能耗统计（年）列表")
    private AjaxResult list(DataItemQueryDTO dataItem) {
        List<ModelNode> nodeId = modelNodeService.getModelNodeByModelCode(dataItem.getIndexCode());
        if (CollectionUtils.isEmpty(nodeId)) {
            return success(new ArrayList<>());
        }
        List<MeterPointVO> energyList = modelNodeService.getSettingIndex(nodeId.get(0).getNodeId());
        if (CollectionUtils.isEmpty(energyList)) {
            return success(new ArrayList<>());
        }
        List<String> indexIds = energyList.stream().map(MeterPointVO::getPointId).collect(Collectors.toList());

        Date convertTime = DateTimeUtil.getTypeTime(dataItem.getTimeType(), dataItem.getDataTime());
        DateTime beginTime = DateUtil.beginOfYear(convertTime);
        DateTime endTime = DateUtil.endOfYear(convertTime);

        List<TypeTime> typeTimeList = DateTimeUtil.getDateTimeList(dataItem.getTimeType(), convertTime);

        List<YearProcessEnergy> list = yearProcessEnergyService.getYearProcessEnergy(indexIds, typeTimeList, beginTime, endTime, dataItem.getTimeType(), dataItem.getEnergyType());
        return success(list);
    }

    @GetMapping("/listChart")
    @ApiOperation(value = "工序能耗（年）图表")
    public AjaxResult listChart(DataItemQueryDTO queryDto) {
        List<YearProcessEnergy> list = yearProcessEnergyService.getListChart(queryDto);
        return AjaxResult.success(list);
    }
}
