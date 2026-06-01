package com.cesi.alarm.services;

import com.cesi.alarm.domain.dto.AlarmAnalysisDTO;
import com.cesi.alarm.domain.vo.AlarmAnalysisVO;

/**
 * 报警分析接口
 *
 * @author cesi
 */
public interface IAlarmAnalysisService {

    AlarmAnalysisVO getByNodeId(AlarmAnalysisDTO alarmAnalysisDTO);

    /**
     * 报警分析统计
     *
     * @param alarmAnalysisDTO
     * @return
     */
    AlarmAnalysisVO getCountInfo(AlarmAnalysisDTO alarmAnalysisDTO);
}
