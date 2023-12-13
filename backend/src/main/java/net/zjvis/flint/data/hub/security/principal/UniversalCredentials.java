package net.zjvis.flint.data.hub.security.principal;

import lombok.*;
import org.apache.http.auth.BasicUserPrincipal;
import org.apache.http.auth.Credentials;

@Builder
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class UniversalCredentials implements Credentials {
    @Getter
    private final String universalUserId;
    @Getter
    private final String authCode;

    @Override
    public BasicUserPrincipal getUserPrincipal() {
        return new BasicUserPrincipal(universalUserId);
    }

    @Override
    public String getPassword() {
        return authCode;
    }
}
