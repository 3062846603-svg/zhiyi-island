package com.example.zhiyiislandbackend.service;

/**
 * 幂等性服务接口
 * 提供请求幂等性控制，防止重复提交
 */
public interface IdempotencyService {

    /**
     * 尝试获取处理锁（使用默认过期时间60秒）
     *
     * @param key 幂等性键
     * @return 是否获取成功（true表示可以处理，false表示请求正在处理中）
     */
    boolean tryAcquire(String key);

    /**
     * 尝试获取处理锁（指定过期时间）
     *
     * @param key      幂等性键
     * @param expireMs 过期时间（毫秒）
     * @return 是否获取成功（true表示可以处理，false表示请求正在处理中）
     */
    boolean tryAcquire(String key, long expireMs);

    /**
     * 释放处理锁
     *
     * @param key 幂等性键
     */
    void release(String key);

    /**
     * 构建幂等性键
     *
     * @param userId    用户ID
     * @param operation 操作名称
     * @param params    额外参数
     * @return 构建的幂等性键
     */
    String buildKey(Long userId, String operation, String... params);

    /**
     * 清理过期的处理锁
     *
     * @param expireMs 过期时间（毫秒）
     */
    void cleanExpired(long expireMs);
}
