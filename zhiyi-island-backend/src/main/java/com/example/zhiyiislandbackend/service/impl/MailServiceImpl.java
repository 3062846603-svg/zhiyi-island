package com.example.zhiyiislandbackend.service.impl;

import com.example.zhiyiislandbackend.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * 邮件服务实现类
 * 基于Spring Mail实现邮件发送功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;

    /** 发件人邮箱地址（从配置文件读取） */
    @Value("${spring.mail.username}")
    private String from;

    /**
     * 发送验证码邮件
     * 使用简单文本邮件格式发送验证码
     */
    @Override
    public void sendVerificationCode(String to, String code) {
        log.info("发送验证码到邮箱: {}", to);
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("【知忆岛】邮箱验证码");
        message.setText("您的验证码是：" + code + "，验证码有效期为5分钟，请勿将验证码告知他人。\n\n—— 知忆岛团队");
        
        mailSender.send(message);
        log.info("验证码发送成功");
    }
}
