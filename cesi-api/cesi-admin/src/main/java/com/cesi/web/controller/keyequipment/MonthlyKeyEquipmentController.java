package com.cesi.web.controller.keyequipment;


import org.springframework.beans.factory.annotation.Autowired;
import com.cesi.common.core.controller.BaseController;
import com.cesi.common.core.domain.AjaxResult;
import com.cesi.keyequipment.domain.MonthlyKeyEquipment;
import com.cesi.keyequipment.service.IMonthlyKeyEquipmentService;
import com.cesi.model.domain.ModelNode;
import com.cesi.model.domain.vo.MeterPointVO;
import com.cesi.model.service.IModelNodeService;
import com.cesi.realtimedata.domain.dto.EnergyUsedDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 重点设备能耗统计 月
 *
 * @author cesi
 */
@Slf4j
@RestController
@RequestMapping("/keyEquipment/MonthlyKeyEquipment")
@Api(value = "重点设备能耗统计（月）", tags = {"设备单耗分析"})
public class MonthlyKeyEquipmentController extends BaseController {

    @Autowired
    private IModelNodeService modelNodeService;
    @Autowired
    private IMonthlyKeyEquipmentService monthlyKeyEquipmentService;

    @GetMapping("/list")
    @ApiOperation(value = "重点设备能耗统计（月）列表")
    public AjaxResult list(EnergyUsedDTO energyUsed) throws ParseException {
        List<MonthlyKeyEquipment> dataList = new ArrayList<>();

        Map tableColumn = new HashMap<>();//表数据
        DateFormat df = new SimpleDateFormat("yyyy-MM");
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = df.format(energyUsed.getDataTime());
        String hourDateTimeStr = "";
        int i = 1;
        String beginTime = formattedDate + "-01 00:00:00";
        energyUsed.setBeginTime(sf.parse(beginTime));
        String endTime = formattedDate + "-" + Integer.valueOf(getLastDayOfMonth(formattedDate).substring(getLastDayOfMonth(formattedDate).length() - 2)) + " 00:00:00";
        energyUsed.setEndTime(sf.parse(endTime));
        while (i <= Integer.valueOf(getLastDayOfMonth(formattedDate).substring(getLastDayOfMonth(formattedDate).length() - 2))) {
            if (i > 9) {
                hourDateTimeStr = formattedDate + "-" + i + " 00:00:00";
            } else {
                hourDateTimeStr = formattedDate + "-0" + i + " 00:00:00";
            }
            MonthlyKeyEquipment report = new MonthlyKeyEquipment();
            report.setDataTime(sf.parse(hourDateTimeStr));
            report.setValue("value" + i);
            dataList.add(report);
            tableColumn.put("value" + i, i + "日");
            i++;
        }
        List<Map> table = new ArrayList<>();
        MonthlyKeyEquipment reportList = new MonthlyKeyEquipment();
        table.add(tableColumn);
        reportList.setTablehead(table);
        List<ModelNode> nodeId = modelNodeService.getModelNodeByModelCode(energyUsed.getIndexCode());
        if (CollectionUtils.isEmpty(nodeId)) {
            return success(new ArrayList<>());
        }
        List<MeterPointVO> energyList = modelNodeService.getSettingIndex(nodeId.get(0).getNodeId());
        if (CollectionUtils.isEmpty(energyList)) {
            return success(new ArrayList<>());
        }
        List<String> indexIds = energyList.stream().map(MeterPointVO::getPointId).collect(Collectors.toList());

        List<MonthlyKeyEquipment> list = monthlyKeyEquipmentService.getMonthlyKeyEquipmentList(indexIds, dataList, energyUsed.getBeginTime(), energyUsed.getEndTime(), energyUsed.getTimeType(), energyUsed.getEnergyType());

        return success(list);
    }

    @GetMapping("/listChart")
    @ApiOperation(value = "重点设备能耗统计（月）图表")
    public AjaxResult listChart(EnergyUsedDTO energyUsed) throws ParseException {
        DateFormat df = new SimpleDateFormat("yyyy-MM");
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = df.format(energyUsed.getDataTime());
        String beginTime = formattedDate + "-01 00:00:00";
        energyUsed.setBeginTime(sf.parse(beginTime));
        String endTime = formattedDate + "-" + Integer.valueOf(getLastDayOfMonth(formattedDate).substring(getLastDayOfMonth(formattedDate).length() - 2)) + " 00:00:00";
        energyUsed.setEndTime(sf.parse(endTime));
        List<MonthlyKeyEquipment> list = monthlyKeyEquipmentService.getListChart(energyUsed.getIndexId(), energyUsed.getBeginTime(), energyUsed.getEndTime(), energyUsed.getTimeType(), energyUsed.getEnergyType());
        return AjaxResult.success(list);
    }

    public static String getLastDayOfMonth(String yearMonth) {
        int year = Integer.parseInt(yearMonth.split("-")[0]);  //年
        int month = Integer.parseInt(yearMonth.split("-")[1]); //月
        Calendar cal = Calendar.getInstance();
        // 设置年份
        cal.set(Calendar.YEAR, year);
        // 设置月份
        // cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.MONTH, month); //设置当前月的上一个月
        // 获取某月最大天数
        //int lastDay = cal.getActualMaximum(Calendar.DATE);
        int lastDay = cal.getMinimum(Calendar.DATE); //获取月份中的最小值，即第一天
        // 设置日历中月份的最大天数
        //cal.set(Calendar.DAY_OF_MONTH, lastDay);
        cal.set(Calendar.DAY_OF_MONTH, lastDay - 1); //上月的第一天减去1就是当月的最后一天
        // 格式化日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(cal.getTime());
    }
}
