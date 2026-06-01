package com.cesi.keyequipment.service;


import com.cesi.common.utils.TypeTime;
import com.cesi.keyequipment.domain.YearKeyEquipment;
import com.cesi.realtimedata.domain.dto.DataItemQueryDTO;

import java.util.Date;
import java.util.List;

/**
 * 重点设备能耗统计 年
 *
 * @author cesi
 */
public interface IYearKeyEquipmentService {
    List<YearKeyEquipment> getYearKeyEquipmentList(List<String> indexIds, List<TypeTime> dataList, Date beginTime, Date endTime, String timeType, String indexStorageId);

    List<YearKeyEquipment> getListChart(DataItemQueryDTO queryDto);
}
