package com.example.zhiyiislandbackend.model.dto.notification.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通知响应DTO
 * 返回用户通知的详细信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    /** 通知ID */
    private Long id;
    /** 标题 */
    private String title;
    /** 内容 */
    private String content;
    /** 通知类型 */
    private String type;
    /** 是否已读 */
    private Boolean read;
    /** 创建时间 */
    private String createTime;
}
