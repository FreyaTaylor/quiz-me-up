package com.example.quizmeup.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.quizmeup.entity.Question;
import com.example.quizmeup.entity.QuestionRecord;
import com.example.quizmeup.mapper.QuestionRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

/**
 * 答题记录数据访问层
 * 封装所有对 QuestionRecord 实体的增删改查逻辑
 */
@Repository
@RequiredArgsConstructor
public class QuestionRecordRepository {

    private final QuestionRecordMapper questionRecordMapper;
    private final QuestionRepository questionRepository;

    /**
     * 根据ID查询答题记录
     *
     * @param id 记录ID
     * @return 答题记录，不存在返回 null
     */
    public QuestionRecord findById(Long id) {
        return questionRecordMapper.selectById(id);
    }


    /**
     * 查询用户每个题目的最近一次得分
     *
     * @param userId      用户ID
     * @param questionIds 题目ID列表
     * @return 答题记录列表
     */
    public List<QuestionRecord> findLatestScoresByUserIdAndQuestionIds(Long userId, List<String> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            return List.of();
        }
        
        LambdaQueryWrapper<QuestionRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionRecord::getUserId, userId)
                .in(QuestionRecord::getQuestionId, questionIds)
                .orderByDesc(QuestionRecord::getSubmittedAt);

        List<QuestionRecord> all = questionRecordMapper.selectList(wrapper);

        List<QuestionRecord> latest = all.stream()
                .collect(Collectors.toMap(
                        QuestionRecord::getQuestionId,
                        record -> record,
                        (existing, replacement) -> existing, // 保留第一个（最新）
                        LinkedHashMap::new // 保持插入顺序（可选）
                ))
                .values()
                .stream()
                .toList();



        return latest;
    }

    /**
     * 根据用户ID和题目ID查询答题记录
     *
     * @param userId     用户ID
     * @param questionId 题目ID
     * @return 答题记录列表
     */
    public List<QuestionRecord> findByUserIdAndQuestionId(Long userId, String questionId) {
        LambdaQueryWrapper<QuestionRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionRecord::getUserId, userId)
                .eq(QuestionRecord::getQuestionId, questionId)
                .orderByDesc(QuestionRecord::getSubmittedAt);
        return questionRecordMapper.selectList(wrapper);
    }

    public int save(QuestionRecord record) {
        return questionRecordMapper.insert(record);
    }
}
