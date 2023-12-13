package net.zjvis.flint.data.hub.lib.minio;

import lombok.Getter;

public enum MinioTagKey {
    LAST_MODIFIED("lastModified"),
    SIZE("size"),
    CREATE_TIME("createTime"),
    ;
    @Getter
    private final String key;

    MinioTagKey(String key) {
        this.key = key;
    }
}
