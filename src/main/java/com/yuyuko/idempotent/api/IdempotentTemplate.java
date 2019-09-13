package com.yuyuko.idempotent.api;

import com.yuyuko.idempotent.RejectedException;

public class IdempotentTemplate {
    private IdempotentManager idempotentManager;

    public IdempotentTemplate(IdempotentManager idempotentManager) {
        this.idempotentManager = idempotentManager;
    }

    public Object execute(IdempotentExecutor business) throws Throwable {
        IdempotentInfo idempotentInfo = business.getIdempotentInfo();
        if (idempotentInfo == null)
            throw new RuntimeException("幂等操作信息为null");

        this.idempotentManager.prepare(idempotentInfo);

        Object res;

        try {
            res = business.execute();
        } catch (Throwable ex) {
            this.idempotentManager.afterThrowing(idempotentInfo, ex);
            throw ex;
        }

        this.idempotentManager.after(idempotentInfo);

        return res;
    }
}
