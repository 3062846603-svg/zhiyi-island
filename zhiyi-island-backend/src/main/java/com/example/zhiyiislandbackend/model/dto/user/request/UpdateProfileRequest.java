package com.example.zhiyiislandbackend.model.dto.user.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新用户资料请求DTO
 * 用于修改用户的个人信息
 */
@Data
public class UpdateProfileRequest {
    /**
     * 用户昵称
     */
    @Size(max = 20, message = "昵称长度不能超过20个字符")
    private String nickname;

    /**
     * 用户头像URL
     */
    @Size(max = 255, message = "头像地址长度不能超过255个字符")
    private String avatar;

    /**
     * 手机号码
     */
    @Size(max = 20, message = "手机号长度不能超过20个字符")
    private String phone;
}
