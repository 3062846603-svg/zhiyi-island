package com.example.zhiyiislandbackend.service;

import com.example.zhiyiislandbackend.model.dto.note.request.NoteRequest;
import com.example.zhiyiislandbackend.model.dto.note.request.SummaryOptionRequest;
import com.example.zhiyiislandbackend.model.entity.Note;

import java.util.List;

/**
 * 笔记服务接口
 * 定义笔记相关的业务操作，包括增删改查、搜索等功能
 */
public interface NoteService {

    /**
     * 创建笔记
     *
     * @param userId  用户ID
     * @param request 笔记请求信息
     * @return 创建的笔记实体
     */
    Note create(Long userId, NoteRequest request);

    /**
     * 更新笔记
     *
     * @param id      笔记ID
     * @param request 笔记请求信息
     * @return 更新后的笔记实体
     */
    Note update(Long id, NoteRequest request);

    /**
     * 删除笔记
     *
     * @param id     笔记ID
     * @param userId 用户ID（用于权限验证）
     */
    void delete(Long id, Long userId);

    /**
     * 根据ID获取笔记
     *
     * @param id 笔记ID
     * @return 笔记实体
     */
    Note getById(Long id);

    /**
     * 获取用户的所有笔记
     *
     * @param userId 用户ID
     * @return 笔记列表
     */
    List<Note> getByUserId(Long userId);

    /**
     * 获取用户指定分类的笔记
     *
     * @param userId   用户ID
     * @param category 分类名称
     * @return 笔记列表
     */
    List<Note> getByUserIdAndCategory(Long userId, String category);

    /**
     * 获取知识库下的所有笔记
     *
     * @param knowledgeId 知识库ID
     * @return 笔记列表
     */
    List<Note> getByKnowledgeId(Long knowledgeId);

    /**
     * 搜索笔记
     *
     * @param userId  用户ID
     * @param keyword 搜索关键词
     * @return 匹配的笔记列表
     */
    List<Note> search(Long userId, String keyword);

    /**
     * 更新笔记所属知识库
     *
     * @param id          笔记ID
     * @param knowledgeId 知识库ID（可为空表示移出知识库）
     * @param userId      用户ID（用于权限验证）
     */
    void updateKnowledgeId(Long id, Long knowledgeId, Long userId);

    /**
     * 统计用户笔记数量
     *
     * @param userId 用户ID
     * @return 笔记数量
     */
    int countByUserId(Long userId);

    /**
     * 统计知识库下笔记数量
     *
     * @param knowledgeId 知识库ID
     * @return 笔记数量
     */
    int countByKnowledgeId(Long knowledgeId);

    /**
     * 生成AI摘要
     *
     * @param id     笔记ID
     * @param userId 用户ID（用于权限验证）
     * @return 更新后的笔记实体
     */
    Note generateSummary(Long id, Long userId);

    /**
     * 生成AI摘要（带选项）
     *
     * @param id      笔记ID
     * @param userId  用户ID（用于权限验证）
     * @param options 摘要选项
     * @return 更新后的笔记实体
     */
    Note generateSummary(Long id, Long userId, SummaryOptionRequest options);

    /**
     * 获取用户的草稿列表
     *
     * @param userId 用户ID
     * @return 草稿列表
     */
    List<Note> getDrafts(Long userId);

    /**
     * 统计用户草稿数量
     *
     * @param userId 用户ID
     * @return 草稿数量
     */
    int countDrafts(Long userId);

    /**
     * 发布草稿
     *
     * @param id     笔记ID
     * @param userId 用户ID（用于权限验证）
     */
    void publishDraft(Long id, Long userId);
}
