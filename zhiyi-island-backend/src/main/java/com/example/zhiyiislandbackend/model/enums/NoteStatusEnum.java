package com.example.zhiyiislandbackend.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 笔记状态枚举
 */
@Getter
@AllArgsConstructor
public enum NoteStatusEnum implements BaseEnum {
    /** 草稿 */
    DRAFT(0, "草稿"),
    /** 已发布 */
    PUBLISHED(1, "已发布"),
    /** 已归档 */
    ARCHIVED(2, "已归档"),
    /** 已删除 */
    DELETED(3, "已删除");

    private final Integer code;
    private final String description;

    /**
     * 根据状态码获取枚举
     */
    public static NoteStatusEnum fromCode(Integer code) {
        if (code == null) return PUBLISHED;
        for (NoteStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return PUBLISHED;
    }
}
