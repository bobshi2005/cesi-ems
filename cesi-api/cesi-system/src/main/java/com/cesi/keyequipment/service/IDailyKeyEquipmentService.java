package com.cesi.keyequipment.service;

import com.cesi.basicdata.domain.Device;
import com.cesi.keyequipment.domain.DailyKeyEquipment;

import java.util.Date;
import java.util.List;

/**
 *重点设备能耗统计 日
 *
 * @author cesi
 */
public interface IDailyKeyEquipmentService {
    public List<DailyKeyEquipment> getdailyKeyEquipmentList(List<String> indexIds, List<DailyKeyEquipment> dataList, Date beginTime, Date endTime, String timeType, String indexStorageId);
    public List<DailyKeyEquipment> getListChart(String indexId, Date beginTime, Date endTime, String timeType, String indexStorageId);
    public List<Device> getFacilityArchives();
    public List<Device> getPointFacility();
}
