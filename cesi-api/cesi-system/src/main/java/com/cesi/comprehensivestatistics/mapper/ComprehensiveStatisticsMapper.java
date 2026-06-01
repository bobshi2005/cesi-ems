package com.cesi.comprehensivestatistics.mapper;

import com.cesi.basicdata.domain.Device;
import com.cesi.common.enums.TimeType;
import com.cesi.comprehensivestatistics.domain.ComprehensiveStatistics;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface ComprehensiveStatisticsMapper {
    List<Device> getFacilityArchives();
    List<ComprehensiveStatistics> getDatasByList(@Param("indexIds") List<String> indexIds, @Param("beginTime") Date beginTime,
                                                 @Param("endTime") Date endTime,
                                                 @Param("timeType") TimeType timeType);
    List<ComprehensiveStatistics> getDatasByIndex(@Param("indexIds") List<String> indexIds, @Param("timeCode") String timeCode);
    List<ComprehensiveStatistics> getDatasIndex(@Param("indexIds") List<String> indexIds,@Param("timeCode") String timeCode);
    List<ComprehensiveStatistics> getEnergyByIndex(@Param("indexType") String indexType);

}
