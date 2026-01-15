package com.example.quizmeup.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.quizmeup.entity.Knowledge;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 知识点 Mapper 接口
 */
@Mapper
public interface KnowledgeMapper extends BaseMapper<Knowledge> {
    /**
     * 查询所有叶节点知识点
     *
     * @return 叶节点知识点列表
     */
    List<Knowledge> selectAllLeafNodes();

    /**
     * 查询所有一级知识点（根节点）
     *
     * @return 一级知识点列表
     */
    List<Knowledge> selectRootNodes();

    /**
     * 根据根节点ID查询该根节点下的所有叶节点
     *
     * @param rootId 根节点ID
     * @return 叶节点列表
     */
    List<Knowledge> selectLeafNodesByRootId(@Param("rootId") String rootId);
}
