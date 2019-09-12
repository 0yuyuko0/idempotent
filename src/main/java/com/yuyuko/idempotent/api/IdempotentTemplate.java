package com.yuyuko.idempotent.api;

import com.yuyuko.idempotent.DenyException;
import com.yuyuko.idempotent.redis.RedisUtils;

public class IdempotentTemplate {
    private RedisUtils redisUtils;

    public IdempotentTemplate(RedisUtils idempotentRedisUtils) {
        this.redisUtils = idempotentRedisUtils;
    }

    public Object execute(IdempotentExecutor business) throws Throwable {
        IdempotentInfo idempotentInfo = business.getIdempotentInfo();
        if (idempotentInfo == null)
            throw new RuntimeException("幂等操作信息为null");

        prepareIdempotent(idempotentInfo);

        Object res;

        try {
            res = business.execute();
        } catch (Throwable ex) {
            afterThrowing(idempotentInfo, ex);
            throw ex;
        }

        after(idempotentInfo);

        return res;
    }

    private void after(IdempotentInfo idempotentInfo) {
        redisUtils.setIfPresent(idempotentInfo.getId(), idempotentInfo.getDuration());
    }

    private void prepareIdempotent(IdempotentInfo idempotentInfo) {
        String id = idempotentInfo.getId();
        int maxExecutionTime = idempotentInfo.getMaxExecutionTime();
        boolean success = redisUtils.setIfAbsent(id, maxExecutionTime);
        if (!success)
            throw new DenyException(idempotentInfo.getId());
    }

    private void afterThrowing(IdempotentInfo idempotentInfo, Throwable ex) {
        if (idempotentInfo.rollbackOn(ex))
            redisUtils.delete(idempotentInfo.getId());
    }
}
