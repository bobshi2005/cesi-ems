package com.cesi.pcf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cesi.pcf.domain.EmspcfAccountingParams;
import org.apache.ibatis.annotations.Param;

public interface EmspcfAccountingParamsMapper extends BaseMapper<EmspcfAccountingParams> {

    void upsertParam(EmspcfAccountingParams param);
}
