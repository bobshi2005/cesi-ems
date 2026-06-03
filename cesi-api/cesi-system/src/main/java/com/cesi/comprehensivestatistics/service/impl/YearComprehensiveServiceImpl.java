package com.cesi.comprehensivestatistics.service.impl;

import cn.hutool.core.date.DateUtil;
import com.cesi.common.constant.CommonConst;
import com.cesi.common.utils.DateTimeUtil;
import com.cesi.comprehensivestatistics.domain.YearComperhensive;
import com.cesi.comprehensivestatistics.mapper.YearComprehensiveMapper;
import com.cesi.comprehensivestatistics.service.IyearComprehensive;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 业务层处理
 * 
 * @author cesi
 */
@Service
@AllArgsConstructor
public class YearComprehensiveServiceImpl implements IyearComprehensive {
    private YearComprehensiveMapper yearMapper;

    @Override
    public List<YearComperhensive> getYearComprehensiveList(String nodeId, List<YearComperhensive> dataList,
                                                            Date beginTime, Date endTime, String timeType, String indexStorageId){
        if (StringUtils.isNotEmpty(nodeId)) {
            return yearMapper.getYearComprehensiveList(nodeId, dataList, beginTime, endTime, timeType, indexStorageId);
        }
        return Collections.emptyList();
    }
    @Override
    public List<YearComperhensive> getListChart(String indexId, Date beginTime, Date endTime, String timeType, String indexStorageId){
        List<YearComperhensive> dataList = new ArrayList<>();
        if (StringUtils.isNotEmpty(indexId)) {
            List<YearComperhensive> listChart = yearMapper.getListChart(indexId, beginTime, endTime, timeType, indexStorageId);
            if (CollectionUtils.isNotEmpty(listChart)) {
                Date date = new Date();
                YearComperhensive first = listChart.get(CommonConst.DIGIT_0);
                Map<String, YearComperhensive> listChartMap = yearMapper.getListChart(indexId, beginTime, endTime, timeType, indexStorageId)
                        .stream().collect(Collectors.toMap(YearComperhensive::getTimeCode, prot -> prot));
                while (beginTime.before(date)) {
                    YearComperhensive yearComperhensive = new YearComperhensive();
                    String format = CommonConst.WORD_M + DateUtil.format(beginTime, DateTimeUtil.COMMON_PATTERN_MONTH);
                    YearComperhensive item = listChartMap.get(format);
                    if (ObjectUtils.isNotEmpty(item)) {
                        yearComperhensive = item;
                    } else {
                        yearComperhensive.setTimeCode(format);
                        yearComperhensive.setIndexId(indexId);
                        yearComperhensive.setTimeType(timeType);
                        yearComperhensive.setUnitId(first.getUnitId());
                        yearComperhensive.setIndexName(first.getIndexName());
                    }
                    dataList.add(yearComperhensive);
                    beginTime = DateUtil.offsetMonth(beginTime, CommonConst.DIGIT_1);
                }
            }
        }
        return dataList;
    }
}
