package com.example.zhiyiislandbackend.service.impl;

import com.example.zhiyiislandbackend.service.TokenService;
import com.example.zhiyiislandbackend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Token服务实现类
 * 基于Redis管理JWT Token，支持Token的存储、验证和失效
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtUtil jwtUtil;

    /**
     * JWT过期时间（毫秒）
     */
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Token在Redis中的键前缀
     */
    private static final String TOKEN_PREFIX = "token:";
    /**
     * 用户Token集合在Redis中的键前缀
     */
    private static final String USER_TOKENS_PREFIX = "user:tokens:";

    /**
     * 保存Token到Redis
     * 同时维护用户-Token映射关系，支持批量失效
     */
    @Override
    public void saveToken(String token, Long userId) {
        String tokenKey = TOKEN_PREFIX + token;
        String userTokensKey = USER_TOKENS_PREFIX + userId;

        redisTemplate.opsForValue().set(tokenKey, userId, jwtExpiration, TimeUnit.MILLISECONDS);
        redisTemplate.opsForSet().add(userTokensKey, token);
        redisTemplate.expire(userTokensKey, jwtExpiration, TimeUnit.MILLISECONDS);

        log.info("Token已保存到Redis，用户ID：{}", userId);
    }

    /**
     * 使Token失效
     * 从Redis中删除Token，并从用户Token集合中移除
     */
    @Override
    public void invalidateToken(String token) {
        String tokenKey = TOKEN_PREFIX + token;
        Object userId = redisTemplate.opsForValue().get(tokenKey);

        if (userId != null) {
            String userTokensKey = USER_TOKENS_PREFIX + userId;
            redisTemplate.opsForSet().remove(userTokensKey, token);
        }

        redisTemplate.delete(tokenKey);
        log.info("Token已失效");
    }

    /**
     * 检查Token是否有效
     * 通过检查Redis中是否存在该Token来判断
     */
    @Override
    public boolean isTokenValid(String token) {
        String tokenKey = TOKEN_PREFIX + token;
        return redisTemplate.hasKey(tokenKey);
    }

    /**
     * 从Token获取用户ID
     */
    @Override
    public Long getUserIdFromToken(String token) {
        String tokenKey = TOKEN_PREFIX + token;
        Object userId = redisTemplate.opsForValue().get(tokenKey);
        return userId != null ? Long.valueOf(userId.toString()) : null;
    }

    /**
     * 使指定用户的所有Token失效
     * 用于修改密码或删除账户后强制登出
     */
    @Override
    public void invalidateAllTokensByUserId(Long userId) {
        String userTokensKey = USER_TOKENS_PREFIX + userId;
        Set<Object> tokens = redisTemplate.opsForSet().members(userTokensKey);

        if (tokens != null && !tokens.isEmpty()) {
            for (Object token : tokens) {
                redisTemplate.delete(TOKEN_PREFIX + token);
            }
        }

        redisTemplate.delete(userTokensKey);
        log.info("用户所有Token已失效，用户ID：{}", userId);
    }
}
