package com.example.zhiyiislandbackend.mapper;

import com.example.zhiyiislandbackend.model.entity.Note;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 笔记数据访问接口
 * 提供笔记表的CRUD操作、搜索和向量检索功能
 */
@Mapper
public interface NoteMapper {

    /**
     * 插入新笔记
     */
    int insert(Note note);

    /**
     * 更新笔记
     */
    int updateById(Note note);

    /**
     * 删除笔记
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据ID查询笔记
     */
    Note selectById(@Param("id") Long id);

    /**
     * 查询用户所有笔记
     */
    List<Note> selectByUserId(@Param("userId") Long userId);

    /**
     * 按分类查询用户笔记
     */
    List<Note> selectByUserIdAndCategory(@Param("userId") Long userId, @Param("category") String category);

    /**
     * 查询知识库下的所有笔记
     */
    List<Note> selectByKnowledgeId(@Param("knowledgeId") Long knowledgeId);

    /**
     * 关键词搜索笔记
     */
    List<Note> searchByKeyword(@Param("userId") Long userId, @Param("keyword") String keyword);

    /**
     * 查询用户草稿列表
     */
    List<Note> selectDraftsByUserId(@Param("userId") Long userId);

    /**
     * 更新笔记状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 更新笔记所属知识库
     */
    int updateKnowledgeId(@Param("id") Long id, @Param("knowledgeId") Long knowledgeId);

    /**
     * 统计用户笔记数量
     */
    int countByUserId(@Param("userId") Long userId);

    /**
     * 统计知识库笔记数量
     */
    int countByKnowledgeId(@Param("knowledgeId") Long knowledgeId);

    /**
     * 统计用户草稿数量
     */
    int countDraftsByUserId(@Param("userId") Long userId);

    /**
     * 更新笔记向量嵌入
     */
    int updateEmbedding(@Param("id") Long id, @Param("embedding") float[] embedding);

    /**
     * 向量搜索笔记
     */
    List<Note> searchByVector(@Param("userId") Long userId, @Param("embedding") float[] embedding, @Param("limit") int limit);

    /**
     * 带阈值的向量搜索
     */
    List<Note> searchByVectorWithThreshold(@Param("userId") Long userId, @Param("embedding") float[] embedding,
                                           @Param("threshold") double threshold, @Param("limit") int limit);

    /**
     * 查询没有向量嵌入的笔记
     */
    List<Note> selectNotesWithoutEmbedding(@Param("userId") Long userId, @Param("limit") int limit);
}
