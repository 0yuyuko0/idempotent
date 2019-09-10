package com.yuyuko.idempotent;

public class DenyException extends RuntimeException {
    private String id;

    public DenyException(String id) {
        super(null, null, true, false);
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
