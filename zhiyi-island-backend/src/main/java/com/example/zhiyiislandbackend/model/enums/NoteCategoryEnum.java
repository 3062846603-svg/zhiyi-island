package com.example.zhiyiislandbackend.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 笔记分类枚举
 */
@Getter
@AllArgsConstructor
public enum NoteCategoryEnum implements BaseEnum {

    /** 学习笔记 */
    STUDY(1, "学习笔记"),
    /** 工作记录 */
    WORK(2, "工作记录"),
    /** 生活随笔 */
    LIFE(3, "生活随笔"),
    /** 技术文档 */
    TECH(4, "技术文档"),
    /** 读书笔记 */
    READING(5, "读书笔记"),
    /** 项目总结 */
    PROJECT(6, "项目总结"),
    /** 会议记录 */
    MEETING(7, "会议记录"),
    /** 其他 */
    OTHER(8, "其他");

    private final Integer code;
    private final String description;

    /**
     * 根据状态码获取枚举
     */
    public static NoteCategoryEnum fromCode(Integer code) {
        if (code == null) return OTHER;
        for (NoteCategoryEnum category : values()) {
            if (category.code.equals(code)) {
                return category;
            }
        }
        return OTHER;
    }

    /**
     * 根据描述获取枚举
     */
    public static NoteCategoryEnum fromDescription(String description) {
        if (description == null) return null;
        for (NoteCategoryEnum category : values()) {
            if (category.description.equals(description)) {
                return category;
            }
        }
        return OTHER;
    }

    /**
     * 验证状态码是否有效
     */
    public static boolean isValid(Integer code) {
        if (code == null) return false;
        for (NoteCategoryEnum category : values()) {
            if (category.code.equals(code)) {
                return true;
            }
        }
        return false;
    }
}
