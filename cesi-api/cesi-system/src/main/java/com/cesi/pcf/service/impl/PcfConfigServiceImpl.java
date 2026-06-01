package com.cesi.pcf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cesi.common.utils.SecurityUtils;
import com.cesi.pcf.domain.EmspcfAccountingParams;
import com.cesi.pcf.domain.EmspcfEmissionFactor;
import com.cesi.pcf.domain.EmspcfGreenCertFactor;
import com.cesi.pcf.mapper.EmspcfAccountingParamsMapper;
import com.cesi.pcf.mapper.EmspcfEmissionFactorMapper;
import com.cesi.pcf.mapper.EmspcfGreenCertFactorMapper;
import com.cesi.pcf.service.IPcfConfigService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PcfConfigServiceImpl implements IPcfConfigService {

    private final EmspcfEmissionFactorMapper    factorMapper;
    private final EmspcfAccountingParamsMapper  paramsMapper;
    private final EmspcfGreenCertFactorMapper   certFactorMapper;

    @Override
    public List<EmspcfEmissionFactor> listEmissionFactors(String factorCategory) {
        if (factorCategory != null && !factorCategory.isEmpty()) {
            return factorMapper.selectByCategory(factorCategory);
        }
        return factorMapper.selectList(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveEmissionFactor(EmspcfEmissionFactor entity) {
        String user = SecurityUtils.getUsername();
        Date now = new Date();
        if (entity.getId() == null) {
            entity.setIsBuiltin("0");
            entity.setCreateBy(user);
            entity.setCreateTime(now);
            factorMapper.insert(entity);
        } else {
            entity.setUpdateBy(user);
            entity.setUpdateTime(now);
            factorMapper.updateById(entity);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeEmissionFactor(Long id) {
        EmspcfEmissionFactor factor = factorMapper.selectById(id);
        if (factor != null && "1".equals(factor.getIsBuiltin())) {
            throw new RuntimeException("内置排放因子不允许删除");
        }
        factorMapper.deleteById(id);
    }

    @Override
    public Map<String, String> getAccountingParams() {
        return paramsMapper.selectList(null).stream()
                .collect(Collectors.toMap(
                        EmspcfAccountingParams::getParamKey,
                        p -> p.getParamValue() == null ? "" : p.getParamValue()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAccountingParams(List<EmspcfAccountingParams> params) {
        String user = SecurityUtils.getUsername();
        Date now = new Date();
        for (EmspcfAccountingParams p : params) {
            p.setUpdateBy(user);
            p.setUpdateTime(now);
            paramsMapper.upsertParam(p);
        }
    }

    @Override
    public List<EmspcfGreenCertFactor> listGreenCertFactors() {
        return certFactorMapper.selectList(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveGreenCertFactor(EmspcfGreenCertFactor entity) {
        String user = SecurityUtils.getUsername();
        Date now = new Date();
        if (entity.getId() == null) {
            entity.setCreateBy(user);
            entity.setCreateTime(now);
            certFactorMapper.insert(entity);
        } else {
            entity.setUpdateBy(user);
            entity.setUpdateTime(now);
            certFactorMapper.updateById(entity);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeGreenCertFactor(Long id) {
        certFactorMapper.deleteById(id);
    }
}
