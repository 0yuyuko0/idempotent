package com.yuyuko.idempotent.api;

public class RollbackRule {
    private final String exceptionName;

    public RollbackRule(String exceptionName) {
        if (exceptionName == null || exceptionName.equals("")) {
            throw new IllegalArgumentException("'exceptionName' cannot be null or empty");
        }
        this.exceptionName = exceptionName;
    }

    public RollbackRule(Class<?> clazz) {
        if (clazz == null) {
            throw new NullPointerException("'clazz' cannot be null");
        }
        if (!Throwable.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException(
                    "Cannot construct rollback rule from [" + clazz.getName() + "]: it's not a " +
                            "Throwable");
        }
        this.exceptionName = clazz.getName();
    }

    public int getDepth(Throwable ex) {
        return getDepth(ex.getClass(), 0);
    }


    private int getDepth(Class<?> exceptionClass, int depth) {
        if (exceptionClass.getName().contains(this.exceptionName)) {
            // Found it!
            return depth;
        }
        // If we've gone as far as we can go and haven't found it...
        if (exceptionClass == Throwable.class) {
            return -1;
        }
        return getDepth(exceptionClass.getSuperclass(), depth + 1);
    }


    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof RollbackRule)) {
            return false;
        }
        RollbackRule rhs = (RollbackRule) other;
        return this.exceptionName.equals(rhs.exceptionName);
    }

    @Override
    public int hashCode() {
        return this.exceptionName.hashCode();
    }

    @Override
    public String toString() {
        return "RollbackRule with pattern [" + this.exceptionName + "]";
    }
}
