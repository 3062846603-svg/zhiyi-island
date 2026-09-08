package com.example.zhiyiislandbackend.model.dto.user.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 更新邮箱请求DTO
 * 用于用户绑定或更换邮箱
 */
@Data
public class UpdateEmailRequest {
    /**
     * 新邮箱地址
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 邮箱验证码
     */
    @NotBlank(message = "验证码不能为空")
    private String code;
}
