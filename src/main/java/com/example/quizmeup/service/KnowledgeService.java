package com.example.quizmeup.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.quizmeup.ai.LlmClient;
import com.example.quizmeup.ai.PromptService;
import com.example.quizmeup.domain.entity.Knowledge;
import com.example.quizmeup.domain.entity.UserMastery;
import com.example.quizmeup.infra.mapper.KnowledgeMapper;
import com.example.quizmeup.infra.mapper.UserMasteryMapper;
import com.example.quizmeup.service.dto.CreateRootNodeRequest;
import com.example.quizmeup.service.dto.ExpandSubtreeRequest;
import com.example.quizmeup.service.dto.KnowledgeNodeDTO;
import com.example.quizmeup.service.dto.UpdateNodeRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 知识树服务。
 */
@Service
@RequiredArgsConstructor
public class KnowledgeService {

    private final KnowledgeMapper knowledgeMapper;
    private final UserMasteryMapper userMasteryMapper;
    private final PromptService promptService;
    private final LlmClient llmClient;

    /**
     * 获取用户的知识树（带掌握度）。
     */
    public List<KnowledgeNodeDTO> getUserKnowledgeTree(Long userId) {
        // 1. 获取所有知识点
        List<Knowledge> allKnowledge = knowledgeMapper.selectList(null);
        
        // 2. 获取用户的掌握度
        Map<String, UserMastery> masteryMap = userMasteryMapper.selectList(
                Wrappers.<UserMastery>lambdaQuery().eq(UserMastery::getUserId, userId)
        ).stream().collect(Collectors.toMap(UserMastery::getKnowledgeId, m -> m));
        
        // 3. 构建树形结构
        Map<String, List<Knowledge>> childrenMap = allKnowledge.stream()
                .collect(Collectors.groupingBy(k -> 
                        Optional.ofNullable(k.getParentId()).orElse("ROOT")));
        
        // 4. 计算加权进度
        return buildKnowledgeTree(childrenMap, masteryMap, "ROOT", 0);
    }

    private List<KnowledgeNodeDTO> buildKnowledgeTree(
            Map<String, List<Knowledge>> childrenMap,
            Map<String, UserMastery> masteryMap,
            String parentId,
            int level) {
        
        List<Knowledge> nodes = childrenMap.getOrDefault(parentId, Collections.emptyList());
        List<KnowledgeNodeDTO> result = new ArrayList<>();
        
        for (Knowledge k : nodes) {
            KnowledgeNodeDTO dto = new KnowledgeNodeDTO();
            dto.setId(k.getId());
            dto.setName(k.getName());
            dto.setDescription(k.getDescription());
            dto.setParentId(k.getParentId());
            dto.setLevel(k.getLevel());
            dto.setImportance(k.getImportance());
            dto.setIsLeaf(k.getIsLeaf());
            
            UserMastery mastery = masteryMap.get(k.getId());
            if (mastery != null) {
                dto.setProficiency(mastery.getProficiency());
                dto.setTotalQuestions(mastery.getTotalQuestions());
                dto.setPracticedCount(mastery.getPracticedCount());
            } else {
                dto.setProficiency(0);
                dto.setTotalQuestions(0);
                dto.setPracticedCount(0);
            }
            
            // 递归构建子节点
            List<KnowledgeNodeDTO> children = buildKnowledgeTree(childrenMap, masteryMap, k.getId(), level + 1);
            dto.setChildren(children);
            
            // 计算加权进度（如果有子节点，则加权平均）
            if (!children.isEmpty()) {
                double totalWeight = 0;
                double weightedSum = 0;
                for (KnowledgeNodeDTO child : children) {
                    if (child.getWeightedProgress() != null) {
                        int weight = child.getImportance() != null ? child.getImportance() : 3;
                        totalWeight += weight;
                        weightedSum += child.getWeightedProgress() * weight;
                    }
                }
                dto.setWeightedProgress(totalWeight > 0 ? weightedSum / totalWeight : 0.0);
            } else {
                // 叶节点：加权进度 = proficiency / 100
                dto.setWeightedProgress((double) dto.getProficiency() / 100.0);
            }
            
            result.add(dto);
        }
        
        return result;
    }

