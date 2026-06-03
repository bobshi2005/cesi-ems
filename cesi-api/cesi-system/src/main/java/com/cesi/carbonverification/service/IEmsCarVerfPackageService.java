package com.cesi.carbonverification.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cesi.carbonverification.domain.EmsCarVerfPackage;
import com.cesi.carbonverification.domain.vo.PackageCompletenessVO;

import jakarta.servlet.http.HttpServletResponse;

public interface IEmsCarVerfPackageService {

    Page<EmsCarVerfPackage> listPage(Integer packageYear, long pageNum, long pageSize);

    PackageCompletenessVO getCompleteness(Integer year, String orgUnit);

    EmsCarVerfPackage generate(EmsCarVerfPackage param) throws Exception;

    void download(Long id, HttpServletResponse response) throws Exception;

    void deleteById(Long id);
}
