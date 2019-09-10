package com.yuyuko.idempotent.api;

public class NoRollbackRule extends RollbackRule {
    public NoRollbackRule(Class<?> clazz) {
        super(clazz);
    }


    public NoRollbackRule(String exceptionName) {
        super(exceptionName);
    }

    @Override
    public boolean equals(Object other) {
        return super.equals(other);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return "No" + super.toString();
    }
}