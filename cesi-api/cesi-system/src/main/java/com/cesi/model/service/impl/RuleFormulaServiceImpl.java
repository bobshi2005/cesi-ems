package com.cesi.model.service.impl;

import com.cesi.common.utils.StringUtils;
import com.cesi.common.utils.uuid.UUID;
import com.cesi.model.domain.RuleFormula;
import com.cesi.model.domain.RuleFormulaParam;
import com.cesi.model.mapper.RuleFormulaMapper;
import com.cesi.model.service.RuleFormulaService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author cesi
 */
@Service
@AllArgsConstructor
public class RuleFormulaServiceImpl implements RuleFormulaService {

  private RuleFormulaMapper ruleFormulaMapper;

  @Override
  public void saveIndexFormula(RuleFormula ruleFormula) {
    if (StringUtils.isEmpty(ruleFormula.getId())) {
      ruleFormula.setId(UUID.fastUUID().toString());
      ruleFormulaMapper.insertIndexFormula(ruleFormula);
    } else {
      ruleFormulaMapper.updateIndexFormula(ruleFormula);
    }

    ruleFormula.getIndexFormulaParams().forEach(param -> {
      param.setId(UUID.fastUUID().toString());
      param.setFormulaId(ruleFormula.getId());
      param.setPointId(ruleFormula.getPointId());
    });
    ruleFormulaMapper
        .saveIndexFormulaParam(ruleFormula.getPointId(), ruleFormula.getIndexFormulaParams());
  }

  @Override
  public RuleFormula getIndexFormula(String indexId) {
    RuleFormula ruleFormula = ruleFormulaMapper.getFormula(indexId);
    if (ruleFormula != null) {
      List<RuleFormulaParam> ruleFormulaParams = ruleFormulaMapper.getFormulaParam(indexId);
      if (!ruleFormulaParams.isEmpty()) {
        ruleFormula.setIndexFormulaParams(ruleFormulaParams);
      }
    } else {
      ruleFormula = new RuleFormula();
    }

    return ruleFormula;
  }

}
