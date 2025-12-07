package com.sky.service.impl;

import com.sky.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

@Service
public class ShopServiceImpl implements ShopService {

    private static final String redis_key = "status";

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 获取营业状态
     * @return
     */
    @Override
    public Integer getStatus() {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Integer object = (Integer) valueOperations.get(redis_key);
        return object;
    }

    /**
     * 设置营业状态
     * @param status
     */
    @Override
    public void setStatus(Integer status) {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.set(redis_key, status);
    }
}
