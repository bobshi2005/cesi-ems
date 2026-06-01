package com.cesi.basicSetup.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cesi.realtimedata.domain.SvgAttach;
import com.cesi.realtimedata.domain.SvgSetting;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 组态图Mapper接口
 *
 * @author cesi
 */
public interface SvgAttachMapper extends BaseMapper<SvgAttach> {

  void saveEquipmentFile(SvgAttach sysEquipmentfile);

  void saveSettingInfo(@Param("nodeId") String nodeId, @Param("svgInfo") List<SvgSetting> svgInfo);

  SvgAttach getConfigure(String nodeId);

  List<SvgSetting> getConfigureTag(String nodeId);
}
