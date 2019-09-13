package com.yuyuko.idempotent.api;

import com.yuyuko.idempotent.RejectedException;
import com.yuyuko.idempotent.redis.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;

public class IdempotentManager {
    RedisUtils redisUtils;

    public IdempotentManager(RedisUtils redisUtils) {
        this.redisUtils = redisUtils;
    }

    public void prepare(IdempotentInfo idempotentInfo) {
        String id = idempotentInfo.getId();
        int maxExecutionTime = idempotentInfo.getMaxExecutionTime();
        boolean success = redisUtils.setIfAbsent(id, maxExecutionTime);
        if (!success)
            throw new RejectedException(idempotentInfo.getId());
    }

    public void after(IdempotentInfo idempotentInfo) {
        redisUtils.setIfPresent(idempotentInfo.getId(), idempotentInfo.getDuration());
    }

    public void afterThrowing(IdempotentInfo idempotentInfo, Throwable ex) {
        if (idempotentInfo.rollbackOn(ex))
            redisUtils.delete(idempotentInfo.getId());
    }

    void delete(String id){
        redisUtils.delete(id);
    }
}
