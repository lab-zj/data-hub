package net.zjvis.flint.data.hub.service.account;


import net.zjvis.flint.data.hub.entity.account.DatabaseUser;
import net.zjvis.flint.data.hub.repository.DatabaseUserRepository;
import org.apache.http.auth.Credentials;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DatabaseUserService implements UserService<DatabaseUser, String> {
    public static final VerifyCredentialsResult<DatabaseUser> FAILED_RESULT = VerifyCredentialsResult.<DatabaseUser>builder()
            .success(false)
            .user(null)
            .message("failed")
            .build();
    private final DatabaseUserRepository databaseUserRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseUserService(
            DatabaseUserRepository databaseUserRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.databaseUserRepository = databaseUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Optional<DatabaseUser> userOptional(String username) {
        return databaseUserRepository.findByUsername(username);
    }

    @Override
    public DatabaseUser user(String username) {
        return userOptional(username)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("username(%s) not found", username)));
    }

    @Override
    public List<DatabaseUser> bindUserList(Long universalId) {
        return databaseUserRepository.findByUniversalUserId(universalId);
    }

    @Override
    public VerifyCredentialsResult<DatabaseUser> verify(Credentials credentials) {
        String username = credentials.getUserPrincipal().getName();
        if (null == username || username.isEmpty()) {
            return FAILED_RESULT.toBuilder()
                    .message(String.format("username(%s) is null or empty", username))
                    .build();
        }
        String password = credentials.getPassword();
        if (null == password) {
            return FAILED_RESULT.toBuilder()
                    .message("password is null")
                    .build();
        }
        DatabaseUser user;
        try {
            user = user(username);
        } catch (UsernameNotFoundException e) {
            return FAILED_RESULT.toBuilder()
                    .message(String.format("username(%s) not found", username))
                    .build();
        }
        if (null == user.getPassword()) {
            return FAILED_RESULT.toBuilder()
                    .message("password not activated")
                    .build();
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return FAILED_RESULT.toBuilder()
                    .message("password not matches")
                    .build();
        }
        if (!user.getUniversalUser().isEnabled()) {
            return FAILED_RESULT.toBuilder()
                    .message(String.format("user(%s) is not enabled", username))
                    .build();
        }
        return VerifyCredentialsResult.<DatabaseUser>builder()
                .success(true)
                .user(user)
                .message("")
                .build();
    }

    @Override
    public DatabaseUser update(DatabaseUser user) {
        return databaseUserRepository.save(user);
    }

    public DatabaseUser updateWithRawPassword(DatabaseUser user) {
        String rawPassword = user.getPassword();
        return update(user.toBuilder()
                .password(passwordEncoder.encode(rawPassword))
                .build());
    }

    @Override
    public void delete(DatabaseUser user) {
        databaseUserRepository.delete(user);
    }
}
