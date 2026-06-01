package com.cesi.pcf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cesi.pcf.domain.EmspcfManufacturing;
import org.apache.ibatis.annotations.Param;
import java.math.BigDecimal;
import java.util.List;

public interface EmspcfManufacturingMapper extends BaseMapper<EmspcfManufacturing> {

    List<EmspcfManufacturing> selectByProductAndYear(@Param("productId") Long productId,
                                                      @Param("year") Integer year);

    BigDecimal sumByProductAndYear(@Param("productId") Long productId,
                                   @Param("year") Integer year);
}
