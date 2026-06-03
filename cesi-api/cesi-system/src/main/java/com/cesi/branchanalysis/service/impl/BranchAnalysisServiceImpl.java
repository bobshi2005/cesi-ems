package com.cesi.branchanalysis.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.cesi.branchanalysis.domain.BranchAnalysisVO;
import com.cesi.branchanalysis.service.IBranchAnalysisService;
import com.cesi.common.constant.TimeTypeConst;
import com.cesi.common.exception.ServiceException;
import com.cesi.common.utils.DateTimeUtil;
import com.cesi.common.utils.PropUtils;
import com.cesi.common.utils.TypeTime;
import com.cesi.energyUsed.mapper.EnergyUsedMapper;
import com.cesi.model.domain.vo.ModelNodePointInfo;
import com.cesi.model.mapper.ModelNodeMapper;
import com.cesi.realtimedata.domain.EnergyUsed;
import com.cesi.realtimedata.domain.dto.BranchAnalysisDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 支路用能分析
 *
 * @author cesi
 */
@Service
@AllArgsConstructor
public class BranchAnalysisServiceImpl implements IBranchAnalysisService {
    @Resource
    private ModelNodeMapper modelNodeMapper;
    @Resource
    private EnergyUsedMapper energyUsedMapper;

    @Override
    public BranchAnalysisVO getBranchAnalysisService(BranchAnalysisDTO dto) {
        String timeType = dto.getTimeType();
        String dataTime = dto.getDataTime();
        Date beginTime = DateTimeUtil.getTime(timeType, dataTime);
        DateTime endTime = null;

        List<ModelNodePointInfo> nodeIndexInfo = modelNodeMapper.getModelNodeIndexIdByNodeId(dto.getNodeId(), dto.getEnergyType());
        List<String> indexlist = nodeIndexInfo.stream().map(ModelNodePointInfo::getIndexId).collect(Collectors.toList());

        if (ObjectUtil.isEmpty(indexlist)) {
            return new BranchAnalysisVO();
        }

        List<TypeTime> dateTimeList;
        //根据时间类型调整时间范围
        switch (dto.getTimeType()) {
            case TimeTypeConst.TIME_TYPE_DAY:
                timeType = TimeTypeConst.TIME_TYPE_HOUR;
                endTime = DateUtil.endOfDay(beginTime);
                dateTimeList = DateTimeUtil.getDateTimeListSame(TimeTypeConst.TIME_TYPE_DAY, beginTime);
                break;
            case TimeTypeConst.TIME_TYPE_MONTH:
                timeType = TimeTypeConst.TIME_TYPE_DAY;
                endTime = DateUtil.endOfMonth(beginTime);
                dateTimeList = DateTimeUtil.getDateTimeListSame(TimeTypeConst.TIME_TYPE_MONTH, beginTime);
                break;
            case TimeTypeConst.TIME_TYPE_YEAR:
                timeType = TimeTypeConst.TIME_TYPE_MONTH;
                endTime = DateUtil.endOfYear(beginTime);
                dateTimeList = DateTimeUtil.getDateTimeListSame(TimeTypeConst.TIME_TYPE_YEAR, beginTime);
                break;
            default:
                throw new ServiceException("时间格式错误");
        }
        BranchAnalysisVO vo = new BranchAnalysisVO();
        if (ObjectUtil.isEmpty(indexlist)) {
            return vo;
        }
        List<EnergyUsed> dataItemlist = energyUsedMapper.getEnergyUsedTimeRangeInforByIndexIds(beginTime, endTime, timeType, indexlist);

        double sum = dataItemlist.stream().mapToDouble(EnergyUsed::getValue).sum();
        vo.setTotal(sum);
        vo.setNodeId(dto.getNodeId());
        vo.setNodeName(nodeIndexInfo.get(0).getName());
        Map<Date, List<EnergyUsed>> dateListMap = dataItemlist.stream().collect(Collectors.groupingBy(EnergyUsed::getDataTime));

        List<EnergyUsed> results = new ArrayList<>();
        dateListMap.forEach((key, value) -> {
            EnergyUsed energyUsed = new EnergyUsed();
            energyUsed.setDataTime(key);
            //保留四位小数
            double totalValue = value.stream().map(data -> BigDecimal.valueOf(data.getValue()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(4, RoundingMode.HALF_UP).doubleValue();
            energyUsed.setValue(totalValue);
            results.add(energyUsed);
        });
        //根据时间排序
        results.sort(Comparator.comparing(EnergyUsed::getDataTime));
        for (int i = 0; i < dateTimeList.size(); i++) {
            TypeTime typeTime = dateTimeList.get(i);
            Optional<EnergyUsed> dataItem = results.stream().filter(result -> result.getDataTime().equals(typeTime.getDateTime())).findFirst();
            if (dataItem.isPresent()) {
                EnergyUsed item = dataItem.get();
                PropUtils.setValue(vo, "value" + i, item.getValue());
            } else {
                PropUtils.setValue(vo, "value" + i, null);
            }
        }
        return vo;
    }
}
