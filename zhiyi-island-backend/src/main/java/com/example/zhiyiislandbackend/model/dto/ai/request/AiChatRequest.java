package com.example.zhiyiislandbackend.model.dto.ai.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * AI问答请求DTO
 * 用于搜索详情页的AI对话问答
 */
@Data
public class AiChatRequest {
    /**
     * 用户问题
     */
    @NotBlank(message = "问题不能为空")
    private String question;

    /**
     * 上下文内容（笔记或知识库的内容）
     */
    private String context;

    /**
     * 对话历史，格式为 [{"role":"user","content":"..."},{"role":"assistant","content":"..."}]
     */
    private List<ChatMessage> history;

    @Data
    public static class ChatMessage {
        private String role;
        private String content;
    }
}
