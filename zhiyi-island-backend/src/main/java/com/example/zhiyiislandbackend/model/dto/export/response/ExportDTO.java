package com.example.zhiyiislandbackend.model.dto.export.response;

import com.example.zhiyiislandbackend.model.entity.AiSummary;
import com.example.zhiyiislandbackend.model.entity.Knowledge;
import com.example.zhiyiislandbackend.model.entity.KnowledgeItem;
import com.example.zhiyiislandbackend.model.entity.Note;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 导出数据DTO
 * 用于数据导出时的数据转换
 */
public class ExportDTO {

    /**
     * 笔记导出DTO
     */
    @Data
    public static class NoteExport {
        /** 标题 */
        private String title;
        /** 内容 */
        private String content;
        /** 分类 */
        private String category;
        /** 状态 */
        private String status;
        /** 字数 */
        private Integer wordCount;
        /** 来源 */
        private String source;
        /** 创建时间 */
        private LocalDateTime createTime;
        /** 更新时间 */
        private LocalDateTime updateTime;

        /**
         * 从实体转换为导出DTO
         */
        public static NoteExport fromEntity(Note note) {
            if (note == null) return null;
            NoteExport dto = new NoteExport();
            dto.setTitle(note.getTitle());
            dto.setContent(note.getContent());
            dto.setCategory(note.getCategory() != null ? note.getCategory().getDescription() : null);
            dto.setStatus(note.getStatus() != null ? note.getStatus().getDescription() : null);
            dto.setWordCount(note.getWordCount());
            dto.setSource(note.getSource());
            dto.setCreateTime(note.getCreateTime());
            dto.setUpdateTime(note.getUpdateTime());
            return dto;
        }
    }

    /**
     * 知识库导出DTO
     */
    @Data
    public static class KnowledgeExport {
        /** 标题 */
        private String title;
        /** 描述 */
        private String description;
        /** 分类 */
        private String category;
        /** 知识条目数量 */
        private Integer itemCount;
        /** 笔记数量 */
        private Integer noteCount;
        /** 创建时间 */
        private LocalDateTime createTime;
        /** 更新时间 */
        private LocalDateTime updateTime;

        /**
         * 从实体转换为导出DTO
         */
        public static KnowledgeExport fromEntity(Knowledge knowledge) {
            if (knowledge == null) return null;
            KnowledgeExport dto = new KnowledgeExport();
            dto.setTitle(knowledge.getTitle());
            dto.setDescription(knowledge.getDescription());
            dto.setCategory(knowledge.getCategory() != null ? knowledge.getCategory().getDescription() : null);
            dto.setItemCount(knowledge.getItemCount());
            dto.setNoteCount(knowledge.getNoteCount());
            dto.setCreateTime(knowledge.getCreateTime());
            dto.setUpdateTime(knowledge.getUpdateTime());
            return dto;
        }
    }

    /**
     * 知识条目导出DTO
     */
    @Data
    public static class KnowledgeItemExport {
        /** 标题 */
        private String title;
        /** 内容 */
        private String content;
        /** 来源 */
        private String source;
        /** 创建时间 */
        private LocalDateTime createTime;
        /** 更新时间 */
        private LocalDateTime updateTime;

        /**
         * 从实体转换为导出DTO
         */
        public static KnowledgeItemExport fromEntity(KnowledgeItem item) {
            if (item == null) return null;
            KnowledgeItemExport dto = new KnowledgeItemExport();
            dto.setTitle(item.getTitle());
            dto.setContent(item.getContent());
            dto.setSource(item.getSource());
            dto.setCreateTime(item.getCreateTime());
            dto.setUpdateTime(item.getUpdateTime());
            return dto;
        }
    }

    /**
     * AI摘要导出DTO
     */
    @Data
    public static class AiSummaryExport {
        /** 摘要内容 */
        private String summaryContent;
        /** 摘要类型 */
        private String summaryType;
        /** 摘要风格 */
        private String summaryStyle;
        /** 摘要长度 */
        private String summaryLength;
        /** 原文字数 */
        private Integer originalWordCount;
        /** 摘要字数 */
        private Integer summaryWordCount;
        /** 创建时间 */
        private LocalDateTime createTime;

        /**
         * 从实体转换为导出DTO
         */
        public static AiSummaryExport fromEntity(AiSummary summary) {
            if (summary == null) return null;
            AiSummaryExport dto = new AiSummaryExport();
            dto.setSummaryContent(summary.getSummaryContent());
            dto.setSummaryType(summary.getSummaryType());
            dto.setSummaryStyle(summary.getSummaryStyle());
            dto.setSummaryLength(summary.getSummaryLength());
            dto.setOriginalWordCount(summary.getOriginalWordCount());
            dto.setSummaryWordCount(summary.getSummaryWordCount());
            dto.setCreateTime(summary.getCreateTime());
            return dto;
        }
    }
}
