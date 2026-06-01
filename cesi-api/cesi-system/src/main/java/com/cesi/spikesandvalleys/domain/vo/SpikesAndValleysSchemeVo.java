package com.cesi.spikesandvalleys.domain.vo;

import com.cesi.spikesandvalleys.domain.SpikesAndValleysItem;
import com.cesi.spikesandvalleys.domain.SpikesAndValleysScheme;
import lombok.Data;

import java.util.List;

/**
 * 尖峰平谷时间段明细对象 spikes_and_valleys_scheme
 *
 * @author Cesi
 */
@Data
public class SpikesAndValleysSchemeVo extends SpikesAndValleysScheme {
    List<SpikesAndValleysItem> itemList;
}
