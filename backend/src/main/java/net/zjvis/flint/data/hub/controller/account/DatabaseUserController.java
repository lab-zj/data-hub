package net.zjvis.flint.data.hub.controller.account;


import com.google.common.base.Preconditions;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import net.zjvis.flint.data.hub.StandardResponse;
import net.zjvis.flint.data.hub.controller.BasicController;
import net.zjvis.flint.data.hub.controller.account.vo.DatabaseUserCredentials;
import net.zjvis.flint.data.hub.entity.account.DatabaseUser;
import net.zjvis.flint.data.hub.entity.account.DingTalkUser;
import net.zjvis.flint.data.hub.entity.account.UniversalUser;
import net.zjvis.flint.data.hub.security.Role;
import net.zjvis.flint.data.hub.security.principal.UniversalCredentials;
import net.zjvis.flint.data.hub.service.account.AccountUtils;
import net.zjvis.flint.data.hub.service.account.DatabaseUserService;
import net.zjvis.flint.data.hub.service.account.DingTalkUserService;
import net.zjvis.flint.data.hub.service.account.UserService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/database-user")
public class DatabaseUserController implements BasicController {
    public enum ERROR implements BasicController.Error {
        USERNAME_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, 1L, "username(%s) already exists"),
        DATABASE_USER_NOT_BIND(HttpStatus.BAD_REQUEST, 2L,
                "cannot find any databaseUser binding to current user(%s)"),
        PASSWORD_ERROR(HttpStatus.BAD_REQUEST, 3L, "password error"),
        CREDENTIALS_NOT_VERIFIED(HttpStatus.UNAUTHORIZED, 4L, "verify your credentials first"),
        DATABASE_USER_ALREADY_INITIALIZED(HttpStatus.BAD_REQUEST, 5L,
                "databaseUser already initialized, cannot initialize again"),
        NOT_LOGIN_USER(HttpStatus.UNAUTHORIZED, 6L, "login first"),
        UNIVERSAL_USER_ID_NOT_MATCHES(HttpStatus.UNAUTHORIZED, 7L,
                "universal user id(%s) not matches to current universal user id(%s)"),
        NOTHING_TO_UPDATE(HttpStatus.BAD_REQUEST, 8L, "nothing to update"),
        VERIFY_CREDENTIALS_FAILED(HttpStatus.UNAUTHORIZED, 9L,
                "verify credentials(username=%s) failed"),
        TARGET_USER_ALREADY_BIND_WITH_ANOTHER_DING_TALK_USER(HttpStatus.BAD_REQUEST, 10L,
                "target user(%s) already bind with another ding talk user"),
        DING_TALK_USER_NOT_BIND(HttpStatus.BAD_REQUEST, 11L,
                "cannot find any ding talk user binding to current user(%s)"),
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

    private final AccountUtils accountUtils;
    private final DatabaseUserService databaseUserService;
    private final DingTalkUserService dingTalkUserService;
    private final AccountSessionManager accountSessionManager;

    public DatabaseUserController(
            AccountUtils accountUtils,
            DatabaseUserService databaseUserService,
            DingTalkUserService dingTalkUserService,
            AccountSessionManager accountSessionManager
    ) {
        this.accountUtils = accountUtils;
        this.databaseUserService = databaseUserService;
        this.dingTalkUserService = dingTalkUserService;
        this.accountSessionManager = accountSessionManager;
    }

    @Override
    public long id() {
        return 1L;
    }

    @Operation(summary = "register databaseUser")
    @PostMapping("/register")
    public StandardResponse<Void> register(@RequestBody DatabaseUserCredentials databaseUserCredentials) {
        String username = databaseUserCredentials.getUsername();
        Optional<DatabaseUser> databaseUserOptional = databaseUserService.userOptional(username);
        if (databaseUserOptional.isPresent()) {
            return errorResponse(ERROR.USERNAME_ALREADY_EXISTS, username);
        }
        databaseUserService.updateWithRawPassword(DatabaseUser.builder()
                .username(username)
                .password(databaseUserCredentials.getPassword())
                .universalUser(UniversalUser.builder()
                        .role(Role.USER)
                        .build())
                .build());
        return success(null);
    }

