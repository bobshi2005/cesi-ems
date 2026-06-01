package com.cesi.costmanagement.service;

import com.cesi.costmanagement.domain.vo.DeviationAnalysisDTO;
import com.cesi.costmanagement.domain.vo.ElectricityDataOutItem;
import com.cesi.costmanagement.domain.vo.StatisticsDataOutItem;
import com.cesi.costmanagement.domain.vo.StatisticsInfoListOut;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * 成本策略Service接口
 *
 * @author Cesi
 */
public interface DeviationAnalysisService {
    StatisticsDataOutItem getStatisticsDataItem(DeviationAnalysisDTO dto) throws ParseException;

    ElectricityDataOutItem getElectricityDataItem(DeviationAnalysisDTO dto) throws ParseException;

    StatisticsInfoListOut getStatisticsInfoList(DeviationAnalysisDTO dto) throws ParseException;

    List<StatisticsInfoListOut> getStatisticsInfoNewList(DeviationAnalysisDTO dto) throws ParseException;

    Map<String, Object> getEcharst(DeviationAnalysisDTO dto) throws ParseException;
}
