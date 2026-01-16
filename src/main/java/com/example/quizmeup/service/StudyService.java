package com.example.quizmeup.service;

import com.example.quizmeup.ai.PromptService;
import com.example.quizmeup.ai.ReasonerLlmClient;
import com.example.quizmeup.common.AiResponse;
import com.example.quizmeup.common.Constants;
import com.example.quizmeup.dto.KnowledgeTreeNode;
import com.example.quizmeup.entity.*;
import com.example.quizmeup.repository.KnowledgeRepository;
import com.example.quizmeup.repository.UserMasteryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudyService {

    private final KnowledgeRepository knowledgeRepository;
    private final UserMasteryRepository userMasteryRepository;
    private final PromptService promptService;
    private final ReasonerLlmClient reasonerLlmClient;
    private final ObjectMapper objectMapper;

    /**
     * 获取所有叶节点知识点
     *
     * @return 所有叶节点知识点列表
     */
    public List<Knowledge> getAllLeafKnowledge() {
        return knowledgeRepository.findAllLeafNodes();
    }

    /**
     * 获取所有一级知识点（根节点）
     *
     * @return 所有根节点列表
     */
    public List<Knowledge> getRootNodes() {
        return knowledgeRepository.findRootNodes();
    }

    /**
     * 根据根节点ID获取该根节点下的所有叶节点
     *
     * @param rootId 根节点ID
     * @return 叶节点列表
     */
    public List<Knowledge> getLeafNodesByRootId(String rootId) {
        return knowledgeRepository.findLeafNodesByRootId(rootId);
    }

    /**
     * 获取完整的知识树结构（用于前端选择）
     *
     * @param userId 用户ID（可选，用于获取熟练程度）
     * @return 知识树根节点列表
     */
    public List<KnowledgeTreeNode> getKnowledgeTree(Long userId) {
        // 1. 查询所有知识点
        List<Knowledge> allKnowledge = knowledgeRepository.findAll();

        // 2. 查询用户的掌握度映射（如果提供了 userId）
        Map<String, BigDecimal> masteryMap = new HashMap<>();
        if (userId != null) {
            masteryMap = getUserMasteryMap(userId);
        }

        // 3. 构建父子关系映射（parentId -> List<Knowledge>）
        Map<String, List<Knowledge>> childrenMap = new HashMap<>();
        for (Knowledge knowledge : allKnowledge) {
            String parentId = knowledge.getParentId();
            if (parentId == null || parentId.isEmpty()) {
                parentId = Constants.ROOT_NODE_ID; // 根节点使用特殊标识
            }
            childrenMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(knowledge);
        }

        // 4. 构建树结构
        List<Knowledge> rootNodes = childrenMap.getOrDefault(Constants.ROOT_NODE_ID, new ArrayList<>());
        List<KnowledgeTreeNode> result = new ArrayList<>();

        for (Knowledge rootNode : rootNodes) {
            KnowledgeTreeNode node = buildKnowledgeTreeNode(rootNode, childrenMap, masteryMap);
            result.add(node);
        }

        return result;
    }

    /**
     * 根据根节点 ID 获取该根节点下的知识树（树形结构）
     * 直接查询指定root的数据，不查询所有root
     *
     * @param rootId 根节点ID
     * @param userId 用户ID（可选，用于获取熟练程度）
     * @return 指定根节点为根的知识树
     */
    public KnowledgeTreeNode getKnowledgeTreeByRoot(String rootId, Long userId) {
        if (rootId == null || rootId.trim().isEmpty()) {
            throw new IllegalArgumentException("rootId 不能为空");
        }

        // 1. 直接查询指定root及其所有子节点
        List<Knowledge> treeNodes = knowledgeRepository.findTreeByRootId(rootId);
        if (treeNodes == null || treeNodes.isEmpty()) {
            return null;
        }

        // 2. 查找根节点
        Knowledge rootKnowledge = null;
        for (Knowledge knowledge : treeNodes) {
            if (rootId.equals(knowledge.getId())) {
                rootKnowledge = knowledge;
                break;
            }
        }
        if (rootKnowledge == null) {
            return null;
        }

        // 3. 查询用户的掌握度映射（如果提供了 userId，只查询当前root下的节点）
        Map<String, BigDecimal> masteryMap = new HashMap<>();
        if (userId != null) {
            masteryMap = getUserMasteryMap(userId, treeNodes);
        }

        // 4. 构建父子关系映射（parentId -> List<Knowledge>）
        Map<String, List<Knowledge>> childrenMap = new HashMap<>();
        for (Knowledge knowledge : treeNodes) {
            String parentId = knowledge.getParentId();
            if (parentId == null || parentId.isEmpty()) {
                parentId = Constants.ROOT_NODE_ID;
            }
            childrenMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(knowledge);
        }

        // 5. 构建树结构
        return buildKnowledgeTreeNode(rootKnowledge, childrenMap, masteryMap);
    }


    /**
     * 获取用户的掌握度映射
     *
     * @param userId 用户ID
     * @param knowledgeList 知识点列表（可选，如果提供则只查询这些节点的掌握度）
     * @return 知识点ID -> 掌握度的映射
     */
    private Map<String, BigDecimal> getUserMasteryMap(Long userId, List<Knowledge> knowledgeList) {
        // 1. 查询用户所有掌握度
        List<UserMastery> masteryList = userMasteryRepository.findByUserId(userId);

        // 2. 转为全量掌握度映射
        Map<String, BigDecimal> fullMasteryMap = masteryList.stream()
                .filter(m -> m.getKnowledgeId() != null && m.getProficiency() != null)
                .collect(Collectors.toMap(
                        UserMastery::getKnowledgeId,
                        UserMastery::getProficiency,
                        (existing, replacement) -> existing // 保留第一个（理论上不会冲突）
                ));

        // 3. 如果指定了 knowledgeList，只保留其中的 ID
        if (knowledgeList != null && !knowledgeList.isEmpty()) {
            Set<String> requestedIds = knowledgeList.stream()
                    .map(Knowledge::getId) // ✅ 修正：Knowledge 类应有 getId()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            // 4. 过滤出请求的 ID（保留已存在的，缺失的不补 null）
            return fullMasteryMap.entrySet().stream()
                    .filter(entry -> requestedIds.contains(entry.getKey()))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue
                    ));
        }

        return fullMasteryMap;
    }

    /**
     * 获取用户的掌握度映射（查询所有知识点）
     *
     * @param userId 用户ID
     * @return 知识点ID -> 掌握度的映射
     */
    private Map<String, BigDecimal> getUserMasteryMap(Long userId) {
        return getUserMasteryMap(userId, null);
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
        node.setImportance(knowledge.getImportance() != null ? knowledge.getImportance() : Constants.DEFAULT_IMPORTANCE);
        node.setLevel(knowledge.getLevel() != null ? knowledge.getLevel() : Constants.DEFAULT_LEVEL);

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
     * @throws IllegalArgumentException 如果参数无效
     * @throws RuntimeException 如果 LLM 调用失败或解析失败
     */
    @Transactional
    public void initKnowledgeTree(String knowledgeRoot, Integer count) {
        if (knowledgeRoot == null || knowledgeRoot.trim().isEmpty()) {
            throw new IllegalArgumentException("知识树根节点名称不能为空");
        }
        if (count == null || count <= 0) {
            throw new IllegalArgumentException("节点数量必须大于0");
        }
        
        try {
            // 1. 调用 PromptService 生成 prompt
            Map<String, Object> params = new HashMap<>();
            params.put("knowledgeRoot", knowledgeRoot);
            params.put("count", count.toString());
            String prompt = promptService.render("KNOWLEDGE_TREE_GEN", params);

            // 2. 调用 专用于知识树初始化 的 LLM 客户端获取知识树
            String llmResponse = reasonerLlmClient.call(prompt);

            // 3. 解析 LLM 返回的 JSON
            AiResponse<KnowledgeTreeNode> resp = objectMapper.readValue(llmResponse, new TypeReference<>() {});
            KnowledgeTreeNode rootNode = resp.getData();

            if (rootNode == null) {
                throw new RuntimeException("LLM 返回的知识树为空");
            }

            // 4. 递归保存知识树到数据库
            List<Knowledge> all = knowledgeRepository.findAll();
            Set<String> existingIds = all.stream().map(Knowledge::getId).collect(Collectors.toSet());
            List<Knowledge> needSaveKnowledge = new ArrayList<>();
            saveKnowledgeTreeRecursive(rootNode, null, 1, needSaveKnowledge, existingIds);
            knowledgeRepository.save(needSaveKnowledge);
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
    private void saveKnowledgeTreeRecursive(KnowledgeTreeNode node, String parentId, int level, List<Knowledge> needSaveKnowledge, Set<String> existingIds) {


        if (node == null || node.getId() == null || existingIds.contains(node.getId())) {
            return;
        }

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
        needSaveKnowledge.add(knowledge);
        existingIds.add(knowledge.getId());


        // 递归保存子节点
        if (node.getChildren() != null && !node.getChildren().isEmpty()) {
            for (KnowledgeTreeNode child : node.getChildren()) {
                // 构建子节点的 ID：如果子节点 ID 不是以父节点 ID 开头的完整路径，则拼接
                String childId = child.getId();
                String parentNodeId = node.getId();
                
                // 如果子节点 ID 不是以父节点 ID 开头的完整路径，则拼接
                if (!childId.startsWith(parentNodeId + Constants.KNOWLEDGE_ID_SEPARATOR) && !childId.equals(parentNodeId)) {
                    childId = parentNodeId + Constants.KNOWLEDGE_ID_SEPARATOR + childId;
                    child.setId(childId);
                }
                
                saveKnowledgeTreeRecursive(child, node.getId(), level + 1, needSaveKnowledge, existingIds);
            }
        }
    }



}
