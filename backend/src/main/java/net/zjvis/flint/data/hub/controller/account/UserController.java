package net.zjvis.flint.data.hub.controller.account;

import net.zjvis.flint.data.hub.StandardResponse;
import net.zjvis.flint.data.hub.controller.BasicController;
import net.zjvis.flint.data.hub.controller.account.vo.user.info.DatabaseUserInfo;
import net.zjvis.flint.data.hub.controller.account.vo.user.info.DingTalkUserInfo;
import net.zjvis.flint.data.hub.controller.account.vo.user.info.UserInfo;
import net.zjvis.flint.data.hub.service.account.AccountUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController implements BasicController {
    private final AccountUtils accountUtils;

    public UserController(AccountUtils accountUtils) {
        this.accountUtils = accountUtils;
    }

    @Override
    public long id() {
        return 3L;
    }

    @GetMapping("/who-am-i")
    public StandardResponse<UserInfo> whoAmI() {
        return success(UserInfo.builder()
                .universalUser(accountUtils.currentUserOptional().orElse(null))
                .databaseUserInfo(accountUtils.firstBindDatabaseUserOptional()
                        .map(databaseUser -> DatabaseUserInfo.builder()
                                .username(databaseUser.getUsername())
                                .build())
                        .orElse(null))
                .dingTalkUserInfo(accountUtils.firstBindDingTalkUserOptional()
                        .map(dingTalkUser -> DingTalkUserInfo.builder()
                                .openId(dingTalkUser.getOpenId())
                                .avatarUrl(dingTalkUser.getAvatarUrl())
                                .nickname(dingTalkUser.getNick())
                                .build())
                        .orElse(null))
                .build());
    }
}
