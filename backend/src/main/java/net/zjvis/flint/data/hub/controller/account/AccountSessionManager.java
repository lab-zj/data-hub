package net.zjvis.flint.data.hub.controller.account;

import com.google.common.base.Preconditions;
import net.zjvis.flint.data.hub.entity.account.DatabaseUser;
import net.zjvis.flint.data.hub.entity.account.DingTalkUser;
import net.zjvis.flint.data.hub.entity.account.UniversalUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
public class AccountSessionManager {
    protected enum SessionKey {
        DING_TALK_USER_VERIFIED,
        DATABASE_USER_VERIFIED,
        DING_TALK_USER_VERIFIED_UPDATE_TIME,
        DATABASE_USER_VERIFIED_UPDATE_TIME,
    }

    private final Long timeout;
    private final TimeUnit timoutTimeUnit;

    public AccountSessionManager(
            @Value("${application.session.account.timeout.time:5}") Long timeout,
            @Value("${application.session.account.timeout.unit:MINUTES}") TimeUnit timoutTimeUnit
    ) {
        this.timeout = timeout;
        this.timoutTimeUnit = timoutTimeUnit;
    }

    @Nullable
    public UniversalUser universalUserVerified(HttpSession httpSession) {
        return Optional.ofNullable(dingTalkUserVerified(httpSession))
                .map(DingTalkUser::getUniversalUser)
                .orElseGet(() -> Optional.ofNullable(databaseUserVerified(httpSession))
                        .map(DatabaseUser::getUniversalUser)
                        .orElse(null));
    }

    @Nullable
    public DingTalkUser dingTalkUserVerified(HttpSession httpSession) {
        Long updateTime = (Long) httpSession.getAttribute(SessionKey.DING_TALK_USER_VERIFIED_UPDATE_TIME.name());
        if (updateTime == null || System.currentTimeMillis() - updateTime > timoutTimeUnit.toMillis(timeout)) {
            return null;
        }
        Object attribute = httpSession.getAttribute(SessionKey.DING_TALK_USER_VERIFIED.name());
        if (null == attribute) {
            return null;
        }
        Preconditions.checkArgument(
                attribute instanceof DingTalkUser,
                "attribute class(%s) is not %s",
                attribute.getClass().getName(), DingTalkUser.class.getName()
        );
        return (DingTalkUser) attribute;
    }

    public void setDingTalkUserVerified(HttpSession httpSession, DingTalkUser dingTalkUser) {
        httpSession.setAttribute(SessionKey.DING_TALK_USER_VERIFIED.name(), dingTalkUser);
        httpSession.setAttribute(SessionKey.DING_TALK_USER_VERIFIED_UPDATE_TIME.name(), System.currentTimeMillis());
    }

    @Nullable
    public DatabaseUser databaseUserVerified(HttpSession httpSession) {
        Long updateTime = (Long) httpSession.getAttribute(SessionKey.DATABASE_USER_VERIFIED_UPDATE_TIME.name());
        if (updateTime == null || System.currentTimeMillis() - updateTime > timoutTimeUnit.toMillis(timeout)) {
            return null;
        }
        Object attribute = httpSession.getAttribute(SessionKey.DATABASE_USER_VERIFIED.name());
        if (null == attribute) {
            return null;
        }
        Preconditions.checkArgument(
                attribute instanceof DatabaseUser,
                "attribute class(%s) is not %s",
                attribute.getClass().getName(), DatabaseUser.class.getName()
        );
        return (DatabaseUser) attribute;
    }

    public void setDatabaseUserVerified(HttpSession httpSession, DatabaseUser databaseUser) {
        httpSession.setAttribute(SessionKey.DATABASE_USER_VERIFIED.name(), databaseUser);
        httpSession.setAttribute(SessionKey.DATABASE_USER_VERIFIED_UPDATE_TIME.name(), System.currentTimeMillis());
    }

    public void removeAllVerifiedFlags(HttpSession httpSession) {
        setDatabaseUserVerified(httpSession, null);
        setDingTalkUserVerified(httpSession, null);
    }
}
