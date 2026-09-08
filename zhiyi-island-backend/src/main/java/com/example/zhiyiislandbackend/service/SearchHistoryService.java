package com.example.zhiyiislandbackend.service;

import com.example.zhiyiislandbackend.model.entity.SearchHistory;

import java.util.List;

/**
 * 搜索历史服务接口
 * 定义搜索历史记录的管理操作，包括记录、查询和删除
 */
public interface SearchHistoryService {

    /**
     * 记录搜索历史（默认类型）
     *
     * @param userId  用户ID
     * @param keyword 搜索关键词
     */
    void record(Long userId, String keyword);

    /**
     * 记录搜索历史（指定类型）
     *
     * @param userId     用户ID
     * @param keyword    搜索关键词
     * @param searchType 搜索类型
     */
    void record(Long userId, String keyword, String searchType);

    /**
     * 获取用户最近的搜索历史
     *
     * @param userId 用户ID
     * @param limit  返回数量限制
     * @return 搜索历史列表
     */
    List<SearchHistory> getRecent(Long userId, int limit);

    /**
     * 获取用户指定类型的最近搜索历史
     *
     * @param userId     用户ID
     * @param limit      返回数量限制
     * @param searchType 搜索类型
     * @return 搜索历史列表
     */
    List<SearchHistory> getRecentByType(Long userId, int limit, String searchType);

    /**
     * 删除单条搜索历史
     *
     * @param id     搜索历史ID
     * @param userId 用户ID（用于权限验证）
     */
    void delete(Long id, Long userId);

    /**
     * 清空用户所有搜索历史
     *
     * @param userId 用户ID
     */
    void clear(Long userId);
}
