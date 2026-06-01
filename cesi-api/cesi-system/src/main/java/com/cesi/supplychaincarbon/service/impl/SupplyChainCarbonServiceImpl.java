package com.cesi.supplychaincarbon.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cesi.common.utils.SecurityUtils;
import com.cesi.supplychaincarbon.domain.SupplyChainCarbon;
import com.cesi.supplychaincarbon.mapper.SupplyChainCarbonMapper;
import com.cesi.supplychaincarbon.service.ISupplyChainCarbonService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@AllArgsConstructor
public class SupplyChainCarbonServiceImpl implements ISupplyChainCarbonService {

    private final SupplyChainCarbonMapper supplyChainCarbonMapper;

    @Override
    public Page<SupplyChainCarbon> listPage(String supplierName, String materialCategory,
                                             long pageNum, long pageSize) {
        Page<SupplyChainCarbon> page = new Page<>(pageNum, pageSize);
        return supplyChainCarbonMapper.selectPage(page, supplierName, materialCategory);
    }

    @Override
    public int add(SupplyChainCarbon entity) {
        String operator = SecurityUtils.getUsername();
        Date now = new Date();
        entity.setCreateBy(operator);
        entity.setCreateTime(now);
        entity.setUpdateBy(operator);
        entity.setUpdateTime(now);
        return supplyChainCarbonMapper.insert(entity);
    }

    @Override
    public int edit(SupplyChainCarbon entity) {
        entity.setUpdateBy(SecurityUtils.getUsername());
        entity.setUpdateTime(new Date());
        return supplyChainCarbonMapper.updateById(entity);
    }

    @Override
    public int deleteById(Long id) {
        return supplyChainCarbonMapper.deleteById(id);
    }
}
