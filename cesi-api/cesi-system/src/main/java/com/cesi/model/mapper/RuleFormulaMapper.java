package com.cesi.model.mapper;

import com.cesi.model.domain.RuleFormula;
import com.cesi.model.domain.RuleFormulaParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RuleFormulaMapper {

    void insertIndexFormula(RuleFormula ruleFormula);

    void updateIndexFormula(RuleFormula ruleFormula);

    void saveIndexFormulaParam(@Param("pointId") String pointId, @Param("ruleFormulaParams") List<RuleFormulaParam> ruleFormulaParams);

    RuleFormula getFormula(String indexId);

    List<RuleFormulaParam> getFormulaParam(String indexId);
}
