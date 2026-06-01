package com.cesi.pcf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cesi.pcf.domain.EmspcfGreenCert;
import org.apache.ibatis.annotations.Param;
import java.math.BigDecimal;
import java.util.List;

public interface EmspcfGreenCertMapper extends BaseMapper<EmspcfGreenCert> {

    List<EmspcfGreenCert> selectByProductAndYear(@Param("productId") Long productId,
                                                  @Param("year") Integer year);

    BigDecimal sumOffsetByProductAndYear(@Param("productId") Long productId,
                                         @Param("year") Integer year);
}
