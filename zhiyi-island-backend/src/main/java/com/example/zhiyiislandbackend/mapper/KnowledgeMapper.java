package com.example.zhiyiislandbackend.mapper;

import com.example.zhiyiislandbackend.model.entity.Knowledge;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 知识库数据访问接口
 * 提供知识库表的CRUD操作和查询功能
 */
@Mapper
public interface KnowledgeMapper {

    /**
     * 插入新知识库
     */
    int insert(Knowledge knowledge);

    /**
     * 更新知识库
     */
    int updateById(Knowledge knowledge);

    /**
     * 删除知识库
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据ID查询知识库
     */
    Knowledge selectById(@Param("id") Long id);

    /**
     * 查询用户所有知识库
     */
    List<Knowledge> selectByUserId(@Param("userId") Long userId);

    /**
     * 按分类查询用户知识库
     */
    List<Knowledge> selectByUserIdAndCategory(@Param("userId") Long userId, @Param("category") String category);

    /**
     * 更新知识库条目数量
     */
    int updateItemCount(@Param("id") Long id, @Param("itemCount") Integer itemCount);

    /**
     * 更新知识库笔记数量
     */
    int updateNoteCount(@Param("id") Long id, @Param("noteCount") Integer noteCount);

    /**
     * 统计用户知识库数量
     */
    int countByUserId(@Param("userId") Long userId);

    /**
     * 查询所有知识库（用于统计校准）
     */
    List<Knowledge> selectAll();
}
