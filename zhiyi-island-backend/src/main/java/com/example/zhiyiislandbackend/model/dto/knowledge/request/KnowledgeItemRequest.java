package com.example.zhiyiislandbackend.model.dto.knowledge.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 知识条目请求DTO
 * 用于创建或更新知识库中的知识条目
 */
@Data
public class KnowledgeItemRequest {
    /**
     * 知识条目标题
     */
    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200个字符")
    private String title;

    /**
     * 知识条目内容
     */
    private String content;

    /**
     * 知识来源
     */
    @Size(max = 255, message = "来源长度不能超过255个字符")
    private String source;

    /**
     * 来源笔记ID，可为空
     */
    private Long noteId;
}
