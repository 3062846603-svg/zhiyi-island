package com.example.zhiyiislandbackend.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 导出记录实体类
 * 存储数据导出的历史记录
 */
@Data
public class ExportRecord {
    /** 记录ID */
    private Long id;
    /** 用户ID */
    private Long userId;
    /** 导出类型 */
    private String exportType;
    /** 导出格式 */
    private String format;
    /** 文件大小（字节） */
    private Long fileSize;
    /** 创建时间 */
    private LocalDateTime createTime;
}
