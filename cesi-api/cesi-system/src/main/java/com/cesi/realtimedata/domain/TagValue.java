package com.cesi.realtimedata.domain;

import com.cesi.common.enums.Quality;
import com.cesi.common.utils.DateUtils;
import lombok.Data;

import java.util.Date;

/**
 * @author cesi
 *
 * 测点数据实体类.
 */
@Data
public class TagValue {

  /**
   * 测点编码.
   */
  private String tagCode;

  /**
   * 测点数据时间.
   */
  private Date dataTime;

  /**
   * 测点值.
   */
  private Double value;

  /**
   * 测点数据质量.
   */
  private Quality quality;

  /**
   * 详细的质量编码.
   */
  private Integer qualityDetail;

  private String showDataTime;

  public void setShowDataTime(String format) {
    this.showDataTime = DateUtils.parseDateToStr(format,this.getDataTime());;
  }
  @Override
  public String toString() {
    return "TagValue{" +
        "tagCode='" + tagCode + '\'' +
        ", dataTime=" + dataTime +
        ", value=" + value +
        ", quality=" + quality +
        ", qualityDetail=" + qualityDetail +
        '}';
  }
}
