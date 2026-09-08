package com.example.zhiyiislandbackend.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通用布尔枚举
 * 用于表示是/否、开/关等二元状态
 */
@Getter
@AllArgsConstructor
public enum BooleanEnum implements BaseEnum {

    /**
     * 否/关闭/未标星
     */
    FALSE(0, "否"),
    /**
     * 是/开启/已标星
     */
    TRUE(1, "是");

    /**
     * 枚举值
     */
    private final Integer code;
    /**
     * 枚举描述
     */
    private final String description;

    /**
     * 根据值获取枚举
     *
     * @param code 枚举值
     * @return 对应的枚举，未找到则返回null
     */
    public static BooleanEnum fromValue(Integer code) {
        if (code == null) {
            return null;
        }
        for (BooleanEnum bool : BooleanEnum.values()) {
            if (bool.getCode().equals(code)) {
                return bool;
            }
        }
        return null;
    }

    /**
     * 根据布尔值获取枚举
     *
     * @param bool 布尔值
     * @return 对应的枚举
     */
    public static BooleanEnum fromBoolean(boolean bool) {
        return bool ? TRUE : FALSE;
    }

    /**
     * 转换为布尔值
     *
     * @return 布尔值
     */
    public boolean toBoolean() {
        return this == TRUE;
    }
}
