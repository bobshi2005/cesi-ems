package com.cesi.model.domain;

import com.cesi.common.enums.PointType;
import lombok.Data;

@Data
public class EnergyIndexQuery {

  private String nodeId;
  private String codeOrName;
  private String indexCategory;
  private PointType pointType;

  public EnergyIndexQuery() {
  }

  public EnergyIndexQuery(String nodeId, String codeOrName, String indexCategory,
      PointType energyType) {
    this.nodeId = nodeId;
    this.codeOrName = codeOrName;
    this.indexCategory = indexCategory;
    this.pointType = energyType;
  }
}
