package net.zjvis.flint.data.hub.service.account;

import lombok.Builder;
import lombok.Getter;
import org.apache.http.auth.Credentials;

import java.util.List;
import java.util.Optional;

public interface UserService<UserType, UniqueIdType> {
    @Getter
    class VerifyCredentialsResult<UserType> {
        private final boolean success;
        private final String message;
        private final UserType user;

        @Builder(toBuilder = true)
        public VerifyCredentialsResult(boolean success, String message, UserType user) {
            this.success = success;
            this.message = message;
            this.user = user;
        }
    }

    Optional<UserType> userOptional(UniqueIdType uniqueId);

    UserType user(UniqueIdType uniqueId);

    List<UserType> bindUserList(Long universalId);

    VerifyCredentialsResult<UserType> verify(Credentials credentials);

    UserType update(UserType user);

    void delete(UserType user);
}
