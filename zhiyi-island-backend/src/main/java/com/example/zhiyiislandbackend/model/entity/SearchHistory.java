package com.example.zhiyiislandbackend.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 搜索历史实体类
 * 存储用户的搜索记录
 */
@Data
public class SearchHistory {
    /** 记录ID */
    private Long id;
    /** 用户ID */
    private Long userId;
    /** 搜索关键词 */
    private String keyword;
    /** 搜索类型 */
    private String searchType;
    /** 创建时间 */
    private LocalDateTime createTime;
}
