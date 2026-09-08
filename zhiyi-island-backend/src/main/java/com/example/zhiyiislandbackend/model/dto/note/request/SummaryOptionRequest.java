package com.example.zhiyiislandbackend.model.dto.note.request;

import lombok.Data;

/**
 * AI摘要选项请求DTO
 * 用于生成笔记摘要时的选项配置
 */
@Data
public class SummaryOptionRequest {
    /**
     * 摘要风格：keypoints-要点式，paragraph-段落式，outline-大纲式
     */
    private String style = "keypoints";

    /**
     * 摘要长度：short-简短，medium-中等，detailed-详细
     */
    private String length = "medium";
}
