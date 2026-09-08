package com.example.zhiyiislandbackend.model.dto.search.response;

import com.example.zhiyiislandbackend.model.entity.Knowledge;
import com.example.zhiyiislandbackend.model.entity.Note;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 搜索结果响应DTO
 * 返回统一格式的搜索结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultResponse {
    /**
     * 结果ID
     */
    private Long id;
    /**
     * 标题
     */
    private String title;
    /**
     * 内容摘要
     */
    private String content;
    /**
     * 结果类型：note-笔记，knowledge-知识库
     */
    private String type;
    /**
     * 书籍名称（笔记类型）
     */
    private String bookName;
    /**
     * 来源
     */
    private String source;

    /**
     * 从笔记实体转换为搜索结果
     *
     * @param note 笔记实体
     * @return 搜索结果响应DTO
     */
    public static SearchResultResponse fromNote(Note note) {
        if (note == null) return null;
        return SearchResultResponse.builder()
                .id(note.getId())
                .title(note.getTitle())
                .content(note.getContent())
                .type("note")
                .bookName(null)
                .source(note.getSource())
                .build();
    }

    /**
     * 从知识库实体转换为搜索结果
     *
     * @param knowledge 知识库实体
     * @return 搜索结果响应DTO
     */
    public static SearchResultResponse fromKnowledge(Knowledge knowledge) {
        if (knowledge == null) return null;
        return SearchResultResponse.builder()
                .id(knowledge.getId())
                .title(knowledge.getTitle())
                .content(knowledge.getDescription())
                .type("knowledge")
                .source(knowledge.getCategory() != null ? knowledge.getCategory().getDescription() : null)
                .build();
    }
}
