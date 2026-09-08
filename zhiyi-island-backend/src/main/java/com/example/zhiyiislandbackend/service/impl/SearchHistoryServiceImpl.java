package com.example.zhiyiislandbackend.service.impl;

import com.example.zhiyiislandbackend.model.entity.SearchHistory;
import com.example.zhiyiislandbackend.exception.BusinessException;
import com.example.zhiyiislandbackend.mapper.SearchHistoryMapper;
import com.example.zhiyiislandbackend.service.SearchHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 搜索历史服务实现类
 * 管理用户的搜索历史记录，支持记录、查询和删除操作
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchHistoryServiceImpl implements SearchHistoryService {

    private final SearchHistoryMapper searchHistoryMapper;

    /**
     * 记录用户搜索历史
     *
     * @param userId  用户ID
     * @param keyword 搜索关键词
     */
    @Override
    public void record(Long userId, String keyword) {
        record(userId, keyword, "search");
    }

    @Override
    public void record(Long userId, String keyword, String searchType) {
        log.info("记录搜索历史，用户ID：{}，关键词：{}，类型：{}", userId, keyword, searchType);

        SearchHistory history = new SearchHistory();
        history.setUserId(userId);
        history.setKeyword(keyword);
        history.setSearchType(searchType);
        history.setCreateTime(LocalDateTime.now());

        searchHistoryMapper.insert(history);
    }

    /**
     * 获取用户最近的搜索历史
     * 按创建时间倒序返回
     *
     * @param userId 用户ID
     * @param limit  返回数量限制
     * @return 搜索历史列表
     */
    @Override
    public List<SearchHistory> getRecent(Long userId, int limit) {
        log.debug("获取最近搜索历史，用户ID：{}，数量：{}", userId, limit);
        return searchHistoryMapper.selectByUserId(userId, limit);
    }

    @Override
    public List<SearchHistory> getRecentByType(Long userId, int limit, String searchType) {
        log.debug("获取指定类型搜索历史，用户ID：{}，数量：{}，类型：{}", userId, limit, searchType);
        return searchHistoryMapper.selectByUserIdAndType(userId, limit, searchType);
    }

    /**
     * 删除单条搜索历史
     * 验证历史记录归属后再进行删除
     *
     * @param id     搜索历史ID
     * @param userId 用户ID
     */
    @Override
    public void delete(Long id, Long userId) {
        log.info("删除搜索历史，ID：{}，用户ID：{}", id, userId);
        SearchHistory history = searchHistoryMapper.selectById(id);
        if (history == null) {
            throw new BusinessException(400, "搜索历史不存在");
        }
        if (!history.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权删除此搜索历史");
        }
        searchHistoryMapper.deleteById(id);
    }

    /**
     * 清空用户所有搜索历史
     *
     * @param userId 用户ID
     */
    @Override
    public void clear(Long userId) {
        log.info("清空搜索历史，用户ID：{}", userId);
        searchHistoryMapper.deleteByUserId(userId);
    }
}
