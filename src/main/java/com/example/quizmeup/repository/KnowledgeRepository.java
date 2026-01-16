package com.example.quizmeup.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.quizmeup.entity.Knowledge;
import com.example.quizmeup.mapper.KnowledgeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 知识点数据访问层
 * 封装所有对 Knowledge 实体的增删改查逻辑
 */
@Repository
@RequiredArgsConstructor
public class KnowledgeRepository {

    private final KnowledgeMapper knowledgeMapper;

    /**
     * 根据ID查询知识点
     *
     * @param id 知识点ID
     * @return 知识点信息，不存在返回 null
     */
    public Knowledge findById(String id) {
        return knowledgeMapper.selectById(id);
    }

    /**
     * 查询所有知识点
     *
     * @return 知识点列表
     */
    public List<Knowledge> findAll() {
        return knowledgeMapper.selectList(null);
    }

    /**
     * 查询所有叶节点知识点
     *
     * @return 叶节点知识点列表
     */
    public List<Knowledge> findAllLeafNodes() {
        LambdaQueryWrapper<Knowledge> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Knowledge::getIsLeaf, true)
                .orderByAsc(Knowledge::getCreatedAt);
        return knowledgeMapper.selectList(wrapper);
    }

    /**
     * 查询所有一级知识点（根节点）
     *
     * @return 一级知识点列表
     */
    public List<Knowledge> findRootNodes() {
        LambdaQueryWrapper<Knowledge> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.isNull(Knowledge::getParentId).or().eq(Knowledge::getParentId, ""))
                .orderByAsc(Knowledge::getCreatedAt);
        return knowledgeMapper.selectList(wrapper);
    }

    /**
     * 根据父节点ID查询子节点
     *
     * @param parentId 父节点ID
     * @return 子节点列表
     */
    public List<Knowledge> findByParentId(String parentId) {
        LambdaQueryWrapper<Knowledge> wrapper = new LambdaQueryWrapper<>();
        if (parentId == null || parentId.isEmpty()) {
            wrapper.isNull(Knowledge::getParentId).or().eq(Knowledge::getParentId, "");
        } else {
            wrapper.eq(Knowledge::getParentId, parentId);
        }
        return knowledgeMapper.selectList(wrapper);
    }

    /**
     * 根据根节点ID查询该根节点下的所有叶节点
     *
     * @param rootId 根节点ID
     * @return 叶节点列表
     */
    public List<Knowledge> findLeafNodesByRootId(String rootId) {
        LambdaQueryWrapper<Knowledge> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Knowledge::getIsLeaf, true)
                .and(w -> w.eq(Knowledge::getId, rootId).or().likeRight(Knowledge::getId, rootId + "."))
                .orderByAsc(Knowledge::getCreatedAt);
        return knowledgeMapper.selectList(wrapper);
    }

    /**
     * 根据根节点ID查询该根节点及其所有子节点
     *
     * @param rootId 根节点ID
     * @return 根节点及其所有子节点列表
     */
    public List<Knowledge> findTreeByRootId(String rootId) {
        LambdaQueryWrapper<Knowledge> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(Knowledge::getId, rootId).or().likeRight(Knowledge::getId, rootId + "."))
                .orderByAsc(Knowledge::getLevel)
                .orderByAsc(Knowledge::getCreatedAt);
        return knowledgeMapper.selectList(wrapper);
    }


    public int save(List<Knowledge> knowledges) {
        knowledgeMapper.insert(knowledges);
        return knowledges.size();
    }


}
