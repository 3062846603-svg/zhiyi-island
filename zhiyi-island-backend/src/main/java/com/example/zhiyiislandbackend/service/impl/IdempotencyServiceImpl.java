package com.example.zhiyiislandbackend.service.impl;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.example.zhiyiislandbackend.service.IdempotencyService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class IdempotencyServiceImpl implements IdempotencyService {

    private final ConcurrentHashMap<String, Long> pendingRequests = new ConcurrentHashMap<>();
    private static final long DEFAULT_EXPIRE_MS = 60000;

    @Override
    public boolean tryAcquire(String key) {
        return tryAcquire(key, DEFAULT_EXPIRE_MS);
    }

    @Override
    public boolean tryAcquire(String key, long expireMs) {
        long now = System.currentTimeMillis();
        Long existingTime = pendingRequests.putIfAbsent(key, now);
        
        if (existingTime != null) {
            long elapsed = now - existingTime;
            if (elapsed < expireMs) {
                log.debug("请求正在处理中，key: {}, 已经过时间: {}ms", key, elapsed);
                return false;
            } else {
                pendingRequests.put(key, now);
                log.debug("请求已过期，重新处理，key: {}", key);
            }
        }
        
        log.debug("获取处理锁成功，key: {}", key);
        return true;
    }

    @Override
    public void release(String key) {
        pendingRequests.remove(key);
        log.debug("释放处理锁，key: {}", key);
    }

    @Override
    public String buildKey(Long userId, String operation, String... params) {
        StringBuilder sb = new StringBuilder();
        sb.append(userId).append(":").append(operation);
        for (String param : params) {
            sb.append(":").append(param != null ? param : "");
        }
        return sb.toString();
    }

    @Override
    public void cleanExpired(long expireMs) {
        long now = System.currentTimeMillis();
        pendingRequests.entrySet().removeIf(entry -> (now - entry.getValue()) > expireMs);
    }
}
