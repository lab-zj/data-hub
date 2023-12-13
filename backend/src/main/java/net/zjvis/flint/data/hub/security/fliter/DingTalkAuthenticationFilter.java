package net.zjvis.flint.data.hub.security.fliter;

import net.zjvis.flint.data.hub.security.exception.AuthenticationCodeNotFoundException;
import net.zjvis.flint.data.hub.security.principal.DingTalkAuthenticationToken;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DingTalkAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final String AUTH_CODE_PARAMETER = "authCode";
    private static final String STATE_PARAMETER = "state";
    private boolean postOnly = true;

    public DingTalkAuthenticationFilter(
            String patternUrl, AuthenticationManager authenticationManager) {
        super(
                new AntPathRequestMatcher(patternUrl, "POST"),
                authenticationManager
        );
    }

    public Authentication attemptAuthentication(
            HttpServletRequest request, HttpServletResponse response
    ) throws AuthenticationException {
        if (this.postOnly && !request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Aauthentication method not supported: " + request.getMethod());
        } else {
            String authCode = obtainAuthCode(request);
            // TODO skip with same state
            String state = obtainState(request);
            DingTalkAuthenticationToken authRequest = new DingTalkAuthenticationToken(authCode);
            authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
            this.setDetails(request, authRequest);
            return this.getAuthenticationManager().authenticate(authRequest);
        }
    }

    @NonNull
    protected String obtainAuthCode(HttpServletRequest request) {
        String authCode = request.getParameter(AUTH_CODE_PARAMETER);
        if (StringUtils.isBlank(authCode)) {
            throw new AuthenticationCodeNotFoundException(
                    String.format("%s(%s) is blank", AUTH_CODE_PARAMETER, authCode));
        }
        return authCode;
    }

    @NonNull
    protected String obtainState(HttpServletRequest request) {
        String state = request.getParameter(STATE_PARAMETER);
        if (StringUtils.isBlank(state)) {
            throw new AuthenticationCodeNotFoundException(String.format("state(%s) is blank", state));
        }
        return state;
    }

    protected void setDetails(HttpServletRequest request, DingTalkAuthenticationToken authRequest) {
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
    }

    public void setPostOnly(boolean postOnly) {
        this.postOnly = postOnly;
    }
}
