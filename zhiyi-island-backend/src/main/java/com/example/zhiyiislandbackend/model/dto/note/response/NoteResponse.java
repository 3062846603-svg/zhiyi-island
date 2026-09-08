package com.example.zhiyiislandbackend.model.dto.note.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 笔记响应DTO
 * 返回笔记的详细信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteResponse {
    /** 笔记ID */
    private Long id;
    /** 标题 */
    private String title;
    /** 内容 */
    private String content;
    /** 图片URL列表 */
    private List<String> images;
    /** 分类名称 */
    private String category;
    /** 状态码 */
    private Integer status;
    /** 状态文本 */
    private String statusText;
    /** 字数 */
    private Integer wordCount;
    /** 来源 */
    private String source;
    /** 所属知识库ID */
    private Long knowledgeId;
    /** 所属知识库名称 */
    private String knowledgeName;
    /** AI摘要 */
    private AiSummaryResponse aiSummary;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;

    /**
     * 从实体转换为响应DTO
     */
    public static NoteResponse fromEntity(com.example.zhiyiislandbackend.model.entity.Note note) {
        if (note == null) return null;

        return NoteResponse.builder()
                .id(note.getId())
                .title(note.getTitle())
                .content(note.getContent())
                .category(note.getCategory() != null ? note.getCategory().getDescription() : null)
                .status(note.getStatus() != null ? note.getStatus().getCode() : 1)
                .statusText(note.getStatus() != null ? note.getStatus().getDescription() : "已发布")
                .wordCount(note.getWordCount())
                .source(note.getSource())
                .knowledgeId(note.getKnowledgeId())
                .createTime(note.getCreateTime())
                .updateTime(note.getUpdateTime())
                .build();
    }

    /**
     * 从实体转换为响应DTO（包含知识库名称）
     */
    public static NoteResponse fromEntityWithKnowledge(com.example.zhiyiislandbackend.model.entity.Note note, String knowledgeName) {
        NoteResponse response = fromEntity(note);
        if (response != null) {
            response.setKnowledgeName(knowledgeName);
        }
        return response;
    }
}
