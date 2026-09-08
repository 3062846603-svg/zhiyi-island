package com.example.zhiyiislandbackend.model.dto.ai.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI语义问答响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiSemanticChatResponse {

    /**
     * AI生成的答案
     */
    private String answer;

    /**
     * 答案来源类型：note-笔记，knowledge-知识库，ai-AI生成
     */
    private String sourceType;

    /**
     * 相关内容列表
     */
    private List<RelatedContent> relatedContents;

    /**
     * 关键词提取结果
     */
    private List<String> keywords;

    /**
     * 意图识别结果
     */
    private String intent;

    /**
     * 置信度（0-1）
     */
    private Double confidence;

    /**
     * 相关内容
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RelatedContent {
        /**
         * 内容ID
         */
        private Long id;

        /**
         * 内容类型：note-笔记，knowledge-知识库
         */
        private String type;

        /**
         * 标题
         */
        private String title;

        /**
         * 内容摘要
         */
        private String summary;

        /**
         * 相关度（0-1）
         */
        private Double relevance;
    }
}
