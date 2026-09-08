package com.example.zhiyiislandbackend.model.dto.user.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 验证邮箱验证码请求DTO
 * 用于验证邮箱验证码是否正确
 */
@Data
public class VerifyEmailCodeRequest {
    /** 邮箱地址 */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    /** 验证码 */
    @NotBlank(message = "验证码不能为空")
    private String code;
}
