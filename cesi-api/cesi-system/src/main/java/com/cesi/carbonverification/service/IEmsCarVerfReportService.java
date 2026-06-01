package com.cesi.carbonverification.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cesi.carbonverification.domain.EmsCarVerfReport;
import com.cesi.carbonverification.domain.vo.ReportPreviewVO;

import javax.servlet.http.HttpServletResponse;

public interface IEmsCarVerfReportService {

    Page<EmsCarVerfReport> listPage(Integer reportYear, String orgUnit,
                                    long pageNum, long pageSize);

    ReportPreviewVO checkStatus(Integer year, String orgUnit);

    EmsCarVerfReport generate(Integer year, String orgUnit, String standard,
                               String templateType, String sections);

    ReportPreviewVO preview(Long id);

    void exportExcel(Long id, HttpServletResponse response) throws Exception;

    void deleteById(Long id);
}
