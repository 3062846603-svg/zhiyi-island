package com.example.zhiyiislandbackend.service;

/**
 * 验证码服务接口
 * 定义邮箱验证码的发送和验证操作
 */
public interface CodeService {
    /**
     * 发送验证码到指定邮箱
     * @param email 目标邮箱地址
     */
    void sendCode(String email);
    
    /**
     * 验证邮箱验证码
     * @param email 邮箱地址
     * @param code 验证码
     * @return 验证是否通过
     */
    boolean verifyCode(String email, String code);
}
