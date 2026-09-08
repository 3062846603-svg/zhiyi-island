package com.example.zhiyiislandbackend.mapper;

import com.example.zhiyiislandbackend.model.entity.AiSummary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI摘要数据访问接口
 * 提供AI摘要表的CRUD操作和查询功能
 */
@Mapper
public interface AiSummaryMapper {

    /**
     * 插入新AI摘要
     */
    int insert(AiSummary aiSummary);

    /**
     * 根据ID查询AI摘要
     */
    AiSummary selectById(@Param("id") Long id);

    /**
     * 查询笔记的最新AI摘要
     */
    AiSummary selectLatestByNoteId(@Param("noteId") Long noteId);

    /**
     * 查询笔记的所有AI摘要
     */
    List<AiSummary> selectByNoteId(@Param("noteId") Long noteId);

    /**
     * 查询用户的AI摘要列表
     */
    List<AiSummary> selectByUserId(@Param("userId") Long userId, @Param("limit") Integer limit);

    /**
     * 删除AI摘要
     */
    int deleteById(@Param("id") Long id);

    /**
     * 删除笔记的所有AI摘要
     */
    int deleteByNoteId(@Param("noteId") Long noteId);

    /**
     * 统计用户AI摘要数量
     */
    int countByUserId(@Param("userId") Long userId);

    /**
     * 统计笔记AI摘要数量
     */
    int countByNoteId(@Param("noteId") Long noteId);
}
