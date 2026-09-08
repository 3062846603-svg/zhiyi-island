package com.example.zhiyiislandbackend.model.dto.user.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应DTO
 * 返回登录成功后的用户信息和Token
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * JWT认证Token
     */
    private String token;
    /**
     * 用户名
     */
    private String username;
    /**
     * 用户昵称
     */
    private String nickname;
    /**
     * 用户头像URL
     */
    private String avatar;
    /**
     * 用户邮箱
     */
    private String email;
}
