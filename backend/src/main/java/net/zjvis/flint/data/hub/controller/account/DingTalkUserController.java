package net.zjvis.flint.data.hub.controller.account;


import com.google.common.base.Preconditions;
import io.swagger.v3.oas.annotations.Operation;
import lombok.Getter;
import net.zjvis.flint.data.hub.StandardResponse;
import net.zjvis.flint.data.hub.controller.BasicController;
import net.zjvis.flint.data.hub.controller.account.vo.DingTalkUserCredentials;
import net.zjvis.flint.data.hub.entity.account.DingTalkUser;
import net.zjvis.flint.data.hub.entity.account.UniversalUser;
import net.zjvis.flint.data.hub.lib.talk.DingTalkAppConfiguration;
import net.zjvis.flint.data.hub.security.principal.UniversalCredentials;
import net.zjvis.flint.data.hub.service.account.AccountUtils;
import net.zjvis.flint.data.hub.service.account.DingTalkUserService;
import net.zjvis.flint.data.hub.service.account.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/ding-talk-user")
public class DingTalkUserController implements BasicController {
    public enum ERROR implements BasicController.Error {
        NOT_LOGIN_USER(HttpStatus.UNAUTHORIZED, 1L, "login first"),
        DING_TALK_USER_NOT_BIND(HttpStatus.BAD_REQUEST, 2L,
                "cannot find any ding talk user binding to current user(%s)"),
        AUTH_CODE_ERROR(HttpStatus.UNAUTHORIZED, 3L, "verify authCode(%s) failed"),
        VERIFIED_DING_TALK_USER_NOT_EXISTS(HttpStatus.UNAUTHORIZED, 4L,
                "verified ding talk user(authCode=%s) not exists"),
        VERIFIED_USER_IS_NOT_CURRENT_USER(HttpStatus.UNAUTHORIZED, 5L,
                "verified user(%s) is not current user(%s)"),
        CREDENTIALS_NOT_VERIFIED(HttpStatus.UNAUTHORIZED, 6L, "verify your credentials first"),
        DING_TALK_USER_NOT_VERIFIED(HttpStatus.UNAUTHORIZED, 7L, "verify your ding talk user first"),
        UNIVERSAL_USER_ID_NOT_MATCHES(HttpStatus.UNAUTHORIZED, 8L,
                "universal user id(%s) not matches to current universal user id(%s)"),
        ALREADY_BIND_WITH_DING_TALK_USER(HttpStatus.BAD_REQUEST, 9L,
                "current user(%s) already bind with a ding talk user(%s): remove bind first"),
        DING_TALK_USER_ALREADY_BIND_WITH_ANOTHER_USER(HttpStatus.BAD_REQUEST, 10L,
                "ding talk user(%s) already bind with another user"),
        ;

        @Getter
        private final HttpStatus httpStatus;
        @Getter
        private final long code;
        @Getter
        private final String messageTemplate;

        ERROR(HttpStatus httpStatus, long code, String messageTemplate) {
            this.httpStatus = httpStatus;
            this.code = code;
            this.messageTemplate = messageTemplate;
            Preconditions.checkArgument(code >= 1 && code < 1000, "code must be in [1, 1000)");
        }
    }

    private final String dingTalkLoginPrefix;
    private final DingTalkAppConfiguration dingTalkAppConfiguration;
    private final AccountUtils accountUtils;
    private final DingTalkUserService dingTalkUserService;
    private final AccountSessionManager accountSessionManager;

    public DingTalkUserController(
            @Value("${application.dingtalk.app.authUrlPrefix}") String dingTalkLoginPrefix,
            DingTalkAppConfiguration dingTalkAppConfiguration,
            AccountUtils accountUtils,
            DingTalkUserService dingTalkUserService,
            AccountSessionManager accountSessionManager
    ) {
        this.dingTalkLoginPrefix = dingTalkLoginPrefix;
        this.dingTalkAppConfiguration = dingTalkAppConfiguration;
        this.accountUtils = accountUtils;
        this.dingTalkUserService = dingTalkUserService;
        this.accountSessionManager = accountSessionManager;
    }

    @Override
    public long id() {
        return 2L;
    }

    @GetMapping("/url/auth")
    public StandardResponse<String> urlAuth() {
        return success(String.format(
                "%s?response_type=code&client_id=%s&scope=openid&state=dddd&prompt=consent",
                dingTalkLoginPrefix, dingTalkAppConfiguration.getAppKey()
        ));
    }

