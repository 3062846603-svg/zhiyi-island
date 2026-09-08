package com.example.zhiyiislandbackend.model.entity;

import com.example.zhiyiislandbackend.model.enums.KnowledgeCategoryEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识库实体类
 * 存储知识库基本信息和统计数量
 */
@Data
public class Knowledge {
    /** 知识库ID */
    private Long id;
    /** 所属用户ID */
    private Long userId;
    /** 标题 */
    private String title;
    /** 描述 */
    private String description;
    /** 分类 */
    private KnowledgeCategoryEnum category;
    /** 知识条目数量 */
    private Integer itemCount;
    /** 笔记数量 */
    private Integer noteCount;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
}
