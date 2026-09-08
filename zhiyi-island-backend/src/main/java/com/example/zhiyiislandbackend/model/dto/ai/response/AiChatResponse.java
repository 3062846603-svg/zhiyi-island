package com.example.zhiyiislandbackend.model.dto.ai.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI问答响应DTO
 * 返回AI对话问答的结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatResponse {
    /**
     * AI回答内容
     */
    private String answer;
}
