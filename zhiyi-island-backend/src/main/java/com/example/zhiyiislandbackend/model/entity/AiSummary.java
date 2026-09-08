package com.example.zhiyiislandbackend.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI摘要实体类
 * 存储笔记的AI生成摘要
 */
@Data
public class AiSummary {
    /** 摘要ID */
    private Long id;
    /** 关联笔记ID */
    private Long noteId;
    /** 用户ID */
    private Long userId;

    /** 摘要内容 */
    private String summaryContent;
    /** 摘要类型 */
    private String summaryType;
    /** 摘要风格 */
    private String summaryStyle;
    /** 摘要长度 */
    private String summaryLength;

    /** 原文字数 */
    private Integer originalWordCount;
    /** 摘要字数 */
    private Integer summaryWordCount;

    /** 创建时间 */
    private LocalDateTime createTime;
}
