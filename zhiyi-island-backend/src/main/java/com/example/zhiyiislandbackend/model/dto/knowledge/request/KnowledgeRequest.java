package com.example.zhiyiislandbackend.model.dto.knowledge.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 知识库请求DTO
 * 用于创建和更新知识库
 */
@Data
public class KnowledgeRequest {
    /** 标题 */
    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200个字符")
    private String title;
    /** 描述 */
    private String description;
    /** 分类 */
    private Integer category;
}
