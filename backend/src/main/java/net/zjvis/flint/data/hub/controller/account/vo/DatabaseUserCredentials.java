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
@Schema(description = "credentials for database user")
public class DatabaseUserCredentials {
    @Schema(description = "username", example = "some-username")
    private final String username;
    @Schema(description = "password", example = "some-password")
    private final String password;

    @Builder
    @Jacksonized
    public DatabaseUserCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
