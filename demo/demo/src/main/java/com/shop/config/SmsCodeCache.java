package com.shop.config;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class SmsCodeCache {

    private final Map<String, CodeEntry> cache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor();

    public SmsCodeCache() {
        // 每 5 分钟清理一次过期验证码
        cleaner.scheduleAtFixedRate(this::cleanExpired, 5, 5, TimeUnit.MINUTES);
    }

    // 生成 6 位验证码并缓存
    public String generateAndSet(String phone) {
        String code = String.format("%06d", new Random().nextInt(1000000));
        cache.put(phone, new CodeEntry(code, System.currentTimeMillis()));
        return code;
    }

    // 验证验证码
    public boolean verify(String phone, String code) {
        CodeEntry entry = cache.get(phone);
        if (entry == null) return false;
        // 5 分钟过期
        if (System.currentTimeMillis() - entry.timestamp > 5 * 60 * 1000) {
            cache.remove(phone);
            return false;
        }
        return entry.code.equals(code);
    }

    // 验证成功后删除
    public void remove(String phone) {
        cache.remove(phone);
    }

    private void cleanExpired() {
        long now = System.currentTimeMillis();
        cache.entrySet().removeIf(e -> now - e.getValue().timestamp > 5 * 60 * 1000);
    }

    private static class CodeEntry {
        String code;
        long timestamp;
        CodeEntry(String code, long timestamp) {
            this.code = code;
            this.timestamp = timestamp;
        }
    }
}