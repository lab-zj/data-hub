package net.zjvis.flint.data.hub.lib.talk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

@Builder(toBuilder = true)
@AllArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@EqualsAndHashCode
@ToString
public class DingTalkUserInfo {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static DingTalkUserInfo deserialize(String json) throws JsonProcessingException {
        return OBJECT_MAPPER.readValue(json, new TypeReference<>() {
        });
    }

    private final String avatarUrl;
    private final String email;
    private final String mobile;
    private final String nick;
    private final String openId;
    private final String unionId;
    private final String stateCode;

    public String toJson() throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsString(this);
    }
}
