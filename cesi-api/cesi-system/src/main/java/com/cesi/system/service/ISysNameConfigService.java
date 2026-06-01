package com.cesi.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cesi.system.domain.SysNameConfig;

/**
 * 系统名称配置Service接口
 *
 * @author Cesi
 */
public interface ISysNameConfigService extends IService<SysNameConfig> {

    SysNameConfig getSysNameConfig();
}
