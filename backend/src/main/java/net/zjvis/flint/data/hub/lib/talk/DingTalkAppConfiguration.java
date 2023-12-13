package net.zjvis.flint.data.hub.lib.talk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
public class DingTalkAppConfiguration {
    private final String protocol;
    private final String regionId;
    private final String appKey;
    private final String appSecret;
}
