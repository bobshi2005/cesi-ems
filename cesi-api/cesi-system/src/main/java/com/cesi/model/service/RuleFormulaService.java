package com.cesi.model.service;


import com.cesi.model.domain.RuleFormula;

public interface RuleFormulaService {

  void saveIndexFormula(RuleFormula ruleFormula);

  RuleFormula getIndexFormula(String indexId);
}
