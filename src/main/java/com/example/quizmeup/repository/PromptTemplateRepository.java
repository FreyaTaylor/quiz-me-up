package com.example.quizmeup.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.quizmeup.entity.PromptTemplate;
import com.example.quizmeup.mapper.PromptTemplateMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * Prompt 模板数据访问层
 * 封装所有对 PromptTemplate 实体的增删改查逻辑
 */
@Repository
@RequiredArgsConstructor
public class PromptTemplateRepository {

    private final PromptTemplateMapper promptTemplateMapper;

    /**
     * 根据ID查询模板
     *
     * @param id 模板ID
     * @return 模板信息，不存在返回 null
     */
    public PromptTemplate findById(Long id) {
        return promptTemplateMapper.selectById(id);
    }

    /**
     * 根据代码查询模板
     *
     * @param code 模板代码
     * @return 模板信息，不存在返回 null
     */
    public PromptTemplate findByCode(String code) {
        LambdaQueryWrapper<PromptTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PromptTemplate::getCode, code);
        return promptTemplateMapper.selectOne(wrapper);
    }

    /**
     * 保存模板
     *
     * @param template 模板信息
     * @return 影响行数
     */
    public int save(PromptTemplate template) {
        if (template.getId() == null) {
            return promptTemplateMapper.insert(template);
        } else {
            return promptTemplateMapper.updateById(template);
        }
    }
}
