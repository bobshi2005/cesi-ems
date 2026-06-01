package com.cesi.carbonasset.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cesi.carbonasset.domain.CarbonAsset;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CarbonAssetMapper extends BaseMapper<CarbonAsset> {

    List<CarbonAsset> selectByYearAndType(@Param("year") Integer year, @Param("assetType") String assetType);

    /** 初始化插入：记录已存在则跳过，不覆盖用户数据 */
    void initAsset(CarbonAsset asset);

    /** 保存更新：存在则更新 quantity */
    void upsertAsset(CarbonAsset asset);
}
