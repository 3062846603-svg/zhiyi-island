package com.example.zhiyiislandbackend.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 笔记图片实体类
 * 存储笔记中上传的图片信息
 */
@Data
public class NoteImage {
    /** 图片ID */
    private Long id;
    /** 关联笔记ID */
    private Long noteId;
    /** 图片URL */
    private String imageUrl;
    /** 创建时间 */
    private LocalDateTime createTime;
}
