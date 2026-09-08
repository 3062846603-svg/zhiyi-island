package com.example.zhiyiislandbackend.service;

/**
 * Token服务接口
 * 定义JWT Token的管理操作，包括保存、验证和失效
 */
public interface TokenService {

    /**
     * 保存Token到Redis
     * @param token JWT Token
     * @param userId 用户ID
     */
    void saveToken(String token, Long userId);

    /**
     * 使Token失效
     * @param token JWT Token
     */
    void invalidateToken(String token);

    /**
     * 检查Token是否有效
     * @param token JWT Token
     * @return Token是否有效
     */
    boolean isTokenValid(String token);

    /**
     * 从Token获取用户ID
     * @param token JWT Token
     * @return 用户ID
     */
    Long getUserIdFromToken(String token);

    /**
     * 使指定用户的所有Token失效
     * @param userId 用户ID
     */
    void invalidateAllTokensByUserId(Long userId);
}
