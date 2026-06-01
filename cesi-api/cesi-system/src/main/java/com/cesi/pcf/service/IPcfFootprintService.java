package com.cesi.pcf.service;

import com.cesi.pcf.domain.*;
import com.cesi.pcf.domain.vo.LifecycleVO;
import java.util.List;

public interface IPcfFootprintService {

    LifecycleVO getLifecycleOverview(Long productId, Integer year);

    /* 原材料 */
    List<EmspcfRawMaterial> listRawMaterial(Long productId, Integer year);
    void saveRawMaterial(EmspcfRawMaterial entity);
    void removeRawMaterial(Long id);

    /* 生产制造 */
    List<EmspcfManufacturing> listManufacturing(Long productId, Integer year);
    void saveManufacturing(EmspcfManufacturing entity);
    void removeManufacturing(Long id);

    /* 物流运输 */
    List<EmspcfLogistics> listLogistics(Long productId, Integer year);
    void saveLogistics(EmspcfLogistics entity);
    void removeLogistics(Long id);

    /* 产品使用 */
    List<EmspcfProductUse> listProductUse(Long productId, Integer year);
    void saveProductUse(EmspcfProductUse entity);
    void removeProductUse(Long id);

    /* 废弃回收 */
    List<EmspcfEol> listEol(Long productId, Integer year);
    void saveEol(EmspcfEol entity);
    void removeEol(Long id);

    /* 绿电绿证 */
    List<EmspcfGreenCert> listGreenCert(Long productId, Integer year);
    void saveGreenCert(EmspcfGreenCert entity);
    void removeGreenCert(Long id);
}
