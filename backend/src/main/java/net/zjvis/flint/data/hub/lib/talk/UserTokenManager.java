package net.zjvis.flint.data.hub.lib.talk;

import com.aliyun.dingtalkoauth2_1_0.Client;
import com.aliyun.dingtalkoauth2_1_0.models.GetUserTokenRequest;
import com.aliyun.teaopenapi.models.Config;
import lombok.*;

import java.util.Map;

@Builder
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@Getter
public class UserTokenManager {
    private final String protocol;
    private final String regionId;
    private final String appKey;
    private final String appSecret;

    /**
     * https://open.dingtalk.com/document/orgapp-server/obtain-user-token
     *
     * @param authCode auth code
     * @return user token
     * @throws Exception when client configure error or get user token from ding talk error
     */
    public String userToken(String authCode) throws Exception {
        Client client = new Client(Config.build(Map.of(
                "protocol", protocol,
                "regionId", regionId
        )));
        GetUserTokenRequest getUserTokenRequest = GetUserTokenRequest.build(Map.of(
                "clientId", appKey,
                "clientSecret", appSecret,
                "code", authCode,
                "grantType", "authorization_code"
        ));
        return client.getUserToken(getUserTokenRequest)
                .getBody()
                .getAccessToken();
    }
}
