package com.cesi.processenergy.service;

import com.cesi.common.utils.TypeTime;
import com.cesi.processenergy.domain.YearProcessEnergy;
import com.cesi.realtimedata.domain.dto.DataItemQueryDTO;

import java.util.Date;
import java.util.List;

/**
 *工序能耗 日
 *
 * @author cesi
 */
public interface IYearProcessEnergyService {

    List<YearProcessEnergy> getYearProcessEnergy(List<String> indexIds, List<TypeTime> dataList, Date beginTime, Date endTime, String timeType, String indexStorageId);

    /**
     *
     * @param queryDto
     * @return
     */
    List<YearProcessEnergy> getListChart(DataItemQueryDTO queryDto);
}
