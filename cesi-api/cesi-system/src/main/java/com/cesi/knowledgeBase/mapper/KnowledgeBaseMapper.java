package com.cesi.knowledgeBase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cesi.knowledgeBase.domain.entity.KnowledgeBase;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识库(KnowledgeBase)表数据库访问层
 */
@Mapper
public interface KnowledgeBaseMapper extends BaseMapper<KnowledgeBase> {

}

