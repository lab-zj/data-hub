package net.zjvis.flint.data.hub.service.account;



import net.zjvis.flint.data.hub.entity.account.UniversalUser;
import net.zjvis.flint.data.hub.repository.UniversalUserRepository;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.http.auth.Credentials;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class UniversalUserService implements UserService<UniversalUser, Long> {
    private final UniversalUserRepository universalUserRepository;

    public UniversalUserService(UniversalUserRepository universalUserRepository) {
        this.universalUserRepository = universalUserRepository;
    }

    public List<UniversalUser> findAll() {
        return StreamSupport.stream(universalUserRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<UniversalUser> userOptional(Long universalId) {
        return universalUserRepository.findById(universalId);
    }

    @Override
    public UniversalUser user(Long universalId) {
        return userOptional(universalId)
                .orElseThrow(() -> new UsernameNotFoundException(
                        String.format("universalUser(id=%s) not found", universalId)));
    }

    @Override
    public List<UniversalUser> bindUserList(Long universalId) {
        Optional<UniversalUser> userOptional = userOptional(universalId);
        if (userOptional.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.singletonList(userOptional.get());
    }

    @Override
    public VerifyCredentialsResult<UniversalUser> verify(Credentials credentials) {
        throw new NotImplementedException("verification not support");
    }

    @Override
    public UniversalUser update(UniversalUser user) {
        return universalUserRepository.save(user);
    }

    @Override
    public void delete(UniversalUser user) {
        universalUserRepository.delete(user);
    }
}
