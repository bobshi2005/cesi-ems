package com.cesi.energydata.service;


import com.cesi.consumptionanalysis.domain.vo.RankingEnergyData;
import com.cesi.energydata.vo.EnergyChainYoyVO;
import com.cesi.energydata.vo.FactoryEnergyConsumptionVo;
import com.cesi.energydata.vo.PurchaseConsumptionVo;
import com.cesi.peakvalley.domain.dto.PeakValleyDTO;
import com.cesi.peakvalley.domain.vo.peakvalley.PeakValleyDayVO;
import com.cesi.peakvalley.domain.vo.peakvalley.PeakValleyHourVO;

import java.util.List;

public interface IEnergyDataStatisticService {
  PeakValleyDayVO segmentAnalysisMonth(PeakValleyDTO dto);

  List<RankingEnergyData> energyConsumptionRanking(String modelCode, String timeType);

  FactoryEnergyConsumptionVo factoryEnergyConsumption(String timeType, String modelCode);

  List<PurchaseConsumptionVo> purchaseConsumption(String modelCode, String timeType );

  List<PurchaseConsumptionVo> costProp(String modelCode, String timeType);

  List<EnergyChainYoyVO> energyConsumptionSummation(String timeType, String modelCode);

  PeakValleyDayVO segmentAnalysisDay(PeakValleyDTO dto);

  PeakValleyHourVO segmentAnalysisHour(PeakValleyDTO dto);
}
