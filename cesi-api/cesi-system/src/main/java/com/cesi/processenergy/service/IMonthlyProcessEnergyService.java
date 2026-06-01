package com.cesi.processenergy.service;

import com.cesi.common.enums.TimeType;
import com.cesi.processenergy.domain.MonthlyProcessEnergy;

import java.util.Date;
import java.util.List;

/**
 *工序能耗 日
 *
 * @author cesi
 */
public interface IMonthlyProcessEnergyService {
    List<MonthlyProcessEnergy> getMonthlyProcessEnergy(List<String> indexIds, List<MonthlyProcessEnergy> dataList, Date beginTime, Date endTime, String timeType, String indexStorageId);

    List<MonthlyProcessEnergy> getListChart(String indexId, Date beginTime, Date endTime, String timeType, String indexStorageId);
}
