package net.zjvis.flint.data.hub.util;

import java.util.Set;

public enum ActionStatusEnum {
    CREATED,
    RUNNING,
    FAILED,
    SUCCEED,
    CANCELED,
    ;
    private static final Set<ActionStatusEnum> STOPPED_SET = Set.of(CREATED, FAILED, SUCCEED, CANCELED);

    public boolean stopped() {
        return STOPPED_SET.contains(this);
    }
}
