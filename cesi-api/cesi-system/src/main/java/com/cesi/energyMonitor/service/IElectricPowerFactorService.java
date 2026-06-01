package com.cesi.energyMonitor.service;

import com.cesi.energyMonitor.domain.vo.ElectricPowerFactorVO;
import com.cesi.model.domain.MeterPoint;


public interface IElectricPowerFactorService {

    /**
     * 获取负荷分析数据
     */
    ElectricPowerFactorVO list(String timeCode, MeterPoint meterPoint);
}
