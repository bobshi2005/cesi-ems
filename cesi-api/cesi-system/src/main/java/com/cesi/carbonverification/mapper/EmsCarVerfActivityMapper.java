package com.cesi.carbonverification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cesi.carbonverification.domain.EmsCarVerfActivity;
import com.cesi.carbonverification.domain.vo.ActivitySourceVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface EmsCarVerfActivityMapper extends BaseMapper<EmsCarVerfActivity> {

    Page<EmsCarVerfActivity> selectPage(
            Page<EmsCarVerfActivity> page,
            @Param("year") Integer year,
            @Param("orgUnit") String orgUnit,
            @Param("emissionSource") String emissionSource);

    List<EmsCarVerfActivity> selectByYearAndSource(
            @Param("year") Integer year,
            @Param("orgUnit") String orgUnit,
            @Param("emissionSource") String emissionSource);

    List<EmsCarVerfActivity> selectByYear(
            @Param("year") Integer year,
            @Param("orgUnit") String orgUnit);

    /** 按排放源汇总年度数据 */
    List<EmsCarVerfActivity> selectAnnualSummary(
            @Param("year") Integer year,
            @Param("orgUnit") String orgUnit);

    /** 查询指定月度记录（用于 upsert 判断） */
    EmsCarVerfActivity selectOneByKey(
            @Param("year") Integer year,
            @Param("month") Integer month,
            @Param("emissionSource") String emissionSource,
            @Param("orgUnit") String orgUnit);

    /** 按排放源聚合，含年总量和前两月样本数据（用于主列表页） */
    List<ActivitySourceVO> selectSourceList(
            @Param("year") Integer year,
            @Param("orgUnit") String orgUnit);
}
