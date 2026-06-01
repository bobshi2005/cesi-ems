package com.cesi.supplychaincarbon.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cesi.supplychaincarbon.domain.SupplyChainCarbon;

public interface ISupplyChainCarbonService {

    Page<SupplyChainCarbon> listPage(String supplierName, String materialCategory, long pageNum, long pageSize);

    int add(SupplyChainCarbon entity);

    int edit(SupplyChainCarbon entity);

    int deleteById(Long id);
}
