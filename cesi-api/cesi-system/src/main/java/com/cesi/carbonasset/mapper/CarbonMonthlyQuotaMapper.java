package com.cesi.carbonasset.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cesi.carbonasset.domain.CarbonMonthlyQuota;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CarbonMonthlyQuotaMapper extends BaseMapper<CarbonMonthlyQuota> {

    List<CarbonMonthlyQuota> selectByYear(@Param("year") Integer year);

    /** 初始化插入：记录已存在则跳过，不覆盖用户数据 */
    void initMonthlyQuota(CarbonMonthlyQuota quota);

    /** 保存更新：存在则更新 monthly_quota */
    void upsertMonthlyQuota(CarbonMonthlyQuota quota);
}
