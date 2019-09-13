package com.yuyuko.idempotent.api;

import com.yuyuko.idempotent.annotation.Idempotent;

import java.util.HashSet;
import java.util.LinkedHashSet;
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

    public int getMaxExecutionTime() {
        return maxExecutionTime;
    }

    public void setMaxExecutionTime(int maxExecutionTime) {
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

    public static class IdempotentInfoBuilder {
        private String id;

        private int maxExecutionTime = DEFAULT_MAX_EXECUTION_TIME;

        private int duration = DEFAULT_DURATION;

        private String prefix = DEFAULT_PREFIX;

        private Set<RollbackRule> rollbackRules = new LinkedHashSet<>();

        public static IdempotentInfoBuilder builder() {
            return new IdempotentInfoBuilder();
        }

        public IdempotentInfoBuilder id(String id) {
            this.id = id;
            return this;
        }

        public IdempotentInfoBuilder maxExecutionTime(int maxExecutionTime) {
            this.maxExecutionTime = maxExecutionTime;
            return this;
        }

        public IdempotentInfoBuilder duration(int duration) {
            this.duration = duration;
            return this;
        }

        public IdempotentInfoBuilder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public IdempotentInfo build() {
            IdempotentInfo idempotentInfo = new IdempotentInfo();
            idempotentInfo.setId(id);
            idempotentInfo.setMaxExecutionTime(maxExecutionTime);
            idempotentInfo.setDuration(duration);
            idempotentInfo.setPrefix(prefix);
            idempotentInfo.setRollbackRules(rollbackRules);
            return idempotentInfo;
        }


        public static IdempotentInfo build(Idempotent idempotent, String id) {
            IdempotentInfo idempotentInfo = new IdempotentInfo();
            idempotentInfo.setId(id);
            idempotentInfo.setMaxExecutionTime(idempotent.maxExecutionTime());
            idempotentInfo.setDuration(idempotent.duration());
            idempotentInfo.setPrefix(idempotent.prefix());
            Set<RollbackRule> rollbackRules = new LinkedHashSet<>();
            for (Class<?> rbRule : idempotent.rollbackFor()) {
                rollbackRules.add(new RollbackRule(rbRule));
            }
            for (String rbRule : idempotent.rollbackForClassName()) {
                rollbackRules.add(new RollbackRule(rbRule));
            }
            for (Class<?> rbRule : idempotent.noRollbackFor()) {
                rollbackRules.add(new NoRollbackRule(rbRule));
            }
            for (String rbRule : idempotent.noRollbackForClassName()) {
                rollbackRules.add(new NoRollbackRule(rbRule));
            }
            idempotentInfo.setRollbackRules(rollbackRules);
            return idempotentInfo;
        }

        public static IdempotentInfo build(String id) {
            IdempotentInfo idempotentInfo = new IdempotentInfo();
            idempotentInfo.setId(id);
            idempotentInfo.setMaxExecutionTime(DEFAULT_MAX_EXECUTION_TIME);
            idempotentInfo.setDuration(DEFAULT_DURATION);
            idempotentInfo.setPrefix(DEFAULT_PREFIX);
            idempotentInfo.setRollbackRules(new HashSet<>());
            return idempotentInfo;
        }
    }
}
