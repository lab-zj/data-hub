package net.zjvis.flint.data.hub.security.provider;



import net.zjvis.flint.data.hub.entity.account.DatabaseUser;
import net.zjvis.flint.data.hub.security.principal.UniversalAuthenticationToken;
import net.zjvis.flint.data.hub.security.principal.UniversalCredentials;
import net.zjvis.flint.data.hub.service.account.DatabaseUserService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DatabaseAuthenticationProvider implements AuthenticationProvider {

    private final DatabaseUserService databaseUserService;
    private final PasswordEncoder passwordEncoder;

    public DatabaseAuthenticationProvider(DatabaseUserService databaseUserService, PasswordEncoder passwordEncoder) {
        this.databaseUserService = databaseUserService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        DatabaseUser databaseUser = databaseUserService.user(authentication.getName());
        String presentedPassword = authentication.getCredentials().toString();
        if (null == presentedPassword || null == databaseUser.getPassword()) {
            throw new BadCredentialsException("password not activated");
        }
        if (!passwordEncoder.matches(presentedPassword, databaseUser.getPassword())) {
            throw new BadCredentialsException("password not matches");
        }
        return new UniversalAuthenticationToken(
                UniversalCredentials.builder()
                        .universalUserId(databaseUser.getUniversalUser().getId().toString())
                        .build(),
                databaseUser.getUniversalUser().getAuthorities()
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
