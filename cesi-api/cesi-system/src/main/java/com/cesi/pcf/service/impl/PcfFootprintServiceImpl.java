package com.cesi.pcf.service.impl;

import com.cesi.common.utils.SecurityUtils;
import com.cesi.pcf.domain.*;
import com.cesi.pcf.domain.vo.LifecycleVO;
import com.cesi.pcf.mapper.*;
import com.cesi.pcf.service.IPcfFootprintService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@AllArgsConstructor
public class PcfFootprintServiceImpl implements IPcfFootprintService {

    private final EmspcfProductMapper      productMapper;
    private final EmspcfRawMaterialMapper  rawMaterialMapper;
    private final EmspcfManufacturingMapper manufacturingMapper;
    private final EmspcfLogisticsMapper    logisticsMapper;
    private final EmspcfProductUseMapper   productUseMapper;
    private final EmspcfEolMapper          eolMapper;
    private final EmspcfGreenCertMapper    greenCertMapper;

    @Override
    public LifecycleVO getLifecycleOverview(Long productId, Integer year) {
        LifecycleVO vo = new LifecycleVO();
        vo.setProductId(productId);
        vo.setYear(year);
        vo.setProduct(productMapper.selectById(productId));

        BigDecimal raw  = rawMaterialMapper.sumByProductAndYear(productId, year);
        BigDecimal mfg  = manufacturingMapper.sumByProductAndYear(productId, year);
        BigDecimal log  = logisticsMapper.sumByProductAndYear(productId, year);
        BigDecimal use  = productUseMapper.sumByProductAndYear(productId, year);
        BigDecimal eol  = eolMapper.sumByProductAndYear(productId, year);
        BigDecimal cert = greenCertMapper.sumOffsetByProductAndYear(productId, year);

        vo.setRawMaterialTotal(raw);
        vo.setManufacturingTotal(mfg);
        vo.setLogisticsTotal(log);
        vo.setProductUseTotal(use);
        vo.setEolTotal(eol);
        vo.setGreenCertOffset(cert);

        BigDecimal total = raw.add(mfg).add(log).add(use).add(eol);
        vo.setTotalFootprint(total.setScale(4, RoundingMode.HALF_UP));
        vo.setNetFootprint(total.subtract(cert).setScale(4, RoundingMode.HALF_UP));

        // 组装阶段列表（用于前端进度条）
        List<LifecycleVO.StageItem> stages = new ArrayList<>();
        stages.add(buildStage("原材料获取", raw, total, "badge-blue"));
        stages.add(buildStage("生产制造",   mfg, total, "badge-orange"));
        stages.add(buildStage("物流/仓储/运输", log, total, "badge-yellow"));
        stages.add(buildStage("产品使用",   use, total, "badge-purple"));
        stages.add(buildStage("废弃回收",   eol, total, "badge-red"));
        vo.setStages(stages);

        return vo;
    }

    private LifecycleVO.StageItem buildStage(String name, BigDecimal amount,
                                              BigDecimal total, String badge) {
        LifecycleVO.StageItem s = new LifecycleVO.StageItem();
        s.setStageName(name);
        s.setAmount(amount.setScale(4, RoundingMode.HALF_UP));
        s.setBadgeClass(badge);
        if (total.compareTo(BigDecimal.ZERO) > 0) {
            s.setPercentage(amount.divide(total, 6, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP));
        } else {
            s.setPercentage(BigDecimal.ZERO);
        }
        return s;
    }

    /* ─── 原材料 ─── */

    @Override
    public List<EmspcfRawMaterial> listRawMaterial(Long productId, Integer year) {
        return rawMaterialMapper.selectByProductAndYear(productId, year);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRawMaterial(EmspcfRawMaterial entity) {
        // 自动计算碳足迹：emission_amount = quantity × emission_factor / 1000 (kg→t)
        if (entity.getQuantity() != null && entity.getEmissionFactor() != null) {
            BigDecimal amount = entity.getQuantity()
                    .multiply(entity.getEmissionFactor())
                    .divide(BigDecimal.valueOf(1000), 4, RoundingMode.HALF_UP);
            entity.setEmissionAmount(amount);
        }
        fillAudit(entity);
        if (entity.getId() == null) rawMaterialMapper.insert(entity);
        else rawMaterialMapper.updateById(entity);
    }

    @Override
    public void removeRawMaterial(Long id) { rawMaterialMapper.deleteById(id); }

    /* ─── 生产制造 ─── */

    @Override
    public List<EmspcfManufacturing> listManufacturing(Long productId, Integer year) {
        return manufacturingMapper.selectByProductAndYear(productId, year);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveManufacturing(EmspcfManufacturing entity) {
        fillAudit(entity);
        if (entity.getId() == null) manufacturingMapper.insert(entity);
        else manufacturingMapper.updateById(entity);
    }

    @Override
    public void removeManufacturing(Long id) { manufacturingMapper.deleteById(id); }

    /* ─── 物流运输 ─── */

    @Override
    public List<EmspcfLogistics> listLogistics(Long productId, Integer year) {
        return logisticsMapper.selectByProductAndYear(productId, year);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveLogistics(EmspcfLogistics entity) {
        fillAudit(entity);
        if (entity.getId() == null) logisticsMapper.insert(entity);
        else logisticsMapper.updateById(entity);
    }

    @Override
    public void removeLogistics(Long id) { logisticsMapper.deleteById(id); }

    /* ─── 产品使用 ─── */

    @Override
    public List<EmspcfProductUse> listProductUse(Long productId, Integer year) {
        return productUseMapper.selectByProductAndYear(productId, year);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveProductUse(EmspcfProductUse entity) {
        fillAudit(entity);
        if (entity.getId() == null) productUseMapper.insert(entity);
        else productUseMapper.updateById(entity);
    }

    @Override
    public void removeProductUse(Long id) { productUseMapper.deleteById(id); }

    /* ─── 废弃回收 ─── */

    @Override
    public List<EmspcfEol> listEol(Long productId, Integer year) {
        return eolMapper.selectByProductAndYear(productId, year);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveEol(EmspcfEol entity) {
        fillAudit(entity);
        if (entity.getId() == null) eolMapper.insert(entity);
        else eolMapper.updateById(entity);
    }

    @Override
    public void removeEol(Long id) { eolMapper.deleteById(id); }

    /* ─── 绿电绿证 ─── */

    @Override
    public List<EmspcfGreenCert> listGreenCert(Long productId, Integer year) {
        return greenCertMapper.selectByProductAndYear(productId, year);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveGreenCert(EmspcfGreenCert entity) {
        // 自动计算减排量 = purchase_volume × emission_factor
        if (entity.getPurchaseVolume() != null && entity.getEmissionFactor() != null) {
            entity.setEmissionReduction(
                    entity.getPurchaseVolume()
                            .multiply(entity.getEmissionFactor())
                            .setScale(4, RoundingMode.HALF_UP));
            entity.setConvertedPower(
                    entity.getPurchaseVolume().multiply(BigDecimal.valueOf(1000)));
        }
        fillAudit(entity);
        if (entity.getId() == null) greenCertMapper.insert(entity);
        else greenCertMapper.updateById(entity);
    }

    @Override
    public void removeGreenCert(Long id) { greenCertMapper.deleteById(id); }

    /* ─── 工具方法 ─── */

    private void fillAudit(com.cesi.common.core.domain.BaseEntity e) {
        String user = SecurityUtils.getUsername();
        Date now = new Date();
        if (e.getCreateBy() == null) {
            e.setCreateBy(user);
            e.setCreateTime(now);
        }
        e.setUpdateBy(user);
        e.setUpdateTime(now);
    }
}
