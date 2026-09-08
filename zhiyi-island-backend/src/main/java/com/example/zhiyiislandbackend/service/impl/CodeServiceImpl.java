package com.example.zhiyiislandbackend.service.impl;

import com.example.zhiyiislandbackend.service.CodeService;
import com.example.zhiyiislandbackend.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务实现类
 * 基于Redis存储验证码，支持验证码的生成、发送和验证
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CodeServiceImpl implements CodeService {

    private final StringRedisTemplate redisTemplate;
    private final MailService mailService;
    
    /** Redis中验证码的键前缀 */
    private static final String CODE_PREFIX = "email:code:";
    /** 验证码有效期（分钟） */
    private static final long CODE_EXPIRE = 5;

    /**
     * 发送验证码到指定邮箱
     * 1. 生成6位数字验证码
     * 2. 存储到Redis（5分钟有效期）
     * 3. 发送邮件
     */
    @Override
    public void sendCode(String email) {
        String code = generateCode();
        String key = CODE_PREFIX + email;
        
        redisTemplate.opsForValue().set(key, code, CODE_EXPIRE, TimeUnit.MINUTES);
        
        mailService.sendVerificationCode(email, code);
        log.info("验证码已发送到: {}", email);
    }

    /**
     * 验证邮箱验证码
     * 验证成功后删除Redis中的验证码（一次性使用）
     */
    @Override
    public boolean verifyCode(String email, String code) {
        String key = CODE_PREFIX + email;
        String savedCode = redisTemplate.opsForValue().get(key);
        
        if (savedCode != null && savedCode.equals(code)) {
            redisTemplate.delete(key);
            return true;
        }
        return false;
    }

    /**
     * 生成6位数字验证码
     */
    private String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
}
