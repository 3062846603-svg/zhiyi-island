package com.example.zhiyiislandbackend.service;

/**
 * 邮件服务接口
 * 定义邮件发送相关的操作
 */
public interface MailService {
    /**
     * 发送验证码邮件
     * @param to 收件人邮箱
     * @param code 验证码
     */
    void sendVerificationCode(String to, String code);
}
