package com.example.zhiyiislandbackend.model.dto.ai.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI语义问答请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiSemanticChatRequest {

    /**
     * 用户问题
     */
    private String question;

    /**
     * 上下文内容（可选）
     */
    private String context;

    /**
     * 搜索类型：note-笔记，knowledge-知识库，all-全部
     */
    private String searchType;

    /**
     * 是否返回相关内容
     */
    private Boolean returnRelatedContent;
}
