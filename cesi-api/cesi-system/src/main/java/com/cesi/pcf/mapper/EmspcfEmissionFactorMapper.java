package com.cesi.pcf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cesi.pcf.domain.EmspcfEmissionFactor;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface EmspcfEmissionFactorMapper extends BaseMapper<EmspcfEmissionFactor> {

    List<EmspcfEmissionFactor> selectByCategory(@Param("factorCategory") String factorCategory);
}
