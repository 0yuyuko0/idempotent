package com.yuyuko.idempotent.api;

public class IdempotentApi {
    private IdempotentManager idempotentManager;

    public IdempotentApi(IdempotentManager idempotentManager) {
        this.idempotentManager = idempotentManager;
    }

    public void prepare(IdempotentInfo idempotentInfo) {
        this.idempotentManager.prepare(idempotentInfo);
    }

    public void afterThrowing(String id) {
        this.idempotentManager.delete(id);
    }

    public void after(IdempotentInfo idempotentInfo) {
        this.idempotentManager.after(idempotentInfo);
    }
}