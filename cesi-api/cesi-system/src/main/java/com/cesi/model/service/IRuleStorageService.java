package com.cesi.model.service;


import com.cesi.common.enums.TimeType;
import com.cesi.model.domain.RuleFormula;
import com.cesi.model.domain.RuleStorage;

import java.util.List;

/**
 * @author cesi
 */
public interface IRuleStorageService {

  void saveIndexStorage(String indexId, List<RuleStorage> ruleStorage);

  void saveFormulaAndStorage(RuleFormula ruleFormula, List<RuleStorage> ruleStorage,
                             String indexId);

  List<RuleStorage> getIndexStorage(String indexId);

  List<RuleStorage> getAllCalcIndexStorage();

  RuleStorage getIndexStorage(String indexId, TimeType timeType);
}
