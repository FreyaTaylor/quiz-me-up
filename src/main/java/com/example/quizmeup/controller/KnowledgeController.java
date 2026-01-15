package com.example.quizmeup.controller;

import com.example.quizmeup.common.Result;
import com.example.quizmeup.dto.KnowledgeInitRequest;
import com.example.quizmeup.dto.KnowledgeTreeNode;
import com.example.quizmeup.entity.Knowledge;
import com.example.quizmeup.service.StudyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
public class KnowledgeController {

    private static final Logger logger = LoggerFactory.getLogger(KnowledgeController.class);

    @Autowired
    private StudyService studyService;

    /**
     * 获取所有一级知识点（根节点）
     *
     * @return 一级知识点列表
     */
    @GetMapping("/root-nodes")
    public Result<List<Knowledge>> getRootNodes() {
        try {
            List<Knowledge> knowledgeList = studyService.getRootNodes();
            return Result.success(knowledgeList);
        } catch (Exception e) {
            logger.error("获取一级知识点失败", e);
            e.printStackTrace();
            return Result.error("系统错误：" + e.getMessage());
        }
    }

    /**
     * 获取知识树结构（树状结构，只有叶节点可选）
     *
     * @param userId 用户ID（可选，用于获取熟练程度）
     * @return 知识树根节点列表
     */
    @GetMapping("/leaf-nodes")
    public Result<List<KnowledgeTreeNode>> getKnowledgeTree(String userId) {
        try {
            Long userIdLong = null;
            if (userId != null && !userId.trim().isEmpty()) {
                try {
                    userIdLong = Long.parseLong(userId);
                } catch (NumberFormatException e) {
                    logger.warn("无效的 userId 参数: {}", userId);
                }
            }
            List<KnowledgeTreeNode> tree = studyService.getKnowledgeTree(userIdLong);
            return Result.success(tree);
        } catch (Exception e) {
            logger.error("获取知识树失败", e);
            e.printStackTrace();
            return Result.error("系统错误：" + e.getMessage());
        }
    }

    /**
     * 根据根节点ID获取该根节点下的所有叶节点
     *
     * @param rootId 根节点ID
     * @return 叶节点列表
     */
    @GetMapping("/leaf-nodes-by-root")
    public Result<List<Knowledge>> getLeafNodesByRootId(String rootId) {
        try {
            if (rootId == null || rootId.trim().isEmpty()) {
                return Result.error("根节点ID不能为空");
            }
            List<Knowledge> knowledgeList = studyService.getLeafNodesByRootId(rootId);
            return Result.success(knowledgeList);
        } catch (Exception e) {
            logger.error("根据根节点ID获取叶节点失败", e);
            e.printStackTrace();
            return Result.error("系统错误：" + e.getMessage());
        }
    }

    /**
     * 初始化知识树
     *
     * @param request 初始化请求（knowledgeRoot, count）
     * @return 初始化结果
     */
    @PostMapping("/init")
    public Result<String> initKnowledgeTree(@RequestBody KnowledgeInitRequest request) {
        try {
            studyService.initKnowledgeTree(request.getKnowledgeRoot(), request.getCount());
            return Result.success("知识树初始化成功");
        } catch (Exception e) {
            logger.error("初始化知识树失败", e);
            e.printStackTrace();
            return Result.error("系统错误：" + e.getMessage());
        }
    }
}
