package com.cesi.keyequipment.service;


import com.cesi.keyequipment.domain.MonthlyKeyEquipment;

import java.util.Date;
import java.util.List;

/**
 *重点设备能耗统计 月
 *
 * @author cesi
 */
public interface IMonthlyKeyEquipmentService {

    List<MonthlyKeyEquipment> getMonthlyKeyEquipmentList(List<String> indexIds, List<MonthlyKeyEquipment> dataList, Date beginTime, Date endTime, String timeType, String indexStorageId);

    List<MonthlyKeyEquipment> getListChart(String indexId, Date beginTime, Date endTime, String timeType, String indexStorageId);
}
