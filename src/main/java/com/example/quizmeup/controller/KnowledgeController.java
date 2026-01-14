package com.example.quizmeup.controller;

import com.example.quizmeup.common.Result;
import com.example.quizmeup.entity.Knowledge;
import com.example.quizmeup.service.StudyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 知识点控制器
 */
@RestController
@RequestMapping("/api/v1/knowledge")
public class KnowledgeController {

    private static final Logger logger = LoggerFactory.getLogger(KnowledgeController.class);

    @Autowired
    private StudyService studyService;

    /**
     * 获取所有叶节点知识点
     *
     * @return 叶节点知识点列表
     */
    @GetMapping("/leaf-nodes")
    public Result<List<Knowledge>> getLeafNodes() {
        try {
            List<Knowledge> knowledgeList = studyService.getAllLeafKnowledge();
            return Result.success(knowledgeList);
        } catch (Exception e) {
            logger.error("获取叶节点知识点失败", e);
            e.printStackTrace(); // 打印异常栈到控制台
            return Result.error("系统错误：" + e.getMessage());
        }
    }
}
