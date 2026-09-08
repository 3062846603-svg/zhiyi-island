package com.example.zhiyiislandbackend.model.enums;

/**
 * 基础枚举接口
 * 所有枚举类实现此接口，提供统一的值获取方法
 */
public interface BaseEnum {

    /**
     * 获取枚举值
     *
     * @return 枚举对应的数据库存储值
     */
    Integer getCode();

    /**
     * 获取枚举描述
     *
     * @return 枚举的中文描述
     */
    String getDescription();
}
