package com.example.quizmeup.service;

import com.example.quizmeup.dto.KnowledgeProgressNode;
import com.example.quizmeup.entity.Knowledge;
import com.example.quizmeup.entity.UserMastery;
import com.example.quizmeup.mapper.KnowledgeMapper;
import com.example.quizmeup.mapper.UserMasteryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 学习进度服务类
 */
@Service
public class ProgressService {

    @Autowired
    private KnowledgeMapper knowledgeMapper;

    @Autowired
    private UserMasteryMapper userMasteryMapper;

    /**
     * 获取用户的知识树进度
     *
     * @param userId 用户ID
     * @return 知识树根节点列表（支持多根节点）
     */
    public List<KnowledgeProgressNode> getProgressTree(Long userId) {
        // 1. 查询所有知识点
        List<Knowledge> allKnowledge = knowledgeMapper.selectList(null);

        // 2. 查询用户的所有掌握度记录
        Map<String, BigDecimal> masteryMap = getUserMasteryMap(userId);

        // 3. 构建知识点映射（id -> Knowledge）
        Map<String, Knowledge> knowledgeMap = allKnowledge.stream()
                .collect(Collectors.toMap(Knowledge::getId, k -> k));

        // 4. 构建父子关系映射（parentId -> List<Knowledge>）
        Map<String, List<Knowledge>> childrenMap = new HashMap<>();
        for (Knowledge knowledge : allKnowledge) {
            String parentId = knowledge.getParentId();
            if (parentId == null) {
                parentId = "ROOT"; // 根节点使用特殊标识
            }
            childrenMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(knowledge);
        }

        // 5. 构建树结构并计算 proficiency
        List<Knowledge> rootNodes = childrenMap.getOrDefault("ROOT", new ArrayList<>());
        List<KnowledgeProgressNode> result = new ArrayList<>();

        for (Knowledge rootNode : rootNodes) {
            KnowledgeProgressNode node = buildNodeTree(rootNode, knowledgeMap, childrenMap, masteryMap);
            result.add(node);
        }

        return result;
    }

    /**
     * 获取用户的掌握度映射
     * 只查询叶节点的掌握度（非叶节点通过加权平均计算得出）
     *
     * @param userId 用户ID
     * @return 知识点ID -> 掌握度的映射（仅包含叶节点）
     */
    private Map<String, BigDecimal> getUserMasteryMap(Long userId) {
        Map<String, BigDecimal> masteryMap = new HashMap<>();

        // 查询所有知识点
        List<Knowledge> allKnowledge = knowledgeMapper.selectList(null);

        // 只查询叶节点的掌握度（非叶节点通过计算得出）
        for (Knowledge knowledge : allKnowledge) {
            if (Boolean.TRUE.equals(knowledge.getIsLeaf())) {
                UserMastery mastery = userMasteryMapper.selectByUserIdAndKnowledgeId(userId, knowledge.getId());
                if (mastery != null && mastery.getProficiency() != null) {
                    masteryMap.put(knowledge.getId(), mastery.getProficiency());
                } else {
                    // 没有掌握度记录，默认为 0
                    masteryMap.put(knowledge.getId(), BigDecimal.ZERO);
                }
            }
        }

        return masteryMap;
    }

    /**
     * 递归构建节点树并计算 proficiency
     *
     * @param knowledge   当前知识点
     * @param knowledgeMap 知识点映射
     * @param childrenMap  子节点映射
     * @param masteryMap   掌握度映射（仅叶节点）
     * @return 节点树
     */
    private KnowledgeProgressNode buildNodeTree(Knowledge knowledge,
                                                 Map<String, Knowledge> knowledgeMap,
                                                 Map<String, List<Knowledge>> childrenMap,
                                                 Map<String, BigDecimal> masteryMap) {
        KnowledgeProgressNode node = new KnowledgeProgressNode();
        node.setId(knowledge.getId());
        node.setName(knowledge.getName());
        node.setDescription(knowledge.getDescription());
        node.setIsLeaf(knowledge.getIsLeaf());
        node.setImportance(knowledge.getImportance() != null ? knowledge.getImportance() : 3);
        node.setLevel(knowledge.getLevel() != null ? knowledge.getLevel() : 1);

        // 获取子节点
        List<Knowledge> children = childrenMap.getOrDefault(knowledge.getId(), new ArrayList<>());

        if (children.isEmpty()) {
            // 叶节点：直接查询掌握度
            BigDecimal proficiency = masteryMap.getOrDefault(knowledge.getId(), BigDecimal.ZERO);
            node.setProficiency(proficiency.setScale(2, RoundingMode.HALF_UP));
            node.setChildren(new ArrayList<>());
        } else {
            // 非叶节点：递归构建子节点树
            List<KnowledgeProgressNode> childNodes = new ArrayList<>();
            for (Knowledge child : children) {
                KnowledgeProgressNode childNode = buildNodeTree(child, knowledgeMap, childrenMap, masteryMap);
                childNodes.add(childNode);
            }
            node.setChildren(childNodes);

            // 计算加权平均：Σ(child.proficiency × child.importance) / Σ(child.importance)
            BigDecimal proficiency = calculateWeightedProficiency(childNodes);
            node.setProficiency(proficiency.setScale(2, RoundingMode.HALF_UP));
        }

        return node;
    }

    /**
     * 计算非叶节点的加权平均掌握度
     * 公式：Σ(child.proficiency × child.importance) / Σ(child.importance)
     *
     * @param childNodes 子节点列表
     * @return 加权平均掌握度
     */
    private BigDecimal calculateWeightedProficiency(List<KnowledgeProgressNode> childNodes) {
        if (childNodes == null || childNodes.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalWeightedSum = BigDecimal.ZERO;
        BigDecimal totalWeight = BigDecimal.ZERO;

        for (KnowledgeProgressNode child : childNodes) {
            BigDecimal proficiency = child.getProficiency() != null ? child.getProficiency() : BigDecimal.ZERO;
            int importance = child.getImportance() != null ? child.getImportance() : 3;

            BigDecimal weight = BigDecimal.valueOf(importance);
            BigDecimal weightedProficiency = proficiency.multiply(weight);

            totalWeightedSum = totalWeightedSum.add(weightedProficiency);
            totalWeight = totalWeight.add(weight);
        }

        // 避免除零
        if (totalWeight.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        // 计算加权平均
        return totalWeightedSum.divide(totalWeight, 2, RoundingMode.HALF_UP);
    }
}
