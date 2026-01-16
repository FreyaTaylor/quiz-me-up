package com.example.quizmeup.controller;

import com.example.quizmeup.common.FeResponse;
import com.example.quizmeup.dto.KnowledgeInitRequest;
import com.example.quizmeup.dto.KnowledgeTreeByRootRequest;
import com.example.quizmeup.dto.KnowledgeTreeNode;
import com.example.quizmeup.entity.Knowledge;
import com.example.quizmeup.service.StudyService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 知识点控制器
 */
@RestController
@RequestMapping("/api/v1/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private static final Logger logger = LoggerFactory.getLogger(KnowledgeController.class);

    private final StudyService studyService;

    /**
     * 获取所有一级知识点（根节点）
     *
     * @return 一级知识点列表
     */
    @GetMapping("/root-nodes")
    public FeResponse<List<Knowledge>> getRootNodes() {
        try {
            List<Knowledge> knowledgeList = studyService.getRootNodes();
            return FeResponse.success(knowledgeList);
        } catch (Exception e) {
            logger.error("获取一级知识点失败", e);
            return FeResponse.error("系统错误：" + e.getMessage());
        }
    }

    /**
     * 根据根节点ID获取该根节点下的知识树（树形结构，结合 userId 用于熟练度）
     *
     * @param request 请求体，包含 rootId 和 userId
     * @return 以 rootId 为根的知识树
     */
    @PostMapping("/leaf-nodes-by-root")
    public FeResponse<KnowledgeTreeNode> getLeafNodesByRootId(@RequestBody KnowledgeTreeByRootRequest request) {
        try {
            KnowledgeTreeNode tree = studyService.getKnowledgeTreeByRoot(request.getRootId(), request.getUserId());
            return FeResponse.success(tree);
        } catch (Exception e) {
            logger.error("根据根节点ID获取知识树失败", e);
            return FeResponse.error("系统错误：" + e.getMessage());
        }
    }

    /**
     * 初始化知识树
     *
     * @param request 初始化请求（knowledgeRoot, count）
     * @return 初始化结果
     */
    @PostMapping("/init")
    public FeResponse<String> initKnowledgeTree(@RequestBody KnowledgeInitRequest request) {
        try {
            studyService.initKnowledgeTree(request.getKnowledgeRoot(), request.getCount());
            return FeResponse.success("知识树初始化成功");
        } catch (Exception e) {
            logger.error("初始化知识树失败", e);
            return FeResponse.error("系统错误：" + e.getMessage());
        }
    }
}
