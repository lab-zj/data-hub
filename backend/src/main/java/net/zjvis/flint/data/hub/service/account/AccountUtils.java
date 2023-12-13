package net.zjvis.flint.data.hub.service.account;


import net.zjvis.flint.data.hub.entity.account.DatabaseUser;
import net.zjvis.flint.data.hub.entity.account.DingTalkUser;
import net.zjvis.flint.data.hub.entity.account.UniversalUser;
import net.zjvis.flint.data.hub.security.principal.UniversalAuthenticationToken;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class AccountUtils {
    private static final UsernameNotFoundException USER_NOT_FOUND_EXCEPTION
            = new UsernameNotFoundException("user not found");
    private final UniversalUserService universalUserService;
    private final DatabaseUserService databaseUserService;
    private final DingTalkUserService dingTalkUserService;

    public AccountUtils(
            UniversalUserService universalUserService,
            DatabaseUserService databaseUserService,
            DingTalkUserService dingTalkUserService
    ) {
        this.universalUserService = universalUserService;
        this.databaseUserService = databaseUserService;
        this.dingTalkUserService = dingTalkUserService;
    }

    public Optional<String> currentUserIdOptional() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof RememberMeAuthenticationToken){
            return  Optional.of(authentication.getName());
        }
        if (!(authentication instanceof UniversalAuthenticationToken)) {
            return Optional.empty();
        }
        return Optional.of(
                ((UniversalAuthenticationToken) authentication).getUniversalId()
        );
    }

    public String currentUserId() {
        return currentUserIdOptional()
                .orElseThrow(() -> USER_NOT_FOUND_EXCEPTION);
    }

    public Optional<UniversalUser> currentUserOptional() {
        Optional<String> userIdOptional = currentUserIdOptional();
        if (userIdOptional.isEmpty()) {
            return Optional.empty();
        }
        return universalUserService.userOptional(
                Long.valueOf(userIdOptional.get()));
    }

    public UniversalUser currentUser() {
        return currentUserOptional()
                .orElseThrow(() -> USER_NOT_FOUND_EXCEPTION);
    }

    public Optional<DatabaseUser> firstBindDatabaseUserOptional() {
        Optional<UniversalUser> universalUserOptional = currentUserOptional();
        if (universalUserOptional.isEmpty()) {
            return Optional.empty();
        }
        UniversalUser universalUser = universalUserOptional.get();
        List<DatabaseUser> databaseUserList = databaseUserService.bindUserList(universalUser.getId());
        return databaseUserList.stream().findFirst();
    }

    public Optional<DingTalkUser> firstBindDingTalkUserOptional() {
        Optional<UniversalUser> universalUserOptional = currentUserOptional();
        if (universalUserOptional.isEmpty()) {
            return Optional.empty();
        }
        UniversalUser universalUser = universalUserOptional.get();
        List<DingTalkUser> dingTalkUserList = dingTalkUserService.bindUserList(universalUser.getId());
        return dingTalkUserList.stream()
                .findFirst();
    }

    public Optional<String> nickName(Long universalUserId) {
        List<DingTalkUser> dingTalkUserList = dingTalkUserService.bindUserList(universalUserId);
        if (!dingTalkUserList.isEmpty()) {
            return Optional.of(dingTalkUserList.get(0).getNick());
        }
        List<DatabaseUser> databaseUserList = databaseUserService.bindUserList(universalUserId);
        if (!databaseUserList.isEmpty()) {
            return Optional.of(databaseUserList.get(0).getUsername());
        }
        return Optional.empty();
    }

    public List<UniversalUser> universalUserList() {
        return universalUserService.findAll();
    }
}
