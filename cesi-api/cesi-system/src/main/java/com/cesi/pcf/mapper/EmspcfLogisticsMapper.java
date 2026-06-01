package com.cesi.pcf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cesi.pcf.domain.EmspcfLogistics;
import org.apache.ibatis.annotations.Param;
import java.math.BigDecimal;
import java.util.List;

public interface EmspcfLogisticsMapper extends BaseMapper<EmspcfLogistics> {

    List<EmspcfLogistics> selectByProductAndYear(@Param("productId") Long productId,
                                                  @Param("year") Integer year);

    BigDecimal sumByProductAndYear(@Param("productId") Long productId,
                                   @Param("year") Integer year);
}
