package com.example.zhiyiislandbackend.model.entity;

import com.example.zhiyiislandbackend.model.enums.ImportStatusEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 导入记录实体类
 * 存储文件导入的历史记录和状态
 */
@Data
public class ImportRecord {
    /** 记录ID */
    private Long id;
    /** 用户ID */
    private Long userId;
    /** 文件名 */
    private String fileName;
    /** 文件类型 */
    private String fileType;
    /** 文件大小（字节） */
    private Long fileSize;
    /** 导入状态 */
    private ImportStatusEnum status;
    /** 生成的笔记数量 */
    private Integer noteCount;
    /** 生成的知识库数量 */
    private Integer knowledgeCount;
    /** 错误信息 */
    private String errorMessage;
    /** 创建时间 */
    private LocalDateTime createTime;
}
