package com.cesi.saving.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cesi.saving.domain.entity.Policy;
import com.cesi.saving.domain.dto.PolicyDTO;
import com.cesi.saving.domain.dto.PolicyPageDTO;
import com.cesi.saving.domain.vo.PolicyDetailVO;
import com.cesi.saving.domain.vo.PolicyPageVO;

/**
 * 政策法规Service接口
 *
 * @author Cesi
 */
public interface IPolicyService extends IService<Policy> {

    /**
     * 政策法规-列表查询
     */
    Page<PolicyPageVO> getPageList(PolicyPageDTO pageDTO);

    /**
     * 政策法规-查询详情
     */
    PolicyDetailVO getDetail(Long id);

    /**
     * 政策法规-新增
     */
    void add(PolicyDTO addDTO);

    /**
     * 政策法规-更新
     */
    void edit(PolicyDTO editDTO);

    /**
     * 政策法规-删除
     */
    void delete(Long id);
}
