package com.cesi.web.controller.singlelogin;

import org.springframework.beans.factory.annotation.Autowired;
import com.cesi.common.annotation.Anonymous;
import com.cesi.common.constant.Constants;
import com.cesi.common.core.domain.AjaxResult;
import com.cesi.common.core.domain.entity.SysUser;
import com.cesi.common.utils.StringUtils;
import com.cesi.framework.web.service.SysLoginService;
import com.cesi.singlelogin.service.ISingleLoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

/**
 * 单点登录接口对接
 *
 * @author cesi
 */
@Slf4j
@RestController
public class SingleLoginController {
    @Autowired
    private ISingleLoginService singleLoginService;
    @Autowired
    private SysLoginService loginService;

    /**
     * 登录方法
     *
     * @param token 登录信息
     * @return 结果
     */
    @Anonymous
    @GetMapping("/singleLogin")
    public AjaxResult singleLogin(@RequestParam String token) {
        AjaxResult ajax = AjaxResult.success();
        if (StringUtils.isEmpty(token)) {
            return AjaxResult.error("token不能为空");
        }
        SysUser user = singleLoginService.singleLogin(token);
        if (null == user) {
            return AjaxResult.error("登录失败");
        }
        // 生成令牌
        String newToken = loginService.loginNoCode(user.getUserName());
        ajax.put(Constants.TOKEN, newToken);
        return ajax;
    }

}
