package com.cesi.web.controller.history;

import org.springframework.beans.factory.annotation.Autowired;
import cn.hutool.core.date.DateUtil;
import com.cesi.common.annotation.Log;
import com.cesi.common.constant.CommonConst;
import com.cesi.common.constant.TimeTypeConst;
import com.cesi.common.core.controller.BaseController;
import com.cesi.common.core.domain.AjaxResult;
import com.cesi.common.enums.BusinessType;
import com.cesi.common.enums.RetrievalModes;
import com.cesi.common.enums.TimeType;
import com.cesi.common.utils.ChartUtils;
import com.cesi.common.utils.DateTimeUtil;
import com.cesi.common.utils.poi.ExcelUtil;
import com.cesi.history.domain.dto.HistoricalDataDTO;
import com.cesi.history.domain.vo.HistoricalDataExcel;
import com.cesi.history.domain.vo.HistoricalDataVO;
import com.cesi.meter.domain.Meter;
import com.cesi.meter.services.IMeterService;
import com.cesi.model.domain.MeterPoint;
import com.cesi.model.service.IMeterPointService;
import com.cesi.realtimedata.domain.TagValue;
import com.cesi.realtimedata.service.RealtimeDatabaseService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 历史数据趋势Controller
 *
 * @author cesi
 */
@Slf4j
@RestController
@Api(tags = "历史数据趋势")
@RequestMapping("/dataMonitoring/historyDataTrend")
public class HistoryDataTrendController extends BaseController {
    @Autowired
    private IMeterPointService meterPointService;
    @Autowired
    private IMeterService meterImplementService;
    @Autowired
    private RealtimeDatabaseService realtimeDatabaseService;


    @Log(title = "获取模型节点关联采集指标", businessType = BusinessType.UPDATE)
    @GetMapping("/energyIndex/list")
    public AjaxResult getSettingIndex(MeterPoint meterPoint) {
        try {
            List<MeterPoint> infoList = meterPointService.listMeterPointByQuery(meterPoint);
            return AjaxResult.success(infoList);
        } catch (Exception ex) {
            logger.error("获取关联采集指标出错！", ex);
            return AjaxResult.error("获取关联指标出错!");
        }
    }

    @Log(title = "根据时间与点位查询历史监测数据", businessType = BusinessType.UPDATE)
    @GetMapping("/getHistoricalDataByIndexId")
    public AjaxResult getHistoricalDataByIndexId(HistoricalDataDTO dto) {
        try {
            // 获取点位信息
            MeterPoint meterPoint = meterPointService.getMeterPointById(dto.getIndexId());
            if (ObjectUtils.isEmpty(meterPoint)) {
                return AjaxResult.error("未找到点位信息");
            }
            List<Date> dateList = new ArrayList<>();
            if (TimeType.DAY.name().equals(dto.getTimeType())) {
                String timeCode = DateTimeUtil.getDateTime(dto.getDataTime(), DateTimeUtil.COMMON_PATTERN_TO_DAY);
                ChartUtils.generateDateList(TimeTypeConst.TIME_TYPE_DAY, timeCode, dateList);
            } else if (TimeType.HOUR.name().equals(dto.getTimeType())) {
                String timeCode = DateTimeUtil.getDateTime(dto.getDataTime(), DateTimeUtil.COMMON_PATTERN_TO_HOUR);
                ChartUtils.generateDateList(TimeTypeConst.TIME_TYPE_HOUR, timeCode, dateList);
            } else {
                return AjaxResult.error("时间间隔类型不正确");
            }
            // 查询计量器具
            Meter meterInfo = meterImplementService.selectMeterImplementById(meterPoint.getMeterId());
            if (ObjectUtils.isEmpty(meterInfo)) {
                return AjaxResult.error("未找到计量器具信息");
            }
            List<HistoricalDataVO> voList = new ArrayList<>();
            for (Date date : dateList) {
                Date beginTime = date;
                List<TagValue> tagValues = new ArrayList<>();
                if(TimeType.DAY.name().equals(dto.getTimeType())){
                    Date endTime = DateUtil.offsetHour(DateUtil.offsetMinute(date, CommonConst.DIGIT_MINUS_1), CommonConst.DIGIT_1);
                    tagValues = realtimeDatabaseService.retrieve(meterPoint.getCode(), beginTime,endTime,CommonConst.DIGIT_1);
                }
                if(TimeType.HOUR.name().equals(dto.getTimeType())){
                    Date endTime = DateUtil.offsetMinute(DateUtil.offsetSecond(date, CommonConst.DIGIT_MINUS_1), CommonConst.DIGIT_1);
                    tagValues = realtimeDatabaseService.retrieve(meterPoint.getCode(), beginTime,endTime,CommonConst.DIGIT_1);
                }

                HistoricalDataVO vo = new HistoricalDataVO();
                vo.setDataTime(DateUtil.formatDateTime(date));
                vo.setIndexId(meterPoint.getPointId());
                vo.setIndexName(meterInfo.getInstallactionLocation() + "_" + meterInfo.getMeterName() + "_" + meterPoint.getName());
                vo.setValue(CommonConst.DOUBLE_MINUS_SIGN);
                if(ObjectUtils.isNotEmpty(tagValues)){
                    vo.setValue(tagValues.get(0).getValue().toString());
                }

                voList.add(vo);
            }
            return AjaxResult.success(voList);
        } catch (Exception ex) {
            logger.error("查询历史监测数据出错！", ex);
            return AjaxResult.error("查询历史监测数据出错!");
        }
    }