    @Operation(summary = "verify identity")
    @PostMapping("/verify")
    public StandardResponse<String> verify(
            @RequestBody DingTalkUserCredentials dingTalkUserCredentials,
            HttpSession httpSession
    ) {
        // TODO use state to avoid replay attack
        String authCode = dingTalkUserCredentials.getAuthCode();
        Optional<DingTalkUser> currentDingTalkUserOptional = accountUtils.firstBindDingTalkUserOptional();
        if (currentDingTalkUserOptional.isEmpty()) {
            return errorResponse(ERROR.DING_TALK_USER_NOT_BIND,
                    accountUtils.currentUserIdOptional().orElse("anonymous"));
        }
        Optional<DingTalkUser> dingTalkUserOptional = checkCredentials(authCode);
        if (dingTalkUserOptional.isEmpty()) {
            return errorResponse(ERROR.AUTH_CODE_ERROR, authCode);
        }
        DingTalkUser verifiedDingTalkUser = dingTalkUserOptional.get();
        if (null == verifiedDingTalkUser.getId()) {
            return errorResponse(ERROR.VERIFIED_DING_TALK_USER_NOT_EXISTS, authCode);
        }
        Long verifiedUserId = verifiedDingTalkUser.getUniversalUser().getId();
        Long currentUserId = currentDingTalkUserOptional.get()
                .getUniversalUser()
                .getId();
        if (!Objects.equals(verifiedUserId, currentUserId)) {
            return errorResponse(ERROR.VERIFIED_USER_IS_NOT_CURRENT_USER,
                    String.valueOf(verifiedUserId), String.valueOf(currentUserId));
        }
        accountSessionManager.setDingTalkUserVerified(httpSession, verifiedDingTalkUser);
        return success(verifiedDingTalkUser.getOpenId());
    }

    @DeleteMapping("/bind")
    public StandardResponse<Void> deleteBind(HttpSession httpSession) {
        Optional<DingTalkUser> currentDingTalkUserOptional = accountUtils.firstBindDingTalkUserOptional();
        if (currentDingTalkUserOptional.isEmpty()) {
            return errorResponse(ERROR.DING_TALK_USER_NOT_BIND,
                    accountUtils.currentUserIdOptional().orElse("anonymous"));
        }
        UniversalUser universalUserVerified = accountSessionManager.universalUserVerified(httpSession);
        if (null == universalUserVerified) {
            return errorResponse(ERROR.CREDENTIALS_NOT_VERIFIED);
        }
        DingTalkUser currentDingTalkUser = currentDingTalkUserOptional.get();
        Long currentUniversalUserId = currentDingTalkUser
                .getUniversalUser()
                .getId();
        Long verifiedUniversalUserId = universalUserVerified.getId();
        if (!Objects.equals(verifiedUniversalUserId, currentUniversalUserId)) {
            return errorResponse(ERROR.UNIVERSAL_USER_ID_NOT_MATCHES,
                    String.valueOf(verifiedUniversalUserId), String.valueOf(currentUniversalUserId));
        }
        dingTalkUserService.delete(currentDingTalkUser);
        accountSessionManager.removeAllVerifiedFlags(httpSession);
        return success(null);
    }

    @PostMapping("/bind")
    public StandardResponse<String> addBind(@RequestBody DingTalkUserCredentials dingTalkUserCredentials) {
        Optional<UniversalUser> currentUniversalUserOptional = accountUtils.currentUserOptional();
        if (currentUniversalUserOptional.isEmpty()) {
            return errorResponse(ERROR.NOT_LOGIN_USER);
        }
        Optional<DingTalkUser> currentDingTalkUserOptional = accountUtils.firstBindDingTalkUserOptional();
        if (currentDingTalkUserOptional.isPresent()) {
            return errorResponse(ERROR.ALREADY_BIND_WITH_DING_TALK_USER,
                    accountUtils.currentUserIdOptional().orElse("anonymous"),
                    currentDingTalkUserOptional.get().getOpenId());
        }
        // TODO use state to avoid replay attack
        String authCode = dingTalkUserCredentials.getAuthCode();
        Optional<DingTalkUser> verifiedDingTalkUserOptional = checkCredentials(authCode);
        if (verifiedDingTalkUserOptional.isEmpty()) {
            return errorResponse(ERROR.AUTH_CODE_ERROR, authCode);
        }
        DingTalkUser verifiedDingTalkUser = verifiedDingTalkUserOptional.get();
        if (null != verifiedDingTalkUser.getId()) {
            return errorResponse(ERROR.DING_TALK_USER_ALREADY_BIND_WITH_ANOTHER_USER,
                    verifiedDingTalkUser.getOpenId());
        }
        dingTalkUserService.update(verifiedDingTalkUser.toBuilder()
                .universalUser(currentUniversalUserOptional.get())
                .build());
        return success(verifiedDingTalkUser.getOpenId());
    }

    private Optional<DingTalkUser> checkCredentials(String authCode) {
        UserService.VerifyCredentialsResult<DingTalkUser> verify = dingTalkUserService.verify(
                UniversalCredentials.builder()
                        .universalUserId(accountUtils.currentUserId())
                        .authCode(authCode)
                        .build());
        return Optional.ofNullable(verify.isSuccess() ? verify.getUser() : null);
    }
}
