package com.cesi.web.controller.system;

import org.springframework.beans.factory.annotation.Autowired;
import com.cesi.common.annotation.Anonymous;
import com.cesi.common.annotation.Log;
import com.cesi.common.core.controller.BaseController;
import com.cesi.common.core.domain.AjaxResult;
import com.cesi.common.enums.BusinessType;
import com.cesi.system.domain.SysNameConfig;
import com.cesi.system.service.ISysNameConfigService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * 系统名称配置Controller
 *
 * @author cesi
 */
@RestController
@RequestMapping("/system/nameconfig")
public class SysNameConfigController extends BaseController {
    @Resource
    @Autowired
    private ISysNameConfigService sysNameConfigService;


    /**
     * 新增系统名称配置
     */
    @Log(title = "系统名称配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult save(@RequestBody SysNameConfig sysNameConfig) {
        return toAjax(sysNameConfigService.saveOrUpdate(sysNameConfig));
    }

    /**
     * 新增系统名称配置
     */
    @GetMapping
    @Anonymous
    public AjaxResult getSysNameConfig() {
        return AjaxResult.success(sysNameConfigService.getSysNameConfig());
    }

}
