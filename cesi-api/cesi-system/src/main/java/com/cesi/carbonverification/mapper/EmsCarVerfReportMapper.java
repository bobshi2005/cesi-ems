package com.cesi.carbonverification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cesi.carbonverification.domain.EmsCarVerfReport;
import org.apache.ibatis.annotations.Param;

public interface EmsCarVerfReportMapper extends BaseMapper<EmsCarVerfReport> {

    Page<EmsCarVerfReport> selectPage(
            Page<EmsCarVerfReport> page,
            @Param("reportYear") Integer reportYear,
            @Param("orgUnit") String orgUnit);
}
