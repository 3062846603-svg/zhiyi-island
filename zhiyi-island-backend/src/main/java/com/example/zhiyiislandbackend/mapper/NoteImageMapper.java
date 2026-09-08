package com.example.zhiyiislandbackend.mapper;

import com.example.zhiyiislandbackend.model.entity.NoteImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 笔记图片数据访问接口
 * 提供笔记图片表的CRUD操作
 */
@Mapper
public interface NoteImageMapper {

    /**
     * 插入单条笔记图片
     */
    void insert(NoteImage noteImage);

    /**
     * 批量插入笔记图片
     */
    void insertBatch(@Param("list") List<NoteImage> list);

    /**
     * 删除笔记的所有图片
     */
    void deleteByNoteId(@Param("noteId") Long noteId);

    /**
     * 查询笔记的所有图片
     */
    List<NoteImage> selectByNoteId(@Param("noteId") Long noteId);

    /**
     * 根据ID查询图片
     */
    NoteImage selectById(@Param("id") Long id);

    /**
     * 删除单条图片
     */
    void deleteById(@Param("id") Long id);
}
