package com.cesi.carbonverification.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cesi.carbonverification.domain.EmsCarVerfActivity;
import com.cesi.carbonverification.domain.vo.ActivitySourceVO;
import com.cesi.carbonverification.domain.vo.ActivitySummaryVO;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public interface IEmsCarVerfActivityService {

    Page<EmsCarVerfActivity> listPage(Integer year, String orgUnit, String emissionSource,
                                      long pageNum, long pageSize);

    List<ActivitySourceVO> getSourceList(Integer year, String orgUnit);

    ActivitySummaryVO getSummary(Integer year, String orgUnit);

    List<EmsCarVerfActivity> getDetail(Integer year, String orgUnit, String emissionSource);

    int add(EmsCarVerfActivity entity);

    int edit(EmsCarVerfActivity entity);

    int deleteById(Long id);

    /** 批量更新某排放源全年的排放因子，并重算各月排放量 */
    int updateFactorBySource(Integer year, String orgUnit, String emissionSource,
                             BigDecimal emissionFactor, String factorSource);

    String importData(MultipartFile file, Integer year, String orgUnit) throws Exception;

    List<EmsCarVerfActivity> exportList(Integer year, String orgUnit);
}