    /**
     * 创建根节点。
     */
    @Transactional
    public String createRootNode(CreateRootNodeRequest request) {
        // 生成节点ID（使用名称的拼音或英文下划线格式）
        String nodeId = generateNodeId(request.getName());
        
        // 检查ID是否已存在
        if (knowledgeMapper.selectById(nodeId) != null) {
            // 如果已存在，添加时间戳后缀
            nodeId = nodeId + "_" + System.currentTimeMillis();
        }
        
        Knowledge rootNode = new Knowledge();
        rootNode.setId(nodeId);
        rootNode.setParentId(null);
        rootNode.setName(request.getName().trim());
        rootNode.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        rootNode.setLevel(1);
        rootNode.setPath(nodeId);
        rootNode.setImportance(request.getImportance() != null ? request.getImportance() : 3);
        rootNode.setIsLeaf(true); // 初始为叶节点，可以后续扩充
        rootNode.setCreatedAt(LocalDateTime.now());
        rootNode.setUpdatedAt(LocalDateTime.now());
        
        knowledgeMapper.insert(rootNode);
        return nodeId;
    }

    /**
     * 生成节点ID（将中文名称转换为英文下划线格式）。
     */
    private String generateNodeId(String name) {
        // 简单处理：移除空格和特殊字符，转换为小写，用下划线连接
        String id = name.trim()
                .toLowerCase()
                .replaceAll("[^a-z0-9\\u4e00-\\u9fa5]", "_")
                .replaceAll("_+", "_");
        
        // 如果包含中文，使用拼音首字母（简化处理，实际可以使用拼音库）
        // 这里简化处理：如果包含中文，使用前几个字符的拼音首字母
        if (id.matches(".*[\\u4e00-\\u9fa5].*")) {
            // 简化处理：使用名称的前几个字符
            id = "root_" + System.currentTimeMillis() % 10000;
        }
        
        return id;
    }

    /**
     * 更新节点信息（编辑节点）。
     */
    @Transactional
    public void updateNode(UpdateNodeRequest request) {
        Knowledge knowledge = knowledgeMapper.selectById(request.getNodeId());
        if (knowledge == null) {
            throw new IllegalArgumentException("节点不存在: " + request.getNodeId());
        }
        
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            knowledge.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            knowledge.setDescription(request.getDescription().trim());
        }
        if (request.getImportance() != null) {
            knowledge.setImportance(request.getImportance());
        }
        knowledge.setUpdatedAt(LocalDateTime.now());
        
        knowledgeMapper.updateById(knowledge);
    }

    /**
     * AI扩充子树（为第二层级以下的空子树生成子节点）。
     */
    @Transactional
    public void expandSubtree(ExpandSubtreeRequest request) {
        Knowledge parentNode = knowledgeMapper.selectById(request.getNodeId());
        if (parentNode == null) {
            throw new IllegalArgumentException("节点不存在: " + request.getNodeId());
        }
        
        // 检查是否为第二层级以下（level >= 2）
        if (parentNode.getLevel() == null || parentNode.getLevel() < 2) {
            throw new IllegalArgumentException("只能扩充第二层级以下的节点");
        }
        
        // 检查是否已有子节点
        List<Knowledge> existingChildren = knowledgeMapper.selectList(
                Wrappers.<Knowledge>lambdaQuery().eq(Knowledge::getParentId, request.getNodeId())
        );
        if (!existingChildren.isEmpty()) {
            throw new IllegalArgumentException("该节点已有子节点，无法扩充");
        }
        
        // 调用LLM生成子节点
        String prompt = promptService.render("EXPAND_SUBTREE", Map.of(
                "parentName", parentNode.getName(),
                "parentDescription", parentNode.getDescription() != null ? parentNode.getDescription() : "",
                "parentLevel", parentNode.getLevel().toString()
        ));
        String raw = llmClient.call(prompt);
        
        // 解析LLM返回的JSON
        JSONArray childrenNodes = JSON.parseArray(raw);
        
        // 保存子节点
        for (int i = 0; i < childrenNodes.size(); i++) {
            JSONObject node = childrenNodes.getJSONObject(i);
            Knowledge child = new Knowledge();
            child.setId(request.getNodeId() + "_" + node.getString("id"));
            child.setParentId(request.getNodeId());
            child.setName(node.getString("name"));
            child.setDescription(node.getString("description"));
            child.setLevel(parentNode.getLevel() + 1);
            child.setPath((parentNode.getPath() != null ? parentNode.getPath() + "/" : "") + node.getString("id"));
            child.setImportance(node.getInteger("importance") != null ? node.getInteger("importance") : 3);
            child.setIsLeaf(node.getBoolean("isLeaf") != null ? node.getBoolean("isLeaf") : true);
            child.setCreatedAt(LocalDateTime.now());
            child.setUpdatedAt(LocalDateTime.now());
            
            knowledgeMapper.insert(child);
        }
        
        // 更新父节点为非叶节点
        parentNode.setIsLeaf(false);
        parentNode.setUpdatedAt(LocalDateTime.now());
        knowledgeMapper.updateById(parentNode);
    }
}
