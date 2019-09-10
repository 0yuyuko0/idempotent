package com.yuyuko.idempotent.bean;

public class TestPOJO {
    private String username;

    public TestPOJO(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
