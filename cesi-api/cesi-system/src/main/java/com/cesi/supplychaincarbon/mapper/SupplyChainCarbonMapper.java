package com.cesi.supplychaincarbon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cesi.supplychaincarbon.domain.SupplyChainCarbon;
import org.apache.ibatis.annotations.Param;

public interface SupplyChainCarbonMapper extends BaseMapper<SupplyChainCarbon> {

    Page<SupplyChainCarbon> selectPage(
            Page<SupplyChainCarbon> page,
            @Param("supplierName") String supplierName,
            @Param("materialCategory") String materialCategory);
}
