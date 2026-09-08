package com.example.zhiyiislandbackend.model.dto.imports.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 导入记录响应DTO
 * 返回文件导入的历史记录信息
 */
@Data
public class ImportRecordResponse {
    /**
     * 记录ID
     */
    private Long id;
    /**
     * 导入的文件名
     */
    private String fileName;
    /**
     * 文件类型
     */
    private String fileType;
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    /**
     * 导入状态：0-处理中，1-成功，2-失败
     */
    private Integer status;
    /**
     * 导入的笔记数量
     */
    private Integer noteCount;
    /**
     * 导入的知识条目数量
     */
    private Integer knowledgeCount;
    /**
     * 错误信息（导入失败时）
     */
    private String errorMessage;
    /**
     * 导入时间
     */
    private LocalDateTime createTime;
}
