package net.zjvis.flint.data.hub.lib.talk;

import com.aliyun.dingtalkcontact_1_0.Client;
import com.aliyun.dingtalkcontact_1_0.models.GetUserHeaders;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;

import java.util.Map;

@Builder
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@Getter
public class UserInfoManager {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final String protocol;
    private final String regionId;

    /**
     * refer to: https://open.dingtalk.com/document/orgapp-server/dingtalk-retrieve-user-information
     *
     * @param userToken user token
     * @return user information with json format
     * @throws Exception when client configure error or get user info from ding talk error
     */
    String userInfo(String userToken) throws Exception {
        Client client = new Client(Config.build(Map.of(
                "protocol", protocol,
                "regionId", regionId
        )));
        GetUserHeaders getUserHeaders = GetUserHeaders.build(Map.of(
                "x-acs-dingtalk-access-token", userToken
        ));
        return OBJECT_MAPPER.writeValueAsString(
                client.getUserWithOptions("me", getUserHeaders, new RuntimeOptions())
                        .getBody());
    }
}
