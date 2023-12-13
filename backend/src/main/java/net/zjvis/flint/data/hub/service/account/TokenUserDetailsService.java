package net.zjvis.flint.data.hub.service.account;


import net.zjvis.flint.data.hub.entity.account.UniversalUser;
import net.zjvis.flint.data.hub.repository.UniversalUserRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TokenUserDetailsService implements UserDetailsService {
    private final UniversalUserRepository universalUserRepository;

    public TokenUserDetailsService(UniversalUserRepository universalUserRepository) {
        this.universalUserRepository = universalUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UniversalUser> userOptional = universalUserRepository.findById(Long.parseLong(username));
        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException(username);
        }
        UniversalUser user = userOptional.get();
        return new User(
                String.valueOf(user.getId()),
                RandomStringUtils.randomAlphanumeric(16),
                user.isEnabled(),
                user.isAccountNonExpired(),
                true,
                user.isAccountNonLocked(),
                user.getAuthorities());
    }
}
