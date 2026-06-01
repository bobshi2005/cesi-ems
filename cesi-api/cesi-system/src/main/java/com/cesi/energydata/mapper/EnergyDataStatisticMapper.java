package com.cesi.energydata.mapper;

import com.cesi.model.domain.vo.ModelNodePointInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface EnergyDataStatisticMapper
{
    List<ModelNodePointInfo> getModelNodeIndexIdByFixedNodeIds(@Param("modelCode")String modelCode, @Param("fixedNodeIds") List<String> fixedNodeIds);
}
