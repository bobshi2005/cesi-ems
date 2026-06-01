package com.cesi.web.controller.carbonemission;

import com.cesi.carbonemission.domain.dto.CarbonEmissionDTO;
import com.cesi.carbonemission.domain.vo.carbonEmissionYQVO;
import com.cesi.carbonemission.service.ICarbonEmissionService;
import com.cesi.common.core.controller.BaseController;
import com.cesi.common.core.domain.AjaxResult;
import com.cesi.common.utils.poi.ExcelUtil;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 碳排放核算Controller
 *
 * @author cesi
 */
@Slf4j
@RestController
@AllArgsConstructor
@Api(tags = "碳排核算")
@RequestMapping("/carbonEmission")
public class CarbonEmissionController extends BaseController {
    private ICarbonEmissionService carbonEmissionService;

    @GetMapping("/up")
    public AjaxResult up(CarbonEmissionDTO carbonEmissionDTO) {
        final Map<String, Object> upCarbonEmission = carbonEmissionService.getUpCarbonEmission(carbonEmissionDTO);
        return AjaxResult.success(upCarbonEmission);
    }

    @GetMapping("/middle")
    public AjaxResult middle(CarbonEmissionDTO carbonEmissionDTO) {
        final List<carbonEmissionYQVO> carbonEmissionYQVO = carbonEmissionService.getMiddleCarbonEmission(carbonEmissionDTO);
        return AjaxResult.success(carbonEmissionYQVO);
    }

    @PostMapping("/export")
    @ResponseBody
    public void export(HttpServletResponse response, CarbonEmissionDTO carbonEmissionDTO) {
        List<carbonEmissionYQVO> carbonEmissionYQVO = carbonEmissionService.getMiddleCarbonEmission(carbonEmissionDTO);
        ExcelUtil<carbonEmissionYQVO> util = new ExcelUtil<carbonEmissionYQVO>(carbonEmissionYQVO.class);
        util.exportExcel(response, carbonEmissionYQVO, "碳排放量同环比");
    }
}
