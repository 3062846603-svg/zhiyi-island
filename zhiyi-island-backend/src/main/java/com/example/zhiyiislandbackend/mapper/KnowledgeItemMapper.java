package com.example.zhiyiislandbackend.mapper;

import com.example.zhiyiislandbackend.model.entity.KnowledgeItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 知识条目数据访问接口
 * 提供知识条目表的CRUD操作
 */
@Mapper
public interface KnowledgeItemMapper {

    /**
     * 插入新知识条目
     */
    int insert(KnowledgeItem item);

    /**
     * 更新知识条目
     */
    int updateById(KnowledgeItem item);

    /**
     * 删除知识条目
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据ID查询知识条目
     */
    KnowledgeItem selectById(@Param("id") Long id);

    /**
     * 查询知识库下的所有条目
     */
    List<KnowledgeItem> selectByKnowledgeId(@Param("knowledgeId") Long knowledgeId);

    /**
     * 查询来源于某笔记的所有条目
     */
    List<KnowledgeItem> selectByNoteId(@Param("noteId") Long noteId);

    /**
     * 统计知识库下的条目数量
     */
    int countByKnowledgeId(@Param("knowledgeId") Long knowledgeId);

    /**
     * 统计来源于某笔记的条目数量
     */
    int countByNoteId(@Param("noteId") Long noteId);

    /**
     * 统计用户所有知识条目数量
     */
    int countByUserId(@Param("userId") Long userId);
}
