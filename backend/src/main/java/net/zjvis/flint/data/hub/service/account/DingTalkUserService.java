package net.zjvis.flint.data.hub.service.account;


import net.zjvis.flint.data.hub.entity.account.DingTalkUser;
import net.zjvis.flint.data.hub.entity.account.UniversalUser;
import net.zjvis.flint.data.hub.lib.talk.DingTalkUserInfo;
import net.zjvis.flint.data.hub.lib.talk.DingTalkUserManager;
import net.zjvis.flint.data.hub.repository.DingTalkUserRepository;
import net.zjvis.flint.data.hub.security.Role;
import org.apache.http.auth.Credentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DingTalkUserService implements UserService<DingTalkUser, String> {
    public static final Logger LOGGER = LoggerFactory.getLogger(DingTalkUserService.class);
    private final DingTalkUserRepository dingTalkUserRepository;
    private final DingTalkUserManager dingTalkUserManager;

    public DingTalkUserService(
            DingTalkUserRepository dingTalkUserRepository,
            DingTalkUserManager dingTalkUserManager
    ) {
        this.dingTalkUserRepository = dingTalkUserRepository;
        this.dingTalkUserManager = dingTalkUserManager;
    }

    @Override
    public Optional<DingTalkUser> userOptional(String openId) {
        return dingTalkUserRepository.findByOpenId(openId);
    }

    @Override
    public DingTalkUser user(String openId) {
        return userOptional(openId)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("openId(%s) not found", openId)));
    }

    @Override
    public List<DingTalkUser> bindUserList(Long universalId) {
        return dingTalkUserRepository.findByUniversalUserId(universalId);
    }

    @Override
    public VerifyCredentialsResult<DingTalkUser> verify(Credentials credentials) {
        String authCode = credentials.getPassword();
        if (null == authCode || authCode.isEmpty()) {
            return VerifyCredentialsResult.<DingTalkUser>builder()
                    .success(false)
                    .user(null)
                    .message(String.format("authCode(%s) empty or null", authCode))
                    .build();
        }
        VerifyCredentialsResult<DingTalkUserInfo> verify = verify(authCode);
        if (!verify.isSuccess()) {
            return VerifyCredentialsResult.<DingTalkUser>builder()
                    .success(false)
                    .user(null)
                    .message(String.format("verify authCode(%s) failed: %s", authCode, verify.getMessage()))
                    .build();
        }
        DingTalkUserInfo dingTalkUserInfo = verify.getUser();
        return VerifyCredentialsResult.<DingTalkUser>builder()
                .success(true)
                .user(userOptional(dingTalkUserInfo.getOpenId())
                        .orElse(constructDingTalkUser(dingTalkUserInfo)))
                .message("")
                .build();
    }

    @Override
    public DingTalkUser update(DingTalkUser user) {
        return dingTalkUserRepository.save(user);
    }

    @Override
    public void delete(DingTalkUser user) {
        dingTalkUserRepository.delete(user);
    }

    public DingTalkUser login(String authCode, boolean createIfNotExists) {
        VerifyCredentialsResult<DingTalkUserInfo> verify = verify(authCode);
        if (!verify.isSuccess()) {
            throw new RuntimeException(String.format("verify authCode(%s) failed: %s", authCode, verify.getMessage()));
        }
        DingTalkUserInfo dingTalkUserInfo = verify.getUser();
        String openId = dingTalkUserInfo.getOpenId();
        Optional<DingTalkUser> dingTalkUser = userOptional(openId);
        if (dingTalkUser.isPresent()) {
            return dingTalkUser.get();
        }
        if (!createIfNotExists) {
            throw new UsernameNotFoundException(String.format("openId(%s) not found", openId));
        }
        DingTalkUser dingTalkUserToCreate = constructDingTalkUser(dingTalkUserInfo);
        LOGGER.info("creat dingTalkUser({})", dingTalkUserToCreate);
        return dingTalkUserRepository.save(dingTalkUserToCreate);
    }

    private DingTalkUser constructDingTalkUser(DingTalkUserInfo dingTalkUserInfo) {
        return DingTalkUser.builder()
                .openId(dingTalkUserInfo.getOpenId())
                .universalUser(UniversalUser.builder()
                        .role(Role.USER)
                        .build())
                .avatarUrl(dingTalkUserInfo.getAvatarUrl())
                .email(dingTalkUserInfo.getEmail())
                .mobile(dingTalkUserInfo.getMobile())
                .nick(dingTalkUserInfo.getNick())
                .unionId(dingTalkUserInfo.getUnionId())
                .build();
    }

    private VerifyCredentialsResult<DingTalkUserInfo> verify(String authCode) {
        DingTalkUserInfo dingTalkUserInfo = dingTalkUserManager.user(authCode);
        String openId = dingTalkUserInfo.getOpenId();
        if (null == openId || openId.isEmpty()) {
            return VerifyCredentialsResult.<DingTalkUserInfo>builder()
                    .success(false)
                    .user(null)
                    .message(String.format("openId(%s) is null or empty", openId))
                    .build();
        }
        return VerifyCredentialsResult.<DingTalkUserInfo>builder()
                .success(true)
                .user(dingTalkUserInfo)
                .message("")
                .build();
    }
}
