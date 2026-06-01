package com.cesi.model.domain;

import com.cesi.common.enums.CalcType;
import com.cesi.common.enums.TimeType;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author cesi
 */
@Data
public class RuleStorage implements Serializable {

  private String id;
  private String pointId;
  private TimeType timeType;
  private CalcType calcType;
  private String calcText;
  private Date createTime;
  private Date updateTime;
  private Integer isPvCalc;
  private List<String> paramIndex = new ArrayList<>();

}
