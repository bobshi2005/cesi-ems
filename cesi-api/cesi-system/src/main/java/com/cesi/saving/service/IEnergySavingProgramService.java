package com.cesi.saving.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cesi.common.core.domain.AjaxResult;
import com.cesi.saving.domain.entity.EnergySavingProgram;
import com.cesi.saving.domain.dto.EnergySavingProgramDTO;
import com.cesi.saving.domain.vo.EnergySavingProgramVO;

/**
 * 节能项目管理
 * Service接口
 *
 * @author Cesi
 */
public interface IEnergySavingProgramService extends IService<EnergySavingProgram> {
    /**
     * 查询
     * 节能项目管理
     *
     * @param id 节能项目管理
     *           主键
     * @return 节能项目管理
     */
    EnergySavingProgram selectEnergySavingProgramById(Long id);

    /**
     * 查询
     * 节能项目管理
     * 列表
     *
     * @param energySavingProgram 节能项目管理
     * @return 节能项目管理
     * 集合
     */
    Page<EnergySavingProgramVO> selectEnergySavingProgramList(EnergySavingProgram energySavingProgram);

    /**
     * 新增
     * 节能项目管理
     *
     * @param dto
     * @return 结果
     */
    AjaxResult insertEnergySavingProgram(EnergySavingProgramDTO dto);

    /**
     * 修改
     * 节能项目管理
     *
     * @param dto
     * @return 结果
     */
    AjaxResult updateEnergySavingProgram(EnergySavingProgramDTO dto);


    /**
     * 删除
     * 节能项目管理
     * 信息
     *
     * @param id 节能项目管理
     *           主键
     * @return 结果
     */
    AjaxResult deleteEnergySavingProgramById(Long id);
}