    @Log(title = "导出Excel", businessType = BusinessType.UPDATE)
    @GetMapping("/export")
    public AjaxResult export(HistoricalDataDTO dto) {
        try {
            // 获取点位信息
            MeterPoint meterPoint = meterPointService.getMeterPointById(dto.getIndexId());
            if (ObjectUtils.isEmpty(meterPoint)) {
                return AjaxResult.success("未找到点位信息");
            }
            Date beginTime = dto.getDataTime();
            Date endTime;
            // 查询条数
            int count = 23;
            if (TimeTypeConst.TIME_TYPE_DAY.equals(dto.getTimeType())) {
                endTime = DateUtil.endOfDay(beginTime);
            } else {
                count = 19;
                endTime = DateUtil.offsetSecond(DateUtil.offsetHour(beginTime, 1), -1);
            }
            // 查询计量器具
            Meter infor = meterImplementService.selectMeterImplementById(meterPoint.getMeterId());
            List<TagValue> tagValueList = realtimeDatabaseService.retrieve(meterPoint.getCode(), beginTime, endTime,
                    RetrievalModes.BestFit, count);
            List<HistoricalDataExcel> excelList = new ArrayList<>();
            Date date = DateUtil.date();
            for (int i = 0; i < count + 1; i++) {
                HistoricalDataExcel vo = new HistoricalDataExcel();
                String indexName = meterPoint.getName();
                if (ObjectUtils.isNotEmpty(infor)) {
                    indexName = infor.getInstallactionLocation() + "_" + infor.getMeterName() + "_" + indexName;
                }
                vo.setIndexName(indexName);
                // 取值
                String value = "--";
                String usedValue = "--";
                if (beginTime.getTime() <= date.getTime()) {
                    try {
                        TagValue tagValue = tagValueList.get(i);
                        BigDecimal cumulative = BigDecimal.valueOf(tagValue.getValue());
                        if (i > 0) {
                            TagValue previousTagValue = tagValueList.get(i - 1);
                            BigDecimal previousValue = BigDecimal.valueOf(previousTagValue.getValue());
                            usedValue = String.valueOf(cumulative.subtract(previousValue).setScale(2, RoundingMode.HALF_UP));
                        }
                        value = String.valueOf(cumulative.setScale(2, RoundingMode.HALF_UP));
                    } catch (Exception ignored) {
                    }
                }
                // 时间
                String timeName = DateUtil.formatDateTime(beginTime);
                vo.setDataTime(timeName);
                if ("DAY".equals(dto.getTimeType())) {
                    beginTime = DateUtil.offsetHour(beginTime, 1);
                } else {
                    beginTime = DateUtil.offsetMinute(beginTime, 3);
                }
                vo.setValue(String.valueOf(value));
                vo.setUsedValue(String.valueOf(usedValue));
                excelList.add(vo);
            }
            ExcelUtil<HistoricalDataExcel> util = new ExcelUtil<>(HistoricalDataExcel.class);
            String sheetName = "历史数据统计" + DateUtil.formatDate(dto.getDataTime());
//            return util.exportRealTimeDataExcel(excelList, sheetName);
            return util.exportExcel(excelList, sheetName);
        } catch (Exception ex) {
            logger.error("导出Excel数据出错！", ex);
            return AjaxResult.error("导出Excel数据出错!");
        }
    }

}
