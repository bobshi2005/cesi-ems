package com.cesi.peakvalley.domain.vo.peakvalley;

import com.cesi.home.domain.vo.HomeEnergyConsumptionTrendVO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author cesi
 */
@Getter
@Setter
public class PeakValleyDayVO {
    List<PeakValleyLineChatVO> costList;
    List<PeakValleyLineChatVO> powerConsumptionList;
    PeakValleyDayTotalVO totalVO;

    HomeEnergyConsumptionTrendVO costNewList;

    HomeEnergyConsumptionTrendVO powerNewList;
}
