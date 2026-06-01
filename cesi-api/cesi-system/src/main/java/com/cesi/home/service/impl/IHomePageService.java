package com.cesi.home.service.impl;

import com.cesi.consumptionanalysis.domain.vo.RankingEnergyData;
import com.cesi.home.domain.vo.HomeEnergyConsumptionTrendVO;
import com.cesi.home.domain.vo.HomeEnergyStatisticsVO;
import com.cesi.home.domain.vo.HomePeakValleyVO;

import java.util.List;

/**
 * description
 *
 * @author
 */
public interface IHomePageService {
    List<HomeEnergyStatisticsVO> energyConsumptionSummation(String timeType, String modelcode);

    List<HomePeakValleyVO> peakValley(String timeType, String modelcode);

    HomeEnergyConsumptionTrendVO energyConsumptionTrend(String timeType, String modelcode);

    List<RankingEnergyData> energyConsumptionRanking(String modelcode, String timeType);
}
