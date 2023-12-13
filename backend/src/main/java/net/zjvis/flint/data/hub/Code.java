package net.zjvis.flint.data.hub;

import com.google.common.base.Preconditions;
import lombok.Getter;

public enum Code {
    SUCCESS(200),
    BAD_REQUEST(4000),
    NOT_FOUND(4040),
    NEEDS_LOGIN(4030),
    NOT_PERMITTED(4031),
    UNSUPPORTED_MEDIA_TYPE(4150),
    UNPROCESSABLE_ENTITY(4220),
    BUG(5000),
    INTERNAL_ERROR(5001),
    ;

    @Getter
    private final long value;

    Code(long value) {
        this.value = value;
    }

    public static long combine(Code code, long controllerCode, long errorCode) {
        Preconditions.checkArgument(
                0 <= controllerCode && controllerCode < 1000,
                "invalid controller code: 0 <= controllerCode(%s) <= 1000",
                controllerCode
        );
        Preconditions.checkArgument(
                0 <= errorCode && errorCode < 1000,
                "invalid controller code: 0 <= errorCode(%s) <= 1000",
                errorCode
        );
        return code.value * 1000 * 1000 + controllerCode * 1000 + errorCode;
    }
}
