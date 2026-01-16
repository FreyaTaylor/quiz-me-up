package com.example.quizmeup.controller;

import com.example.quizmeup.common.FeResponse;
import com.example.quizmeup.dto.KnowledgeProgressNode;
import com.example.quizmeup.dto.ProgressTreeRequest;
import com.example.quizmeup.service.ProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 学习进度控制器
 */
@RestController
@RequestMapping("/api/v1/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    /**
     * 获取学习进度树接口
     * 返回整个知识树（递归结构），包含每个节点的掌握度
     *
     * @param request 请求体，包含 userId
     * @return 知识树根节点列表
     */
    @PostMapping("/tree")
    public FeResponse<List<KnowledgeProgressNode>> getProgressTree(@RequestBody ProgressTreeRequest request) {
        List<KnowledgeProgressNode> tree = progressService.getProgressTree(request.getUserId());
        return FeResponse.success(tree);
    }
}
