package com.yuyuko.idempotent.api;

public interface IdempotentExecutor {
    Object execute() throws Throwable;

    IdempotentInfo getIdempotentInfo();
}
