package com.example.zhiyiislandbackend.model.entity;

import com.example.zhiyiislandbackend.model.enums.UserStatusEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * 存储用户基本信息和账户状态
 */
@Data
public class User {
    /** 用户ID */
    private Long id;
    /** 用户名（唯一） */
    private String username;
    /** 密码（加密存储） */
    private String password;
    /** 邮箱（唯一） */
    private String email;
    /** 昵称 */
    private String nickname;
    /** 头像URL */
    private String avatar;
    /** 手机号 */
    private String phone;
    /** 用户状态 */
    private UserStatusEnum status;
    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
}
