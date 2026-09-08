package com.example.zhiyiislandbackend.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 知识库分类枚举
 */
@Slf4j
@Getter
@AllArgsConstructor
public enum KnowledgeCategoryEnum implements BaseEnum {

    /** 技术 */
    TECH(1, "技术"),
    /** 学习 */
    STUDY(2, "学习"),
    /** 工作 */
    WORK(3, "工作"),
    /** 生活 */
    LIFE(4, "生活"),
    /** 健康 */
    HEALTH(5, "健康"),
    /** 财经 */
    FINANCE(6, "财经"),
    /** 文化 */
    CULTURE(7, "文化"),
    /** 其他 */
    OTHER(8, "其他");

    private final Integer code;
    private final String description;

    /**
     * 根据状态码获取枚举
     */
    public static KnowledgeCategoryEnum fromCode(Integer code) {
        if (code == null) return OTHER;
        for (KnowledgeCategoryEnum category : values()) {
            if (category.code.equals(code)) {
                return category;
            }
        }
        return OTHER;
    }

    /**
     * 根据描述获取枚举
     */
    public static KnowledgeCategoryEnum fromDescription(String description) {
        if (description == null || description.isBlank()) {
            log.warn("分类描述为空，返回默认分类: 其他");
            return OTHER;
        }
        
        String normalizedDesc = description.trim();
        
        for (KnowledgeCategoryEnum category : values()) {
            if (category.description.equals(normalizedDesc)) {
                return category;
            }
        }
        
        log.warn("未找到匹配的分类: [{}]，返回默认分类: 其他", description);
        return OTHER;
    }

    /**
     * 验证状态码是否有效
     */
    public static boolean isValid(Integer code) {
        if (code == null) return false;
        for (KnowledgeCategoryEnum category : values()) {
            if (category.code.equals(code)) {
                return true;
            }
        }
        return false;
    }
}
