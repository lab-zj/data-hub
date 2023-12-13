package net.zjvis.flint.data.hub.lib.talk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.Builder;
import lombok.NonNull;

import java.util.concurrent.ExecutionException;

public class DingTalkUserManager {
    private final LoadingCache<String, String> cache;

    @Builder
    public DingTalkUserManager(DingTalkAppConfiguration dingTalkAppConfiguration) {
        this(UserTokenManager.builder()
                        .protocol(dingTalkAppConfiguration.getProtocol())
                        .regionId(dingTalkAppConfiguration.getRegionId())
                        .appKey(dingTalkAppConfiguration.getAppKey())
                        .appSecret(dingTalkAppConfiguration.getAppSecret())
                        .build(),
                UserInfoManager.builder()
                        .protocol(dingTalkAppConfiguration.getProtocol())
                        .regionId(dingTalkAppConfiguration.getRegionId())
                        .build()
        );
    }

    protected DingTalkUserManager(UserTokenManager userTokenManager, UserInfoManager userInfoManager) {
        CacheLoader<String, String> loader = new CacheLoader<>() {
            @Override
            @NonNull
            public String load(@NonNull String authCode) throws Exception {
                String userToken = userTokenManager.userToken(authCode);
                return userInfoManager.userInfo(userToken);
            }
        };
        cache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .build(loader);
    }

    public DingTalkUserInfo user(String authCode) {
        String userInfoJson = userInfoJson(authCode);
        try {
            return DingTalkUserInfo.deserialize(userInfoJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(String.format("user(%s) cannot be deserialized", userInfoJson));
        }
    }

    private String userInfoJson(String authCode) {
        try {
            return cache.get(authCode);
        } catch (ExecutionException e) {
            throw new RuntimeException(String.format("user not found by authCode(%s)", authCode));
        }
    }
}
