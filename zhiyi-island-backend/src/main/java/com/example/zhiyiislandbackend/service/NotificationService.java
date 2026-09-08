package com.example.zhiyiislandbackend.service;

import com.example.zhiyiislandbackend.model.dto.notification.response.NotificationResponse;
import com.example.zhiyiislandbackend.model.enums.NotificationTypeEnum;

import java.util.List;

/**
 * 通知服务接口
 * 定义用户通知相关的业务操作，包括创建、查询、标记已读和删除
 */
public interface NotificationService {

    /**
     * 创建通知
     *
     * @param userId  用户ID
     * @param title   通知标题
     * @param content 通知内容
     * @param type    通知类型
     */
    void createNotification(Long userId, String title, String content, NotificationTypeEnum type);

    /**
     * 获取用户通知列表
     *
     * @param userId 用户ID
     * @param limit  返回数量限制
     * @return 通知响应列表
     */
    List<NotificationResponse> getNotifications(Long userId, Integer limit);

    /**
     * 获取用户未读通知数量
     *
     * @param userId 用户ID
     * @return 未读通知数量
     */
    int getUnreadCount(Long userId);

    /**
     * 标记通知为已读
     *
     * @param userId         用户ID
     * @param notificationId 通知ID
     */
    void markAsRead(Long userId, Long notificationId);

    /**
     * 标记用户所有通知为已读
     *
     * @param userId 用户ID
     */
    void markAllAsRead(Long userId);

    /**
     * 删除通知
     *
     * @param userId         用户ID
     * @param notificationId 通知ID
     */
    void deleteNotification(Long userId, Long notificationId);
}
