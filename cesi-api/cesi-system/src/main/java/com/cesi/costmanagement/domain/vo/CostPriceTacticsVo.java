package com.cesi.costmanagement.domain.vo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.cesi.common.annotation.Excel;
import com.cesi.common.core.domain.BaseEntity;
import com.cesi.costmanagement.domain.CostPriceTactics;
import com.cesi.costmanagement.domain.CostPriceTacticsItem;
import com.cesi.spikesandvalleys.domain.SpikesAndValleysItem;
import lombok.Data;

import java.util.List;

/**
 * 成本策略对象 vo
 *
 *
 */
@Data
public class CostPriceTacticsVo extends CostPriceTactics {
    List<CostPriceTacticsItem> itemList;

}
