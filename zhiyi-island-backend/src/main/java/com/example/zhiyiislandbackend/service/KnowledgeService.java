package com.example.zhiyiislandbackend.service;

import com.example.zhiyiislandbackend.model.dto.knowledge.request.KnowledgeItemRequest;
import com.example.zhiyiislandbackend.model.dto.knowledge.request.KnowledgeRequest;
import com.example.zhiyiislandbackend.model.entity.Knowledge;
import com.example.zhiyiislandbackend.model.entity.KnowledgeItem;

import java.util.List;

/**
 * 知识库服务接口
 * 定义知识库和知识条目相关的业务操作
 */
public interface KnowledgeService {

    /**
     * 创建知识库
     *
     * @param userId  用户ID
     * @param request 知识库请求信息
     * @return 创建的知识库实体
     */
    Knowledge create(Long userId, KnowledgeRequest request);

    /**
     * 更新知识库
     *
     * @param id      知识库ID
     * @param request 知识库请求信息
     * @return 更新后的知识库实体
     */
    Knowledge update(Long id, KnowledgeRequest request);

    /**
     * 删除知识库
     *
     * @param id     知识库ID
     * @param userId 用户ID（用于权限验证）
     */
    void delete(Long id, Long userId);

    /**
     * 根据ID获取知识库
     *
     * @param id 知识库ID
     * @return 知识库实体
     */
    Knowledge getById(Long id);

    /**
     * 获取用户的所有知识库
     *
     * @param userId 用户ID
     * @return 知识库列表
     */
    List<Knowledge> getByUserId(Long userId);

    /**
     * 获取用户指定分类的知识库
     *
     * @param userId   用户ID
     * @param category 分类名称
     * @return 知识库列表
     */
    List<Knowledge> getByUserIdAndCategory(Long userId, String category);

    /**
     * 创建知识条目
     *
     * @param knowledgeId 知识库ID
     * @param userId      用户ID（用于权限验证）
     * @param request     知识条目请求信息
     * @return 创建的知识条目实体
     */
    KnowledgeItem createItem(Long knowledgeId, Long userId, KnowledgeItemRequest request);

    /**
     * 更新知识条目
     *
     * @param itemId  知识条目ID
     * @param userId  用户ID（用于权限验证）
     * @param request 知识条目请求信息
     * @return 更新后的知识条目实体
     */
    KnowledgeItem updateItem(Long itemId, Long userId, KnowledgeItemRequest request);

    /**
     * 删除知识条目
     *
     * @param itemId 知识条目ID
     * @param userId 用户ID（用于权限验证）
     */
    void deleteItem(Long itemId, Long userId);

    /**
     * 获取知识库下的所有知识条目
     *
     * @param knowledgeId 知识库ID
     * @return 知识条目列表
     */
    List<KnowledgeItem> getItemsByKnowledgeId(Long knowledgeId);

    /**
     * 获取来源于某笔记的所有知识条目
     *
     * @param noteId 笔记ID
     * @return 知识条目列表
     */
    List<KnowledgeItem> getItemsByNoteId(Long noteId);

    /**
     * 更新知识库笔记数量
     *
     * @param knowledgeId 知识库ID
     */
    void updateNoteCount(Long knowledgeId);

    /**
     * 统计用户知识库数量
     *
     * @param userId 用户ID
     * @return 知识库数量
     */
    int countByUserId(Long userId);

    /**
     * 统计用户知识条目数量
     *
     * @param userId 用户ID
     * @return 知识条目数量
     */
    int countItemsByUserId(Long userId);

    /**
     * 异步处理笔记的知识提取
     * 在笔记创建后自动调用，提取知识点并创建知识库
     *
     * @param noteId 笔记ID
     * @param userId 用户ID
     */
    void processNoteKnowledgeExtraction(Long noteId, Long userId);
}
