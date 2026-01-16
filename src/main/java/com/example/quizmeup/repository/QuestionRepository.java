package com.example.quizmeup.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.quizmeup.entity.Question;
import com.example.quizmeup.mapper.QuestionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 题目数据访问层
 * 封装所有对 Question 实体的增删改查逻辑
 */
@Repository
@RequiredArgsConstructor
public class QuestionRepository {

    private final QuestionMapper questionMapper;

    /**
     * 根据ID查询题目
     *
     * @param id 题目ID
     * @return 题目信息，不存在返回 null
     */
    public Question findById(String id) {
        return questionMapper.selectById(id);
    }

    /**
     * 根据知识点ID查询题目列表
     *
     * @param knowledgeId 知识点ID
     * @return 题目列表
     */
    public List<Question> findByKnowledgeId(String knowledgeId) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Question::getKnowledgeId, knowledgeId)
                .orderByDesc(Question::getCreatedAt);
        return questionMapper.selectList(wrapper);
    }

    /**
     * 保存题目
     *
     * @param question 题目信息
     * @return 影响行数
     */
    public int save(Question question) {
        if (question.getId() == null) {
            return questionMapper.insert(question);
        } else {
            return questionMapper.updateById(question);
        }
    }

    /**
     * 批量保存题目
     *
     * @param questions 题目列表
     * @return 是否成功
     */
    public boolean saveBatch(List<Question> questions) {
        for (Question question : questions) {
            save(question);
        }
        return true;
    }
}
