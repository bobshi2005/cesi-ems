package com.cesi.pcf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cesi.pcf.domain.EmspcfCategory;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface EmspcfCategoryMapper extends BaseMapper<EmspcfCategory> {

    List<EmspcfCategory> selectAll();

    int countProductsByCategoryId(@Param("categoryId") Long categoryId);
}
