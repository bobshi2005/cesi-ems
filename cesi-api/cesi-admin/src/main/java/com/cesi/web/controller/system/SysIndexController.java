package com.cesi.web.controller.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.cesi.common.config.BaseConfig;
import com.cesi.common.utils.StringUtils;

import jakarta.annotation.Resource;

/**
 * 首页
 *
 * @author cesi
 */
@RestController
public class SysIndexController
{
    /** 系统基础配置 */
    @Resource
    @Autowired
    private BaseConfig baseConfig;

    /**
     * 访问首页，提示语
     */
    @RequestMapping("/")
    public String index()
    {
        return StringUtils.format("欢迎使用{}后台管理框架，当前版本：v{}，请通过前端地址访问。", baseConfig.getName(), baseConfig.getVersion());
    }
}
