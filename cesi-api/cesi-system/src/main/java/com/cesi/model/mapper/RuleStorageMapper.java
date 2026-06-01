package com.cesi.model.mapper;

import com.cesi.common.enums.CalcType;
import com.cesi.common.enums.TimeType;
import com.cesi.model.domain.RuleStorage;
import com.cesi.model.domain.IndexStorageParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RuleStorageMapper {

  void insertIndexStorage(RuleStorage storage);

  void updateIndexStorage(RuleStorage storage);

  List<RuleStorage> getIndexStorage(String indexId);

  void saveParams(@Param("storageId")String storageId, @Param("parameterIds")List<String> parameterIds);

  List<IndexStorageParam> getAllParameter();

  List<RuleStorage> getAllCalcIndexStorage(CalcType calc);

  RuleStorage getWithTimetype(@Param("indexId") String indexId, @Param("timeType") TimeType timeType);
}
