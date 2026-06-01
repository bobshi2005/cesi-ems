package com.cesi.comprehensivestatistics.service;


import com.cesi.basicdata.domain.Device;
import com.cesi.common.enums.TimeType;
import com.cesi.comprehensivestatistics.domain.ComprehensiveStatistics;

import java.util.Date;
import java.util.List;

/**
 * 【请填写功能名称】Service接口
 *
 * @author cesi
 */
public interface IComprehensiveStatisticsService {
    List<Device> getFacilityArchives();
    List<ComprehensiveStatistics> getDatasByList(List<String> indexIds, Date beginTime, Date endTime, TimeType timeType);
    List<ComprehensiveStatistics> getDatasByIndex(List<String> indexIds,Date beginTime, TimeType timeType);
    List<ComprehensiveStatistics>  getDatasIndex(List<String> indexIds,Date dataTime, TimeType timeType);
    List<ComprehensiveStatistics> getEnergyByIndex(String indexType);
}