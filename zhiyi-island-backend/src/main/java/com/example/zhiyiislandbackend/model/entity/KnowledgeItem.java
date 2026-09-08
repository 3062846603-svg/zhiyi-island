package com.example.zhiyiislandbackend.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识条目实体类
 * 存储从笔记中提取的知识点
 */
@Data
public class KnowledgeItem {
    /** 条目ID */
    private Long id;
    /** 所属知识库ID */
    private Long knowledgeId;
    /** 来源笔记ID */
    private Long noteId;
    /** 标题 */
    private String title;
    /** 内容 */
    private String content;
    /** 来源 */
    private String source;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
}
