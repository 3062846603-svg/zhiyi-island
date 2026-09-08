package com.example.zhiyiislandbackend.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通知类型枚举
 */
@Getter
@AllArgsConstructor
public enum NotificationTypeEnum implements BaseEnum {

    /** 系统通知 */
    SYSTEM(1, "系统通知"),
    /** 笔记通知 */
    NOTE(2, "笔记通知"),
    /** AI通知 */
    AI(3, "AI通知"),
    /** 知识库通知 */
    KNOWLEDGE(4, "知识库通知");

    private final Integer code;
    private final String description;

    /**
     * 根据类型码获取枚举
     */
    public static NotificationTypeEnum fromCode(Integer code) {
        if (code == null) return SYSTEM;
        for (NotificationTypeEnum type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return SYSTEM;
    }
}
