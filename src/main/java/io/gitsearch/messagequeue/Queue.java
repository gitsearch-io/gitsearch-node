package io.gitsearch.messagequeue;

public enum Queue {
    CLONE, UPDATE;

    private int priority;

    static {
        CLONE.priority = 10;
        UPDATE.priority = 0;
    }

    public int getPriority() {
        return priority;
    }
}
