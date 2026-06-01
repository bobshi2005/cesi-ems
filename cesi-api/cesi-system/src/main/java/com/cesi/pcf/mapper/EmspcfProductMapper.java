package com.cesi.pcf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cesi.pcf.domain.EmspcfProduct;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface EmspcfProductMapper extends BaseMapper<EmspcfProduct> {

    List<EmspcfProduct> selectByCategoryId(@Param("categoryId") Long categoryId);

    List<EmspcfProduct> selectAllEnabled();
}
