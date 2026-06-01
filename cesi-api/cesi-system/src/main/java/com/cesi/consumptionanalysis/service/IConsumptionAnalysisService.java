package com.cesi.consumptionanalysis.service;

import com.cesi.consumptionanalysis.domain.dto.ConsumptionAnalysisDTO;
import com.cesi.consumptionanalysis.domain.vo.ConsumptionAnalysisVO;
import com.cesi.consumptionanalysis.domain.vo.ProductEnergyAnalysisVO;
import com.cesi.consumptionanalysis.domain.vo.RankingDataVO;
import com.cesi.consumptionanalysis.domain.vo.RankingEnergyData;

import java.util.List;

/**
 * description 能耗对比分析
 *
 * @author cesi
 */
public interface IConsumptionAnalysisService {
    ConsumptionAnalysisVO getByArea(ConsumptionAnalysisDTO dto);

    List<RankingDataVO> getByDepartment(ConsumptionAnalysisDTO dto);

    ConsumptionAnalysisVO getComprehensiveEnergy(ConsumptionAnalysisDTO dto);

    ConsumptionAnalysisVO getYOY(ConsumptionAnalysisDTO dto);

    List<RankingEnergyData> getEnergyRanking(ConsumptionAnalysisDTO dto);

    ConsumptionAnalysisVO getPlanAndProdCount(ConsumptionAnalysisDTO dto);

    ProductEnergyAnalysisVO getProdEnergy(ConsumptionAnalysisDTO dto);
}
