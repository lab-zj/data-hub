package net.zjvis.flint.data.hub.security.principal;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.util.Assert;

public class DingTalkAuthenticationToken extends AbstractAuthenticationToken {
    @Getter
    private final String authCode;

    public DingTalkAuthenticationToken(String authCode) {
        super(null);
        this.authCode = authCode;
        super.setAuthenticated(false);
    }

    @Override
    public Object getCredentials() {
        return this.authCode;
    }

    @Override
    public Object getPrincipal() {
        return this.authCode;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        // only accept isAuthenticated=false
        Assert.isTrue(!isAuthenticated,
                "this token should never be authenticated: use UniversalAuthenticationToken");
        super.setAuthenticated(false);
    }
}
