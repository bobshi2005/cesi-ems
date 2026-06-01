package com.cesi.pcf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cesi.pcf.domain.EmspcfGreenCertFactor;
import java.util.List;

public interface EmspcfGreenCertFactorMapper extends BaseMapper<EmspcfGreenCertFactor> {

    List<EmspcfGreenCertFactor> selectAllEnabled();
}
