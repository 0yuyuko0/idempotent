package com.yuyuko.idempotent;

public class RejectedException extends RuntimeException {
    private String id;

    public RejectedException(String id) {
        super(null, null, true, false);
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
