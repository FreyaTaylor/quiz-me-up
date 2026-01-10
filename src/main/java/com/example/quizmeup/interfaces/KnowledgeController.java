package com.example.quizmeup.interfaces;

import com.example.quizmeup.service.KnowledgeService;
import com.example.quizmeup.service.dto.CreateRootNodeRequest;
import com.example.quizmeup.service.dto.ExpandSubtreeRequest;
import com.example.quizmeup.service.dto.KnowledgeNodeDTO;
import com.example.quizmeup.service.dto.UpdateNodeRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 知识树控制器。
 */
@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    /**
     * 获取用户的知识树（带掌握度）。
     */
    @GetMapping("/tree")
    public List<KnowledgeNodeDTO> getKnowledgeTree(@RequestParam Long userId) {
        return knowledgeService.getUserKnowledgeTree(userId);
    }

    /**
     * 创建根节点。
     */
    @PostMapping("/mindmap/root")
    public Map<String, Object> createRootNode(@Valid @RequestBody CreateRootNodeRequest request) {
        String nodeId = knowledgeService.createRootNode(request);
        return Map.of("message", "根节点创建成功", "nodeId", nodeId);
    }

    /**
     * 更新节点信息（编辑节点）。
     */
    @PutMapping("/mindmap/node")
    public Map<String, String> updateNode(@Valid @RequestBody UpdateNodeRequest request) {
        knowledgeService.updateNode(request);
        return Map.of("message", "节点更新成功");
    }

    /**
     * AI扩充子树。
     */
    @PostMapping("/mindmap/expand")
    public Map<String, String> expandSubtree(@Valid @RequestBody ExpandSubtreeRequest request) {
        knowledgeService.expandSubtree(request);
        return Map.of("message", "子树扩充成功");
    }
}
