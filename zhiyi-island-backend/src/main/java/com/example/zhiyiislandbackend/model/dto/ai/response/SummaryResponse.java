package com.example.zhiyiislandbackend.model.dto.ai.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI摘要响应DTO
 * 返回AI生成的摘要信息
 */
@Data
public class SummaryResponse {
    /**
     * 摘要ID
     */
    private Long id;
    /**
     * 原内容标题
     */
    private String title;
    /**
     * 生成的摘要内容
     */
    private String summaryContent;
    /**
     * 摘要风格
     */
    private String summaryStyle;
    /**
     * 摘要长度
     */
    private String summaryLength;
    /**
     * 原文字数
     */
    private Integer wordCount;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
