package com.example.zhiyiislandbackend.model.dto.knowledge.response;

import com.example.zhiyiislandbackend.model.entity.Knowledge;
import com.example.zhiyiislandbackend.model.entity.KnowledgeItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 知识库响应DTO
 * 返回知识库的详细信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeResponse {
    /** 知识库ID */
    private Long id;
    /** 标题 */
    private String title;
    /** 描述 */
    private String description;
    /** 分类名称 */
    private String category;
    /** 知识条目数量 */
    private Integer itemCount;
    /** 笔记数量 */
    private Integer noteCount;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
    /** 知识条目列表 */
    private List<KnowledgeItemResponse> items;

    /**
     * 知识条目响应DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KnowledgeItemResponse {
        /** 条目ID */
        private Long id;
        /** 标题 */
        private String title;
        /** 内容 */
        private String content;
        /** 来源 */
        private String source;
        /** 来源笔记ID */
        private Long noteId;
        /** 创建时间 */
        private LocalDateTime createTime;

        /**
         * 从实体转换为响应DTO
         */
        public static KnowledgeItemResponse fromEntity(KnowledgeItem item) {
            if (item == null) return null;
            return KnowledgeItemResponse.builder()
                    .id(item.getId())
                    .title(item.getTitle())
                    .content(item.getContent())
                    .source(item.getSource())
                    .noteId(item.getNoteId())
                    .createTime(item.getCreateTime())
                    .build();
        }
    }

    /**
     * 从实体转换为响应DTO
     */
    public static KnowledgeResponse fromEntity(Knowledge knowledge) {
        if (knowledge == null) return null;
        return KnowledgeResponse.builder()
                .id(knowledge.getId())
                .title(knowledge.getTitle())
                .description(knowledge.getDescription())
                .category(knowledge.getCategory() != null ? knowledge.getCategory().getDescription() : null)
                .itemCount(knowledge.getItemCount())
                .noteCount(knowledge.getNoteCount())
                .createTime(knowledge.getCreateTime())
                .updateTime(knowledge.getUpdateTime())
                .build();
    }

    /**
     * 从实体转换为响应DTO（包含知识条目）
     */
    public static KnowledgeResponse fromEntityWithItems(Knowledge knowledge, List<KnowledgeItem> items) {
        KnowledgeResponse response = fromEntity(knowledge);
        if (response != null && items != null) {
            response.setItems(items.stream()
                    .map(KnowledgeItemResponse::fromEntity)
                    .collect(Collectors.toList()));
        }
        return response;
    }
}
