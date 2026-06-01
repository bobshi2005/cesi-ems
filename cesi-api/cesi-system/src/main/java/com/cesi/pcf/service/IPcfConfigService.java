package com.cesi.pcf.service;

import com.cesi.pcf.domain.EmspcfAccountingParams;
import com.cesi.pcf.domain.EmspcfEmissionFactor;
import com.cesi.pcf.domain.EmspcfGreenCertFactor;
import java.util.List;
import java.util.Map;

public interface IPcfConfigService {

    List<EmspcfEmissionFactor> listEmissionFactors(String factorCategory);

    void saveEmissionFactor(EmspcfEmissionFactor entity);

    void removeEmissionFactor(Long id);

    Map<String, String> getAccountingParams();

    void saveAccountingParams(List<EmspcfAccountingParams> params);

    List<EmspcfGreenCertFactor> listGreenCertFactors();

    void saveGreenCertFactor(EmspcfGreenCertFactor entity);

    void removeGreenCertFactor(Long id);
}
