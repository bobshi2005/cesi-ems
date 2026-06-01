package com.cesi.branchanalysis.service;


import com.cesi.branchanalysis.domain.BranchAnalysisVO;
import com.cesi.realtimedata.domain.dto.BranchAnalysisDTO;

/**
 * 支路用能分析服务
 */
public interface IBranchAnalysisService {
    /**
     * 支路用能分析
     *
     * @author cesi
     */
    BranchAnalysisVO getBranchAnalysisService(BranchAnalysisDTO dataItem);
}
