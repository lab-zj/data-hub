package net.zjvis.flint.data.hub.controller.account.vo.user.info;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

@Getter
@EqualsAndHashCode
@ToString
@Schema(description = "ding talk user info")
public class DingTalkUserInfo {
    @Schema(description = "openId", example = "some-open-id")
    private final String openId;
    @Schema(description = "nickname", example = "some-nickname")
    private final String nickname;
    @Schema(description = "avatarUrl", example = "https://images/%E8%B4%A6%E5%8F%B7%E5%AF%86%E7%A0%81%E8%AE%BE%E7%BD%AE")
    private final String avatarUrl;

    @Builder
    @Jacksonized
    public DingTalkUserInfo(@NonNull String openId, String nickname, String avatarUrl) {
        this.openId = openId;
        this.nickname = nickname;
        this.avatarUrl = avatarUrl;
    }
}
