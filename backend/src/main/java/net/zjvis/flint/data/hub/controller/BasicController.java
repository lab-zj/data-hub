package net.zjvis.flint.data.hub.controller;

import net.zjvis.flint.data.hub.StandardResponse;
import org.springframework.http.HttpStatus;

public interface BasicController {
    interface Error {
        HttpStatus getHttpStatus();
        long getCode();

        String getMessageTemplate();
    }

    /**
     * @return the id(from 1 to 999) of the controller
     */
    long id();

    default <T> StandardResponse<T> errorResponse(Error error, String... messageArgs) {
        return StandardResponse.<T>builder()
                .code(renderErrorCode(error.getHttpStatus(), error.getCode()))
                .message(null == messageArgs || messageArgs.length == 0
                        ? error.getMessageTemplate()
                        : String.format(error.getMessageTemplate(), (Object[]) messageArgs))
                .build();
    }
    default <T> StandardResponse<T> success(T data) {
        return StandardResponse.<T>builder()
                .data(data)
                .build();
    }

    private long renderErrorCode(HttpStatus httpStatus, long specificErrorCode) {
        return Long.parseLong(String.format("%03d%03d%03d", httpStatus.value(), id(), specificErrorCode));
    }
}
