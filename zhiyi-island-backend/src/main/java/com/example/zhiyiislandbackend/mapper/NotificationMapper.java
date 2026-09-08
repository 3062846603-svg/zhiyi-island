package com.example.zhiyiislandbackend.mapper;

import com.example.zhiyiislandbackend.model.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 通知数据访问接口
 * 提供通知表的CRUD操作和已读状态管理
 */
@Mapper
public interface NotificationMapper {

    /**
     * 插入新通知
     */
    int insert(Notification notification);

    /**
     * 根据ID查询通知
     */
    Notification selectById(@Param("id") Long id);

    /**
     * 查询用户通知列表
     */
    List<Notification> selectByUserId(@Param("userId") Long userId, @Param("limit") Integer limit);

    /**
     * 统计用户未读通知数量
     */
    int countUnread(@Param("userId") Long userId);

    /**
     * 标记单条通知已读
     */
    int markAsRead(@Param("id") Long id);

    /**
     * 标记用户所有通知已读
     */
    int markAllAsRead(@Param("userId") Long userId);

    /**
     * 删除通知
     */
    int deleteById(@Param("id") Long id);

    /**
     * 删除用户所有通知
     */
    int deleteByUserId(@Param("userId") Long userId);
}
