package com.example.quizmeup.service;

import com.example.quizmeup.ai.LlmClient;
import com.example.quizmeup.ai.PromptService;
import com.example.quizmeup.common.AiResponse;
import com.example.quizmeup.dto.KnowledgeTreeNode;
import com.example.quizmeup.entity.*;
import com.example.quizmeup.mapper.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StudyService {

    @Autowired
    private KnowledgeMapper knowledgeMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private QuestionRecordMapper questionRecordMapper;

    @Autowired
    private UserMasteryMapper userMasteryMapper;

    @Autowired
    private PromptService promptService;

    @Autowired
    private LlmClient llmClient;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 获取所有叶节点知识点
     */
    public List<Knowledge> getAllLeafKnowledge() {
        return knowledgeMapper.selectAllLeafNodes();
    }

    /**
     * 获取所有一级知识点（根节点）
     */
    public List<Knowledge> getRootNodes() {
        return knowledgeMapper.selectRootNodes();
    }

    /**
     * 根据根节点ID获取该根节点下的所有叶节点
     *
     * @param rootId 根节点ID
     * @return 叶节点列表
     */
    public List<Knowledge> getLeafNodesByRootId(String rootId) {
        return knowledgeMapper.selectLeafNodesByRootId(rootId);
    }

    /**
     * 获取完整的知识树结构（用于前端选择）
     *
     * @param userId 用户ID（可选，用于获取熟练程度）
     * @return 知识树根节点列表
     */
    public List<KnowledgeTreeNode> getKnowledgeTree(Long userId) {
        // 1. 查询所有知识点
        List<Knowledge> allKnowledge = knowledgeMapper.selectList(null);

        // 2. 查询用户的掌握度映射（如果提供了 userId）
        Map<String, BigDecimal> masteryMap = new HashMap<>();
        if (userId != null) {
            masteryMap = getUserMasteryMap(userId);
        }

        // 3. 构建知识点映射（id -> Knowledge）
        Map<String, Knowledge> knowledgeMap = allKnowledge.stream()
                .collect(Collectors.toMap(Knowledge::getId, k -> k));

        // 4. 构建父子关系映射（parentId -> List<Knowledge>）
        Map<String, List<Knowledge>> childrenMap = new HashMap<>();
        for (Knowledge knowledge : allKnowledge) {
            String parentId = knowledge.getParentId();
            if (parentId == null || parentId.isEmpty()) {
                parentId = "ROOT"; // 根节点使用特殊标识
            }
            childrenMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(knowledge);
        }

        // 5. 构建树结构
        List<Knowledge> rootNodes = childrenMap.getOrDefault("ROOT", new ArrayList<>());
        List<KnowledgeTreeNode> result = new ArrayList<>();

        for (Knowledge rootNode : rootNodes) {
            KnowledgeTreeNode node = buildKnowledgeTreeNode(rootNode, childrenMap, masteryMap);
            result.add(node);
        }

        return result;
    }

    /**
     * 获取用户的掌握度映射
     *
     * @param userId 用户ID
     * @return 知识点ID -> 掌握度的映射
     */
    private Map<String, BigDecimal> getUserMasteryMap(Long userId) {
        Map<String, BigDecimal> masteryMap = new HashMap<>();
        List<Knowledge> allKnowledge = knowledgeMapper.selectList(null);

        for (Knowledge knowledge : allKnowledge) {
            if (Boolean.TRUE.equals(knowledge.getIsLeaf())) {
                UserMastery mastery = userMasteryMapper.selectByUserIdAndKnowledgeId(userId, knowledge.getId());
                if (mastery != null && mastery.getProficiency() != null) {
                    masteryMap.put(knowledge.getId(), mastery.getProficiency());
                } else {
                    masteryMap.put(knowledge.getId(), BigDecimal.ZERO);
                }
            }
        }

        return masteryMap;
    }

    /**
     * 递归构建知识树节点
     *
     * @param knowledge   当前知识点
     * @param childrenMap 子节点映射
     * @param masteryMap  掌握度映射（知识点ID -> 掌握度）
     * @return 知识树节点
     */
    private KnowledgeTreeNode buildKnowledgeTreeNode(Knowledge knowledge, Map<String, List<Knowledge>> childrenMap, Map<String, BigDecimal> masteryMap) {
        KnowledgeTreeNode node = new KnowledgeTreeNode();
        node.setId(knowledge.getId());
        node.setName(knowledge.getName());
        node.setDescription(knowledge.getDescription());
        node.setIsLeaf(knowledge.getIsLeaf());
        node.setImportance(knowledge.getImportance() != null ? knowledge.getImportance() : 3);
        node.setLevel(knowledge.getLevel() != null ? knowledge.getLevel() : 1);

        // 设置熟练程度（仅叶节点）
        if (Boolean.TRUE.equals(knowledge.getIsLeaf())) {
            BigDecimal proficiency = masteryMap.getOrDefault(knowledge.getId(), BigDecimal.ZERO);
            node.setProficiency(proficiency.setScale(2, RoundingMode.HALF_UP));
        } else {
            node.setProficiency(null);
        }

        // 获取子节点
        List<Knowledge> children = childrenMap.getOrDefault(knowledge.getId(), new ArrayList<>());

        if (children.isEmpty()) {
            node.setChildren(new ArrayList<>());
        } else {
            // 递归构建子节点
            List<KnowledgeTreeNode> childNodes = new ArrayList<>();
            for (Knowledge child : children) {
                KnowledgeTreeNode childNode = buildKnowledgeTreeNode(child, childrenMap, masteryMap);
                childNodes.add(childNode);
            }
            node.setChildren(childNodes);
        }

        return node;
    }

    /**
     * 初始化知识树
     * 调用 LLM 生成知识树并保存到数据库
     *
     * @param knowledgeRoot 知识树根节点名称（如 "Java", "Python"）
     * @param count         建议的节点数量
     */
    @Transactional
    public void initKnowledgeTree(String knowledgeRoot, Integer count) {
        try {
            // 1. 调用 PromptService 生成 prompt
            Map<String, Object> params = new HashMap<>();
            params.put("knowledgeRoot", knowledgeRoot);
            params.put("count", count.toString());
            String prompt = promptService.render("KNOWLEDGE_TREE_GEN", params);

            // 2. 调用 LLM 获取知识树
            String llmResponse = llmClient.call(prompt);

            // 3. 解析 LLM 返回的 JSON
            AiResponse<KnowledgeTreeNode> resp = objectMapper.readValue(llmResponse, new TypeReference<>() {});
            KnowledgeTreeNode rootNode = resp.getData();

            if (rootNode == null) {
                throw new RuntimeException("LLM 返回的知识树为空");
            }

            // 4. 递归保存知识树到数据库
            saveKnowledgeTreeRecursive(rootNode, null, 1);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("解析 LLM 响应失败: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("初始化知识树失败: " + e.getMessage(), e);
        }
    }

    /**
     * 递归保存知识树节点
     *
     * @param node     当前节点
     * @param parentId 父节点ID（根节点为 null）
     * @param level    当前层级（从 1 开始）
     */
    private void saveKnowledgeTreeRecursive(KnowledgeTreeNode node, String parentId, int level) {
        // 检查节点是否已存在
        Knowledge existing = knowledgeMapper.selectById(node.getId());
        if (existing != null) {
            // 如果节点已存在，更新信息
            existing.setName(node.getName());
            existing.setImportance(node.getImportance());
            existing.setParentId(parentId);
            existing.setLevel(level);
            existing.setUpdatedAt(LocalDateTime.now());
            knowledgeMapper.updateById(existing);
        } else {
            // 创建新节点
            Knowledge knowledge = new Knowledge();
            knowledge.setId(node.getId());
            knowledge.setParentId(parentId);
            knowledge.setName(node.getName());
            knowledge.setImportance(node.getImportance());
            knowledge.setLevel(level);
            knowledge.setCreatedAt(LocalDateTime.now());
            knowledge.setUpdatedAt(LocalDateTime.now());
            
            // 判断是否为叶节点（没有子节点或子节点为空）
            boolean isLeaf = node.getChildren() == null || node.getChildren().isEmpty();
            knowledge.setIsLeaf(isLeaf);
            
            knowledgeMapper.insert(knowledge);
        }

        // 递归保存子节点
        if (node.getChildren() != null && !node.getChildren().isEmpty()) {
            for (KnowledgeTreeNode child : node.getChildren()) {
                // 构建子节点的 ID：如果子节点 ID 不是以父节点 ID 开头的完整路径，则拼接
                String childId = child.getId();
                String parentNodeId = node.getId();
                
                // 如果子节点 ID 不是以父节点 ID 开头的完整路径，则拼接
                if (!childId.startsWith(parentNodeId + ".") && !childId.equals(parentNodeId)) {
                    childId = parentNodeId + "." + childId;
                    child.setId(childId);
                }
                
                saveKnowledgeTreeRecursive(child, node.getId(), level + 1);
            }
        }
    }



    /**
     * 更新用户掌握度
     */
    @Transactional
    public void updateUserMastery(Long userId, String knowledgeId) {
        List<QuestionRecord> records = questionRecordMapper.selectByUserIdAndKnowledgeId(userId, knowledgeId);

        if (records.isEmpty()) {
            return;
        }

        double avgScore = records.stream()
                .mapToInt(QuestionRecord::getScoreInt)
                .average()
                .orElse(0.0);

        BigDecimal proficiency = BigDecimal.valueOf(avgScore)
                .setScale(2, RoundingMode.HALF_UP);

        UserMastery mastery = userMasteryMapper.selectByUserIdAndKnowledgeId(userId, knowledgeId);
        if (mastery == null) {
            mastery = new UserMastery();
            mastery.setUserId(userId);
            mastery.setKnowledgeId(knowledgeId);
            mastery.setProficiency(proficiency);
            userMasteryMapper.insert(mastery);
        } else {
            mastery.setProficiency(proficiency);
            userMasteryMapper.updateProficiency(mastery);
        }
    }
}