    @Operation(summary = "initialize username and password")
    @PostMapping("/initialize")
    public StandardResponse<DatabaseUserCredentials> initialize(
            @RequestBody DatabaseUserCredentials databaseUserCredentials) {
        Optional<UniversalUser> currentUserOptional = accountUtils.currentUserOptional();
        if (currentUserOptional.isEmpty()) {
            return errorResponse(ERROR.NOT_LOGIN_USER);
        }
        Optional<DatabaseUser> databaseUserOptional = accountUtils.firstBindDatabaseUserOptional();
        if (databaseUserOptional.isPresent()) {
            return errorResponse(ERROR.DATABASE_USER_ALREADY_INITIALIZED);
        }
        String username = Optional.ofNullable(databaseUserCredentials.getUsername())
                // TODO guarantee username is unique
                .orElse(RandomStringUtils.randomAlphanumeric(8));
        String password = Optional.ofNullable(databaseUserCredentials.getPassword())
                .orElse(RandomStringUtils.randomAlphanumeric(16));
        if (databaseUserService.userOptional(username).isPresent()) {
            return errorResponse(ERROR.USERNAME_ALREADY_EXISTS, username);
        }
        DatabaseUser updatedDatabaseUser = databaseUserService.updateWithRawPassword(DatabaseUser.builder()
                .username(username)
                .password(password)
                .universalUser(currentUserOptional.get())
                .build());
        return success(DatabaseUserCredentials.builder()
                .username(updatedDatabaseUser.getUsername())
                // raw password is not available from updatedDatabaseUser
                .password(password)
                .build());
    }

    @Operation(summary = "update username and password")
    @PostMapping("/update")
    public StandardResponse<String> update(
            @RequestBody DatabaseUserCredentials databaseUserCredentials,
            HttpSession httpSession
    ) {
        Optional<UniversalUser> currentUserOptional = accountUtils.currentUserOptional();
        if (currentUserOptional.isEmpty()) {
            return errorResponse(ERROR.NOT_LOGIN_USER);
        }
        UniversalUser universalUserVerified = accountSessionManager.universalUserVerified(httpSession);
        if (null == universalUserVerified) {
            return errorResponse(ERROR.CREDENTIALS_NOT_VERIFIED);
        }
        if (!Objects.equals(universalUserVerified.getId(), currentUserOptional.get().getId())) {
            return errorResponse(ERROR.UNIVERSAL_USER_ID_NOT_MATCHES,
                    String.valueOf(universalUserVerified.getId()),
                    String.valueOf(currentUserOptional.get().getId()));
        }
        Optional<DatabaseUser> currentDatabaseUserOptional = accountUtils.firstBindDatabaseUserOptional();
        if (currentDatabaseUserOptional.isEmpty()) {
            // call initialize if there is no bind databaseUser
            return errorResponse(ERROR.DATABASE_USER_NOT_BIND, String.valueOf(universalUserVerified.getId()));
        }
        boolean changingUsername = null != databaseUserCredentials.getUsername();
        boolean changingPassword = null != databaseUserCredentials.getPassword();
        DatabaseUser updatedDatabaseUser;
        if (changingUsername) {
            // check target username exists when changing username
            String targetUsername = databaseUserCredentials.getUsername();
            Optional<DatabaseUser> databaseUserOptional = databaseUserService.userOptional(targetUsername);
            if (databaseUserOptional.isPresent()) {
                return errorResponse(ERROR.USERNAME_ALREADY_EXISTS, targetUsername);
            }
            if (changingPassword) {
                updatedDatabaseUser = databaseUserService.updateWithRawPassword(
                        currentDatabaseUserOptional.get()
                                .toBuilder()
                                .username(databaseUserCredentials.getUsername())
                                .password(databaseUserCredentials.getPassword())
                                .build());
            } else {
                // if changing username only
                updatedDatabaseUser = databaseUserService.update(
                        currentDatabaseUserOptional.get()
                                .toBuilder()
                                .username(databaseUserCredentials.getUsername())
                                .build());
            }
        } else {
            if (!changingPassword) {
                return errorResponse(ERROR.NOTHING_TO_UPDATE);
            }
            // if changing password only
            updatedDatabaseUser = databaseUserService.updateWithRawPassword(
                    currentDatabaseUserOptional.get()
                            .toBuilder()
                            .password(databaseUserCredentials.getPassword())
                            .build());
        }
        accountSessionManager.removeAllVerifiedFlags(httpSession);
        return success(updatedDatabaseUser.getUsername());
    }

