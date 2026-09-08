package com.example.zhiyiislandbackend.model.dto.user.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 验证密码请求DTO
 * 用于验证用户密码是否正确
 */
@Data
public class VerifyPasswordRequest {
    /** 用户密码 */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;
}
