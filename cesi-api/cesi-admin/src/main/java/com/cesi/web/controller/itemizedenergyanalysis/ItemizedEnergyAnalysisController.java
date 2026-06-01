package com.cesi.web.controller.itemizedenergyanalysis;

import com.cesi.Itemizedenergyanalysis.domain.VO.ItemizedEnergyAnalysisVO;
import com.cesi.Itemizedenergyanalysis.dto.ItemizedEnergyAnalysisDTO;
import com.cesi.Itemizedenergyanalysis.service.IItemizedEnergyAnalysisService;
import com.cesi.common.core.domain.AjaxResult;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.cesi.common.core.domain.AjaxResult.success;

/**
 * 分项用能分析 接口
 */
@Slf4j
@RestController
@AllArgsConstructor
@Api(value = "分项用能分析", tags = {"分项用能分析"})
@RequestMapping("/itemizedEnergyAnalysis")
public class ItemizedEnergyAnalysisController {

    private IItemizedEnergyAnalysisService itemizedEnergyAnalysisService;

    /**
     *
     * @param dataItem
     * @return
     */
    @GetMapping("/list")
    private AjaxResult list(ItemizedEnergyAnalysisDTO dataItem) {
        ItemizedEnergyAnalysisVO list = itemizedEnergyAnalysisService.getItemizedEnergyAnalysisService(dataItem);
        return success(list);
    }
}