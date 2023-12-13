package net.zjvis.flint.data.hub.security.provider;

import com.google.common.base.Preconditions;
import net.zjvis.flint.data.hub.entity.account.DingTalkUser;
import net.zjvis.flint.data.hub.security.principal.DingTalkAuthenticationToken;
import net.zjvis.flint.data.hub.security.principal.UniversalAuthenticationToken;
import net.zjvis.flint.data.hub.security.principal.UniversalCredentials;
import net.zjvis.flint.data.hub.service.account.DingTalkUserService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class DingTalkAuthenticationProvider implements AuthenticationProvider {

    private final DingTalkUserService dingTalkUserService;

    public DingTalkAuthenticationProvider(DingTalkUserService dingTalkUserService) {
        this.dingTalkUserService = dingTalkUserService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Preconditions.checkArgument(
                authentication instanceof DingTalkAuthenticationToken,
                String.format("authentication(%s) type is not %s",
                        authentication.getClass(), DingTalkAuthenticationToken.class));
        String authCode = ((DingTalkAuthenticationToken) authentication).getAuthCode();
        DingTalkUser dingTalkUser = dingTalkUserService.login(authCode, true);
        return new UniversalAuthenticationToken(
                UniversalCredentials.builder()
                        .universalUserId(dingTalkUser.getUniversalUser().getId().toString())
                        .authCode(authCode)
                        .build(),
                dingTalkUser.getUniversalUser().getAuthorities()
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(DingTalkAuthenticationToken.class);
    }
}
