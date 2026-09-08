package com.example.zhiyiislandbackend.service.impl;

import com.example.zhiyiislandbackend.model.dto.notification.response.NotificationResponse;
import com.example.zhiyiislandbackend.model.entity.Notification;
import com.example.zhiyiislandbackend.model.enums.BooleanEnum;
import com.example.zhiyiislandbackend.model.enums.NotificationTypeEnum;
import com.example.zhiyiislandbackend.exception.BusinessException;
import com.example.zhiyiislandbackend.mapper.NotificationMapper;
import com.example.zhiyiislandbackend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 通知服务实现类
 * 处理系统通知的创建、查询、标记已读和删除等操作
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 创建通知
     * 用于系统向用户发送各类通知消息
     *
     * @param userId  用户ID
     * @param title   通知标题
     * @param content 通知内容
     * @param type    通知类型
     */
    @Override
    public void createNotification(Long userId, String title, String content, NotificationTypeEnum type) {
        log.info("创建通知，用户ID：{}，标题：{}", userId, title);

        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setIsRead(BooleanEnum.FALSE);
        notification.setCreateTime(LocalDateTime.now());

        notificationMapper.insert(notification);
    }

    /**
     * 获取用户通知列表
     * 按创建时间倒序返回
     *
     * @param userId 用户ID
     * @param limit  返回数量限制
     * @return 通知响应列表
     */
    @Override
    public List<NotificationResponse> getNotifications(Long userId, Integer limit) {
        log.debug("获取通知列表，用户ID：{}，限制：{}", userId, limit);

        List<Notification> notifications = notificationMapper.selectByUserId(userId, limit);
        return notifications.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 获取用户未读通知数量
     *
     * @param userId 用户ID
     * @return 未读通知数量
     */
    @Override
    public int getUnreadCount(Long userId) {
        return notificationMapper.countUnread(userId);
    }

    /**
     * 标记单条通知为已读
     * 验证通知归属后再进行标记
     *
     * @param userId         用户ID
     * @param notificationId 通知ID
     */
    @Override
    public void markAsRead(Long userId, Long notificationId) {
        log.info("标记通知已读，用户ID：{}，通知ID：{}", userId, notificationId);

        Notification notification = notificationMapper.selectById(notificationId);
        if (notification == null) {
            throw new BusinessException(404, "通知不存在");
        }
        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作此通知");
        }

        notificationMapper.markAsRead(notificationId);
    }

    /**
     * 标记用户所有通知为已读
     *
     * @param userId 用户ID
     */
    @Override
    public void markAllAsRead(Long userId) {
        log.info("标记所有通知已读，用户ID：{}", userId);
        notificationMapper.markAllAsRead(userId);
    }

    /**
     * 删除单条通知
     * 验证通知归属后再进行删除
     *
     * @param userId         用户ID
     * @param notificationId 通知ID
     */
    @Override
    public void deleteNotification(Long userId, Long notificationId) {
        log.info("删除通知，用户ID：{}，通知ID：{}", userId, notificationId);

        Notification notification = notificationMapper.selectById(notificationId);
        if (notification == null) {
            throw new BusinessException(404, "通知不存在");
        }
        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作此通知");
        }

        notificationMapper.deleteById(notificationId);
    }

    /**
     * 将通知实体转换为响应DTO
     *
     * @param notification 通知实体
     * @return 通知响应DTO
     */
    private NotificationResponse convertToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .content(notification.getContent())
                .type(notification.getType().getDescription())
                .read(notification.getIsRead().toBoolean())
                .createTime(notification.getCreateTime() != null ?
                        notification.getCreateTime().format(TIME_FORMATTER) : null)
                .build();
    }
}
