package com.cesi.carbonverification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cesi.carbonverification.domain.EmsCarVerfVoucher;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface EmsCarVerfVoucherMapper extends BaseMapper<EmsCarVerfVoucher> {

    Page<EmsCarVerfVoucher> selectPage(
            Page<EmsCarVerfVoucher> page,
            @Param("year") Integer year,
            @Param("month") Integer month,
            @Param("orgUnit") String orgUnit,
            @Param("emissionSource") String emissionSource,
            @Param("voucherType") String voucherType,
            @Param("voucherName") String voucherName);

    List<EmsCarVerfVoucher> selectByYearAndSource(
            @Param("year") Integer year,
            @Param("orgUnit") String orgUnit,
            @Param("emissionSource") String emissionSource);

    /** 统计各月已上传凭证数（用于热力矩阵） */
    List<EmsCarVerfVoucher> selectUploadedMonths(
            @Param("year") Integer year,
            @Param("orgUnit") String orgUnit);

    List<EmsCarVerfVoucher> selectByYear(
            @Param("year") Integer year,
            @Param("orgUnit") String orgUnit);
}
