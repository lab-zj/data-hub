package net.zjvis.flint.data.hub.util;

import lombok.Getter;

@Getter
public enum JobStatusEnum {
    SUCCESS("success"),
    FAILED("failed"),
    INIT("init"),
    START("start"),
    FINISHED("finished"),
    CANCELED("canceled"),
    STOPPED("stopped")
    ;

    private String name;

    JobStatusEnum(String name) {
        this.name = name;
    }

}