    @Operation(summary = "verify identity")
    @PostMapping(path = "/verify", consumes = MediaType.TEXT_PLAIN_VALUE)
    public StandardResponse<String> verify(
            @Schema(description = "password for current user", example = "some-password")
            @RequestBody String password,
            HttpSession httpSession
    ) {
        Optional<DatabaseUser> databaseUserOptional = accountUtils.firstBindDatabaseUserOptional();
        if (databaseUserOptional.isEmpty()) {
            return errorResponse(ERROR.DATABASE_USER_NOT_BIND,
                    accountUtils.currentUserIdOptional().orElse("anonymous"));
        }
        Optional<DatabaseUser> checkedDatabaseUserOptional
                = checkCredentials(databaseUserOptional.get().getUsername(), password);
        if (checkedDatabaseUserOptional.isEmpty()) {
            return errorResponse(ERROR.PASSWORD_ERROR);
        }
        DatabaseUser checkedDatabaseUser = checkedDatabaseUserOptional.get();
        accountSessionManager.setDatabaseUserVerified(httpSession, checkedDatabaseUser);
        return success(checkedDatabaseUser.getUsername());
    }

    @Operation(summary = "bind existing databaseUser")
    @PostMapping("/bind")
    public StandardResponse<Void> bindExisting(@RequestBody DatabaseUserCredentials databaseUserCredentials) {
        Optional<UniversalUser> currentUserOptional = accountUtils.currentUserOptional();
        if (currentUserOptional.isEmpty()) {
            return errorResponse(ERROR.NOT_LOGIN_USER);
        }
        // forbid bind to another databaseUser
        if (accountUtils.firstBindDatabaseUserOptional().isPresent()) {
            return errorResponse(ERROR.DATABASE_USER_ALREADY_INITIALIZED);
        }
        String targetUsername = databaseUserCredentials.getUsername();
        Optional<DatabaseUser> verifiedDatabaseUser
                = checkCredentials(targetUsername, databaseUserCredentials.getPassword());
        if (verifiedDatabaseUser.isEmpty()) {
            return errorResponse(ERROR.VERIFY_CREDENTIALS_FAILED, targetUsername);
        }
        // NOTE: account merge logic should be checked to avoid conflicts
        Long targetUniversalUserId = verifiedDatabaseUser.get().getUniversalUser().getId();
        if (!dingTalkUserService.bindUserList(targetUniversalUserId).isEmpty()) {
            return errorResponse(ERROR.TARGET_USER_ALREADY_BIND_WITH_ANOTHER_DING_TALK_USER,
                    String.valueOf(targetUsername));
        }
        Optional<DingTalkUser> dingTalkUserOptional = accountUtils.firstBindDingTalkUserOptional();
        if (dingTalkUserOptional.isEmpty()) {
            return errorResponse(ERROR.DING_TALK_USER_NOT_BIND,
                    accountUtils.currentUserIdOptional().orElse("anonymous"));
        }
        dingTalkUserService.update(dingTalkUserOptional.get()
                .toBuilder()
                .universalUser(verifiedDatabaseUser.get().getUniversalUser())
                .build());
        return success(null);
    }

    private Optional<DatabaseUser> checkCredentials(String username, String password) {
        UserService.VerifyCredentialsResult<DatabaseUser> verify = databaseUserService.verify(
                UniversalCredentials.builder()
                        .universalUserId(username)
                        .authCode(password)
                        .build()
        );
        return Optional.ofNullable(verify.isSuccess() ? verify.getUser() : null);
    }
}
