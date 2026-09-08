package com.example.zhiyiislandbackend.exception;

import lombok.Getter;

/**
 * 业务异常类
 * 用于业务逻辑中抛出的可预期异常
 */
@Getter
public class BusinessException extends RuntimeException {
    /**
     * 错误状态码
     */
    private final Integer code;

    /**
     * 构造业务异常（默认状态码500）
     *
     * @param message 错误消息
     */
    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    /**
     * 构造业务异常（自定义状态码）
     *
     * @param code    错误状态码
     * @param message 错误消息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

}
