package com.example.zhiyiislandbackend.model.entity;

import com.example.zhiyiislandbackend.model.enums.BooleanEnum;
import com.example.zhiyiislandbackend.model.enums.NotificationTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知实体类
 * 存储用户通知消息
 */
@Data
public class Notification {
    /** 通知ID */
    private Long id;
    /** 接收用户ID */
    private Long userId;
    /** 标题 */
    private String title;
    /** 内容 */
    private String content;
    /** 通知类型 */
    private NotificationTypeEnum type;
    /** 是否已读 */
    private BooleanEnum isRead;
    /** 阅读时间 */
    private LocalDateTime readTime;
    /** 创建时间 */
    private LocalDateTime createTime;
}
