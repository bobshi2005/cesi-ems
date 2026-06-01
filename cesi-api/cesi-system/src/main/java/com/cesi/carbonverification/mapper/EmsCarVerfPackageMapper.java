package com.cesi.carbonverification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cesi.carbonverification.domain.EmsCarVerfPackage;
import org.apache.ibatis.annotations.Param;

public interface EmsCarVerfPackageMapper extends BaseMapper<EmsCarVerfPackage> {

    Page<EmsCarVerfPackage> selectPage(
            Page<EmsCarVerfPackage> page,
            @Param("packageYear") Integer packageYear);
}
