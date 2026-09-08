package com.example.zhiyiislandbackend.mapper;

import com.example.zhiyiislandbackend.model.entity.SearchHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 搜索历史数据访问接口
 * 提供搜索历史表的CRUD操作
 */
@Mapper
public interface SearchHistoryMapper {

    /**
     * 插入搜索历史记录
     */
    int insert(SearchHistory searchHistory);

    /**
     * 查询用户最近的搜索历史
     */
    List<SearchHistory> selectByUserId(@Param("userId") Long userId, @Param("limit") Integer limit);

    /**
     * 按类型查询用户搜索历史
     */
    List<SearchHistory> selectByUserIdAndType(@Param("userId") Long userId, @Param("limit") Integer limit, @Param("searchType") String searchType);

    /**
     * 根据ID查询搜索历史
     */
    SearchHistory selectById(@Param("id") Long id);

    /**
     * 清空用户搜索历史
     */
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 删除单条搜索历史
     */
    int deleteById(@Param("id") Long id);
}
