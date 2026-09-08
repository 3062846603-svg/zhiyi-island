package com.example.zhiyiislandbackend.model.dto.export.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 导出记录响应DTO
 * 返回导出历史记录信息
 */
@Data
public class ExportRecordResponse {
    /**
     * 记录ID
     */
    private Long id;
    /**
     * 导出类型
     */
    private String exportType;
    /**
     * 导出格式
     */
    private String format;
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    /**
     * 导出时间
     */
    private LocalDateTime createTime;
}
