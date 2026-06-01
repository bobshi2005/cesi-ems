package com.cesi.energyMonitor.service;

import com.cesi.energyMonitor.domain.vo.ElectricThreePhaseVO;
import com.cesi.model.domain.MeterPoint;

import java.util.List;

public interface IElectricThreePhaseService {

    /**
     * 获取三相不平衡数据
     */
    ElectricThreePhaseVO list(String timeType, String timeCode, List<MeterPoint> meterPointList, String requestType, String meterId);
}
