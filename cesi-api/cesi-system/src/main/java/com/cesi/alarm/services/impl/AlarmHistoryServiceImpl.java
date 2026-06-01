package com.cesi.alarm.services.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cesi.alarm.domain.JkHistoryAlarm;
import com.cesi.alarm.domain.entity.AlarmHistory;
import com.cesi.alarm.mapper.AlarmHistoryMapper;
import com.cesi.alarm.services.IAlarmHistoryService;
import com.cesi.common.enums.TimeType;
import com.cesi.common.utils.DateUtils;
import com.cesi.common.utils.PageUtils;
import com.cesi.common.utils.StringUtils;
import com.cesi.model.domain.MeterPoint;
import com.cesi.model.domain.ModelNode;
import com.cesi.model.domain.NodePoint;
import com.cesi.model.domain.vo.ModelNodePointInfo;
import com.cesi.model.mapper.MeterPointMapper;
import com.cesi.model.mapper.ModelNodeMapper;
import com.cesi.model.mapper.NodePointMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AlarmHistoryServiceImpl implements IAlarmHistoryService {

    private AlarmHistoryMapper alarmHistoryMapper;
    private ModelNodeMapper modelNodeMapper;
    private NodePointMapper nodePointMapper;
    private MeterPointMapper meterPointMapper;


    @Override
    public List<AlarmHistory> getHistoryAlarm(Date beginTime, Date endTime) {
        return null;
    }

    @Override
    public List<AlarmHistory> getHistoryAlarm(Date beginTime, Date endTime, TimeType timeType) {
        return null;
    }

    @Override
    public List<AlarmHistory> getHistoryAlarm(Date beginTime, Date endTime, String alarmType) {
        return null;
    }

    @Override
    public List<JkHistoryAlarm> selectJkHistoryAlarmList(JkHistoryAlarm jkHistoryAlarm) {
        return alarmHistoryMapper.selectJkHistoryAlarmList(jkHistoryAlarm);
    }

    @Override
    public List<JkHistoryAlarm> selectJkHistoryAlarmListExcel(JkHistoryAlarm jkHistoryAlarm) {
        return alarmHistoryMapper.selectJkHistoryAlarmListExcel(jkHistoryAlarm);
    }

    @Override
    public void updateHistoryAlarm(String alarmCode, AlarmHistory alarmHistory) {
        alarmHistoryMapper.updateHistoryAlarm(alarmCode, alarmHistory);
    }

    /**
     * 获取历史报警分页数据
     *
     * @param historyAlarm
     * @return
     */
    @Override
    public Page<JkHistoryAlarm> selectHistoryAlarmPageList(JkHistoryAlarm historyAlarm) {
        Page<JkHistoryAlarm> pageInfo = PageUtils.getPageInfo(JkHistoryAlarm.class);

        List<String> indexIdList = new ArrayList<>();
        if ("ALL".equals(historyAlarm.getEierarchyFlag())) {

            ModelNode modelNode = modelNodeMapper.selectModelNodeById(historyAlarm.getNodeId());
            List<ModelNodePointInfo> modelNodePointInfoList =
                    modelNodeMapper.getAllModelNodeIndexByAddress(modelNode.getModelCode(), modelNode.getAddress());
            if (StringUtils.isNotEmpty(historyAlarm.getIndexName())) {
                modelNodePointInfoList = modelNodePointInfoList.stream()
                        .filter(modelNodeIndexInfo -> modelNodeIndexInfo.getIndexName().contains(historyAlarm.getIndexName()))
                        .collect(Collectors.toList());
            }
            if (ObjectUtils.isNotEmpty(historyAlarm.getIndexType())) {
                modelNodePointInfoList = modelNodePointInfoList.stream()
                        .filter(modelNodeIndexInfo -> historyAlarm.getIndexType().equals(modelNodeIndexInfo.getIndexType()))
                        .collect(Collectors.toList());
            }
            indexIdList = modelNodePointInfoList.stream().map(ModelNodePointInfo::getIndexId).collect(Collectors.toList());

        } else {

            LambdaQueryWrapper<NodePoint> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(NodePoint::getNodeId, historyAlarm.getNodeId());
            List<NodePoint> nodePointList = nodePointMapper.selectList(queryWrapper);
            List<String> allIndexIdList = nodePointList.stream().map(NodePoint::getPointId).collect(Collectors.toList());
            if (ObjectUtils.isNotEmpty(allIndexIdList)) {
                List<MeterPoint> indexList = meterPointMapper.listMeterPointByIds(allIndexIdList);
                if (ObjectUtils.isNotEmpty(historyAlarm.getIndexName())) {
                    indexList = indexList.stream().filter(energyIndex -> energyIndex.getName().contains(historyAlarm.getIndexName())).collect(Collectors.toList());
                }
                if (ObjectUtils.isNotEmpty(historyAlarm.getIndexType())) {
                    indexList = indexList.stream().filter(energyIndex -> historyAlarm.getIndexType().equals(energyIndex.getPointTypeCode())).collect(Collectors.toList());
                }
                indexIdList = indexList.stream().map(MeterPoint::getPointId).collect(Collectors.toList());
            }
        }

        if (ObjectUtils.isEmpty(indexIdList)) {
            return pageInfo;
        }

        //时间处理 如果不传时间默认查询当天的数据
        Date endTime = DateUtils.parseDate(historyAlarm.getEndTime());
        if (ObjectUtils.isEmpty(endTime)) {
            endTime = DateUtil.endOfDay(DateUtils.getNowDate());
        }
        Date beginTime = DateUtils.parseDate(historyAlarm.getBeginTime());
        if (ObjectUtils.isEmpty(beginTime)) {
            beginTime = DateUtil.beginOfDay(DateUtils.getNowDate());
        }

        return alarmHistoryMapper.getHistoryAlarmList(beginTime, endTime, indexIdList, pageInfo);
    }

}
