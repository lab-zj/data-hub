package net.zjvis.flint.data.hub.security.principal;

import lombok.Getter;
import org.apache.http.auth.BasicUserPrincipal;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

import java.util.Collection;

public class UniversalAuthenticationToken extends AbstractAuthenticationToken {
    @Getter
    private final String universalId;
    private final BasicUserPrincipal principal;
    private UniversalCredentials credentials;

    public UniversalAuthenticationToken(UniversalCredentials credentials) {
        super(null);
        universalId = credentials.getUniversalUserId();
        this.principal = credentials.getUserPrincipal();
        this.credentials = credentials;
        super.setAuthenticated(false);
    }

    public UniversalAuthenticationToken(
            UniversalCredentials credentials,
            Collection<? extends GrantedAuthority> authorities
    ) {
        super(authorities);
        universalId = credentials.getUniversalUserId();
        this.principal = credentials.getUserPrincipal();
        this.credentials = credentials;
        super.setAuthenticated(true);
    }

    @Override
    public UniversalCredentials getCredentials() {
        return this.credentials;
    }

    @Override
    public BasicUserPrincipal getPrincipal() {
        return this.principal;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        // only accept isAuthenticated=false
        Assert.isTrue(!isAuthenticated,
                "Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
        super.setAuthenticated(false);
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
        this.credentials = null;
    }
}
