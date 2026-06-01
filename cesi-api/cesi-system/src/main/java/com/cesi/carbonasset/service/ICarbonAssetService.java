package com.cesi.carbonasset.service;

import com.cesi.carbonasset.domain.vo.CarbonAssetPageVO;
import com.cesi.carbonasset.domain.vo.CarbonAssetSaveDTO;

public interface ICarbonAssetService {

    CarbonAssetPageVO getPageData(Integer year);

    void saveData(CarbonAssetSaveDTO dto);
}
