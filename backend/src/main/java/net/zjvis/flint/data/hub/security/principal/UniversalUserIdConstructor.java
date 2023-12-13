package net.zjvis.flint.data.hub.security.principal;

public enum UniversalUserIdConstructor {
    DING_TALK("ding-talk"),
    USER_PASSWORD("user-password"),
    ;
    private final String prefix;

    UniversalUserIdConstructor(String prefix) {
        this.prefix = prefix;
    }

    public String prefix() {
        return prefix;
    }

    public String userId(String originUserId) {
        return String.format("%s-%s", prefix, originUserId);
    }
}
