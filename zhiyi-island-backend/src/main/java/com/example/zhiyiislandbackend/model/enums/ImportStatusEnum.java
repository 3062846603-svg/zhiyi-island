package com.example.zhiyiislandbackend.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 导入状态枚举
 */
@Getter
@AllArgsConstructor
public enum ImportStatusEnum implements BaseEnum {

    /** 处理中 */
    PROCESSING(0, "处理中"),
    /** 成功 */
    SUCCESS(1, "成功"),
    /** 失败 */
    FAILED(2, "失败");

    private final Integer code;
    private final String description;

    /**
     * 根据状态码获取枚举
     */
    public static ImportStatusEnum fromCode(Integer code) {
        if (code == null) return null;
        for (ImportStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
