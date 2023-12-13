package net.zjvis.flint.data.hub.controller.account.vo.user.info;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import net.zjvis.flint.data.hub.entity.account.UniversalUser;

@Getter
@EqualsAndHashCode
@ToString
@Schema(description = "databaseUser info")
public class UserInfo {
    private final UniversalUser universalUser;
    private final DatabaseUserInfo databaseUserInfo;
    private final DingTalkUserInfo dingTalkUserInfo;

    @Builder
    @Jacksonized
    public UserInfo(
            UniversalUser universalUser,
            DatabaseUserInfo databaseUserInfo,
            DingTalkUserInfo dingTalkUserInfo
    ) {
        this.universalUser = universalUser;
        this.databaseUserInfo = databaseUserInfo;
        this.dingTalkUserInfo = dingTalkUserInfo;
    }
}
