package com.example.zhiyiislandbackend.controller;

import com.example.zhiyiislandbackend.common.result.Result;
import com.example.zhiyiislandbackend.model.dto.notification.response.NotificationResponse;
import com.example.zhiyiislandbackend.service.NotificationService;
import com.example.zhiyiislandbackend.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通知控制器
 * 处理系统通知相关的HTTP请求，包括通知列表查询、已读标记和删除
 */
@Slf4j
@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final JwtUtil jwtUtil;

    /**
     * 获取通知列表
     * 按创建时间倒序返回用户的通知
     *
     * @param request HTTP请求
     * @param limit   返回数量限制
     * @return 通知列表
     */
    @GetMapping("/list")
    public Result<List<NotificationResponse>> getNotifications(
            HttpServletRequest request,
            @RequestParam(defaultValue = "10") Integer limit) {
        Long userId = getUserId(request);
        log.info("获取通知列表，用户ID：{}", userId);

        List<NotificationResponse> notifications = notificationService.getNotifications(userId, limit);
        return Result.success(notifications);
    }

    /**
     * 获取未读通知数量
     *
     * @param request HTTP请求
     * @return 包含未读数量的响应
     */
    @GetMapping("/unread-count")
    public Result<Map<String, Integer>> getUnreadCount(HttpServletRequest request) {
        Long userId = getUserId(request);
        int count = notificationService.getUnreadCount(userId);

        Map<String, Integer> result = new HashMap<>();
        result.put("count", count);
        return Result.success(result);
    }

    /**
     * 标记单条通知已读
     *
     * @param request HTTP请求
     * @param id      通知ID
     * @return 操作结果
     */
    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(HttpServletRequest request, @PathVariable Long id) {
        Long userId = getUserId(request);
        log.info("标记通知已读，用户ID：{}，通知ID：{}", userId, id);

        notificationService.markAsRead(userId, id);
        return Result.success("已标记已读", null);
    }

    /**
     * 标记所有通知已读
     *
     * @param request HTTP请求
     * @return 操作结果
     */
    @PutMapping("/read-all")
    public Result<Void> markAllAsRead(HttpServletRequest request) {
        Long userId = getUserId(request);
        log.info("标记所有通知已读，用户ID：{}", userId);

        notificationService.markAllAsRead(userId);
        return Result.success("已全部标记已读", null);
    }

    /**
     * 删除单条通知
     *
     * @param request HTTP请求
     * @param id      通知ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteNotification(HttpServletRequest request, @PathVariable Long id) {
        Long userId = getUserId(request);
        log.info("删除通知，用户ID：{}，通知ID：{}", userId, id);

        notificationService.deleteNotification(userId, id);
        return Result.success("删除成功", null);
    }

    /**
     * 从HTTP请求中获取用户ID
     *
     * @param request HTTP请求
     * @return 用户ID
     */
    private Long getUserId(HttpServletRequest request) {
        String token = extractToken(request);
        return jwtUtil.getUserIdFromToken(token);
    }

    /**
     * 从HTTP请求头中提取Token
     *
     * @param request HTTP请求
     * @return Token字符串
     */
    private String extractToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return null;
    }
}
