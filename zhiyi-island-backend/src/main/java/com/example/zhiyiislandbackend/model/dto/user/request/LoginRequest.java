package com.example.zhiyiislandbackend.model.dto.user.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户登录请求DTO
 * 包含用户登录所需的用户名和密码
 */
@Data
public class LoginRequest {
    /**
     * 用户名或邮箱
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 用户密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;
}
