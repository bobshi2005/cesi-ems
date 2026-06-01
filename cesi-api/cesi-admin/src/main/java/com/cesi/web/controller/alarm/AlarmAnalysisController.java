package com.cesi.web.controller.alarm;

import com.cesi.alarm.domain.dto.AlarmAnalysisDTO;
import com.cesi.alarm.domain.vo.AlarmAnalysisVO;
import com.cesi.alarm.services.IAlarmAnalysisService;
import com.cesi.common.core.controller.BaseController;
import com.cesi.common.core.domain.AjaxResult;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
* description 报警分析相关功能
 *
 * @author cesi
 */
@RestController
@AllArgsConstructor
@Api(tags = "报警分析相关功能")
@RequestMapping("/alarmAnalysis")
public class AlarmAnalysisController extends BaseController {

    @Resource
    private IAlarmAnalysisService alarmAnalysisService;

    @GetMapping("/getByNodeId")
    public AjaxResult getByNodeId(@Validated AlarmAnalysisDTO alarmAnalysisDTO){
        AlarmAnalysisVO alarmAnalysisVO  = alarmAnalysisService.getByNodeId(alarmAnalysisDTO);
        return AjaxResult.success(alarmAnalysisVO);
    }

    @GetMapping("/getCountInfo")
    public AjaxResult getCountInfo(@Validated AlarmAnalysisDTO alarmAnalysisDTO){

        if(ObjectUtils.isEmpty(alarmAnalysisDTO.getModelCode())){
            return AjaxResult.error("模型编码不能为空");
        }
        AlarmAnalysisVO alarmAnalysisVO  = alarmAnalysisService.getCountInfo(alarmAnalysisDTO);
        return AjaxResult.success(alarmAnalysisVO);
    }
}
