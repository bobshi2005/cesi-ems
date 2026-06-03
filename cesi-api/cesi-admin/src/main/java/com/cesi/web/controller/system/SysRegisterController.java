package com.cesi.web.controller.system;

import org.springframework.beans.factory.annotation.Autowired;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.cesi.common.core.controller.BaseController;
import com.cesi.common.core.domain.AjaxResult;
import com.cesi.common.core.domain.model.RegisterBody;
import com.cesi.common.utils.StringUtils;
import com.cesi.framework.web.service.SysRegisterService;
import com.cesi.system.service.ISysConfigService;

/**
 * 注册验证
 * 
 * @author cesi
 */
@RestController
public class SysRegisterController extends BaseController
{
    @Resource
    @Autowired
    private SysRegisterService registerService;

    @Resource
    @Autowired
    private ISysConfigService configService;

    @PostMapping("/register")
    public AjaxResult register(@RequestBody RegisterBody user)
    {
        if (!("true".equals(configService.selectConfigByKey("sys.account.registerUser"))))
        {
            return error("当前系统没有开启注册功能！");
        }
        String msg = registerService.register(user);
        return StringUtils.isEmpty(msg) ? success() : error(msg);
    }
}
