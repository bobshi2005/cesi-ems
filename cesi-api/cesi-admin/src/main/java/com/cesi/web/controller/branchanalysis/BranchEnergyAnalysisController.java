package com.cesi.web.controller.branchanalysis;

import com.cesi.branchanalysis.domain.BranchAnalysisVO;
import com.cesi.common.core.controller.BaseController;
import com.cesi.common.core.domain.AjaxResult;
import com.cesi.branchanalysis.service.IBranchAnalysisService;

import com.cesi.realtimedata.domain.dto.BranchAnalysisDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *支路用能分析
 *
 * @author cesi
 */
@Slf4j
@RestController
@AllArgsConstructor
@Api(value = "支路用能能耗统计",tags = {"支路用能分析"})
@RequestMapping("/branchanalysis")
public class BranchEnergyAnalysisController extends BaseController {

    private IBranchAnalysisService branchAnalysisService;

    @GetMapping("/list")
    @ApiOperation(value = "支路用能能耗统计列表")
    public AjaxResult list(BranchAnalysisDTO dataItem)  {
        BranchAnalysisVO vo = branchAnalysisService.getBranchAnalysisService(dataItem);
        return success(vo);
    }
}

