package com.example.zhiyiislandbackend.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户状态枚举
 */
@Getter
@AllArgsConstructor
public enum UserStatusEnum implements BaseEnum {

    /** 禁用 */
    DISABLED(0, "禁用"),
    /** 正常 */
    NORMAL(1, "正常");

    private final Integer code;
    private final String description;

    /**
     * 根据状态码获取枚举
     */
    public static UserStatusEnum fromCode(Integer code) {
        if (code == null) return null;
        for (UserStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
