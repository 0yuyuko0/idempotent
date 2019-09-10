package com.yuyuko.idempotent.api;

import java.util.Set;

public class IdempotentInfo {
    public static final int DEFAULT_MAX_EXECUTION_TIME = 1000 * 10;

    public static final int DEFAULT_DURATION = 1000 * 60 * 20;

    public static final String DEFAULT_PREFIX = "idem:";

    private String id;

    private int maxExecutionTime;

    private int duration;

    private String prefix;

    private Set<RollbackRule> rollbackRules;

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getId() {
        return prefix.concat(id);
    }

    public void setPrefix(String prefix) {
        if (prefix == null)
            throw new IllegalArgumentException("prefix不能为null");
        this.prefix = prefix;
    }

    public int getMaxExecutionTime(){
        return maxExecutionTime;
    }

    public void setMaxExecutionTime(int maxExecutionTime){
        this.maxExecutionTime = maxExecutionTime;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<RollbackRule> getRollbackRules() {
        return rollbackRules;
    }

    public void setRollbackRules(Set<RollbackRule> rollbackRules) {
        this.rollbackRules = rollbackRules;
    }

    public boolean rollbackOn(Throwable ex) {

        RollbackRule winner = null;
        int deepest = Integer.MAX_VALUE;

        if (this.rollbackRules != null) {
            for (RollbackRule rule : this.rollbackRules) {
                int depth = rule.getDepth(ex);
                if (depth >= 0 && depth < deepest) {
                    deepest = depth;
                    winner = rule;
                }
            }
        }

        return winner == null || !(winner instanceof NoRollbackRule);
    }
}
