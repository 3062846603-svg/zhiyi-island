package com.example.zhiyiislandbackend.model.dto.ai.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI知识提取响应
 * 用于封装AI从笔记中提取的知识点
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeExtractionResponse {

    /**
     * 建议的知识库标题
     */
    private String knowledgeTitle;

    /**
     * 建议的知识库分类
     */
    private String knowledgeCategory;

    /**
     * 提取的知识点列表
     */
    private List<KnowledgePoint> knowledgePoints;

    /**
     * 知识点
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KnowledgePoint {
        /**
         * 知识点标题
         */
        private String title;

        /**
         * 知识点内容
         */
        private String content;

        /**
         * 知识点来源（笔记中的具体位置或上下文）
         */
        private String source;
    }
}
