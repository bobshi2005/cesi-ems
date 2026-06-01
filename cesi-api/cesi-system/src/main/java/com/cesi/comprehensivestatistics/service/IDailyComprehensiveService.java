package com.cesi.comprehensivestatistics.service;


import com.cesi.comprehensivestatistics.domain.DailyComprehensive;

import java.util.Date;
import java.util.List;

/**
 * 全厂综合能耗统计 日
 * 
 * @author cesi
 */
public interface IDailyComprehensiveService {
    List<DailyComprehensive> getDailyComprehensiveList(String nodeId, List<DailyComprehensive> dataList, Date beginTime, Date endTime, String timeType, String indexStorageId);
    List<DailyComprehensive> getListChart(String indexId, Date beginTime, Date endTime, String timeType, String indexStorageId);
}
