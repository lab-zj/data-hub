package net.zjvis.flint.data.hub.controller.account.vo.user.info;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

@Getter
@EqualsAndHashCode
@ToString
@Schema(description = "databaseUser info")
public class DatabaseUserInfo {
    @Schema(description = "username", example = "some-username")
    private final String username;

    @Builder
    @Jacksonized
    public DatabaseUserInfo(@NonNull String username) {
        this.username = username;
    }
}
