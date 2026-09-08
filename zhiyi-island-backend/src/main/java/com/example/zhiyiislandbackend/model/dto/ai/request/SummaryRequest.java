package com.example.zhiyiislandbackend.model.dto.ai.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI摘要请求DTO
 * 用于请求AI生成内容摘要
 */
@Data
public class SummaryRequest {
    /**
     * 待摘要的内容
     */
    @NotBlank(message = "内容不能为空")
    private String content;

    /**
     * 内容标题（可选）
     */
    private String title;

    /**
     * 摘要风格：keypoints-要点式，narrative-叙述式
     */
    private String style = "keypoints";

    /**
     * 摘要长度：short-简短，medium-中等，long-详细
     */
    private String length = "medium";
}
