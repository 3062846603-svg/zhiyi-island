package com.example.zhiyiislandbackend.model.entity;

import com.example.zhiyiislandbackend.model.enums.BooleanEnum;
import com.example.zhiyiislandbackend.model.enums.NoteCategoryEnum;
import com.example.zhiyiislandbackend.model.enums.NoteStatusEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 笔记实体类
 * 存储用户笔记内容、分类和状态信息
 */
@Data
public class Note {
    /** 笔记ID */
    private Long id;
    /** 所属用户ID */
    private Long userId;
    /** 所属知识库ID */
    private Long knowledgeId;

    /** 标题 */
    private String title;
    /** 内容 */
    private String content;

    /** 分类 */
    private NoteCategoryEnum category;
    /** 状态 */
    private NoteStatusEnum status;

    /** 字数 */
    private Integer wordCount;
    /** 来源 */
    private String source;

    /** 向量嵌入（用于语义搜索） */
    private float[] embedding;

    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
}
