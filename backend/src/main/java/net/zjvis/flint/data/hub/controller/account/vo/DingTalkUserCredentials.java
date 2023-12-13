package net.zjvis.flint.data.hub.controller.account.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

@Getter
@EqualsAndHashCode
@ToString
@Schema(description = "credentials for ding talk user")
public class DingTalkUserCredentials {
    @Schema(description = "authCode", example = "some-auth-code")
    private final String authCode;
    @Schema(description = "state", example = "some-state")
    private final String state;

    @Builder
    @Jacksonized
    public DingTalkUserCredentials(String authCode, String state) {
        this.authCode = authCode;
        this.state = state;
    }
}
