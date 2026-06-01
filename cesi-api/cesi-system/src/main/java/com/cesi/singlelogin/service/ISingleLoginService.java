package com.cesi.singlelogin.service;

import com.cesi.common.core.domain.entity.SysUser;

/**
 * description todu
 *
 * @author cesi
 */
public interface ISingleLoginService {
    SysUser singleLogin(String token);
}
