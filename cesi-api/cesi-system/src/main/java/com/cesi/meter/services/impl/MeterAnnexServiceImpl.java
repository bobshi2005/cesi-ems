package com.cesi.meter.services.impl;

import com.cesi.meter.domain.MeterAnnex;
import com.cesi.meter.mapper.MeterAnnexMapper;
import com.cesi.meter.services.IMeterAnnexService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 计量器具档案附件Service业务层处理
 *
 * @author cesi
 */
@Service
@AllArgsConstructor
public class MeterAnnexServiceImpl implements IMeterAnnexService {
    private MeterAnnexMapper meterAnnexMapper;

    /**
     * 查询计量器具档案附件列表
     *
     * @param meterAnnex 计量器具档案附件
     * @return 计量器具档案附件
     */
    @Override
    public List<MeterAnnex> selectMeterAnnexList(MeterAnnex meterAnnex) {
        return meterAnnexMapper.selectMeterAnnexList(meterAnnex);
    }
}
