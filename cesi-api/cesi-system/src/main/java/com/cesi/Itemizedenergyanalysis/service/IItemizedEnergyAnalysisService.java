package com.cesi.Itemizedenergyanalysis.service;

import com.cesi.Itemizedenergyanalysis.domain.VO.ItemizedEnergyAnalysisVO;
import com.cesi.Itemizedenergyanalysis.dto.ItemizedEnergyAnalysisDTO;

public interface IItemizedEnergyAnalysisService {
    /**
     * 分项用能分析
     * @param dto   请求参数
     * @return 结果
     */
    ItemizedEnergyAnalysisVO getItemizedEnergyAnalysisService(ItemizedEnergyAnalysisDTO dto);
}
