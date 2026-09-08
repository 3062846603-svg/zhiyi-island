package com.example.zhiyiislandbackend.model.dto.note.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI摘要响应DTO
 * 返回笔记的AI生成摘要
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiSummaryResponse {
    /** 摘要ID */
    private Long id;
    /** 关联笔记ID */
    private Long noteId;
    /** 摘要内容 */
    private String summaryContent;
    /** 摘要风格 */
    private String summaryStyle;
    /** 摘要长度 */
    private String summaryLength;
    /** 创建时间 */
    private LocalDateTime createTime;

    /**
     * 从实体转换为响应DTO
     */
    public static AiSummaryResponse fromEntity(com.example.zhiyiislandbackend.model.entity.AiSummary summary) {
        if (summary == null) return null;

        return AiSummaryResponse.builder()
                .id(summary.getId())
                .noteId(summary.getNoteId())
                .summaryContent(summary.getSummaryContent())
                .summaryStyle(summary.getSummaryStyle())
                .summaryLength(summary.getSummaryLength())
                .createTime(summary.getCreateTime())
                .build();
    }
}
