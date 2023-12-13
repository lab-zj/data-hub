package net.zjvis.flint.data.hub.lib.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.Builder;
import org.springframework.lang.NonNull;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class CacheManager<KeyType, ValueType> {
    private final LoadingCache<KeyType, ValueType> cache;

    @Builder
    public CacheManager(
            Long timeoutSeconds,
            Integer maximumSize,
            Function<KeyType, ValueType> loader
    ) {
        if (null == timeoutSeconds) {
            timeoutSeconds = 1800L;
        }
        if (null == maximumSize) {
            maximumSize = 100;
        }
        cache = CacheBuilder.newBuilder()
                .expireAfterWrite(timeoutSeconds, TimeUnit.SECONDS)
                .maximumSize(maximumSize)
                .build(new CacheLoader<>() {
                    @Override
                    public ValueType load(@NonNull KeyType key) throws Exception {
                        try {
                            return loader.apply(key);
                        } catch (Exception e) {
                            throw new Exception(String.format("load key(%s) failed", key), e);
                        }
                    }
                });
    }

    public ValueType get(KeyType key) throws ExecutionException {
        return cache.get(key);
    }

    public ValueType getUnchecked(KeyType key) {
        return cache.getUnchecked(key);
    }

    public void refresh(KeyType key) {
        cache.refresh(key);
    }

    public void put(KeyType key, ValueType value) {
        cache.put(key, value);
    }

    public void invalidate(KeyType key) {
        cache.invalidate(key);
    }

    public void invalidateAll(Iterable<KeyType> keys) {
        cache.invalidateAll(keys);
    }

    public void invalidateAll() {
        cache.invalidateAll();
    }

    public Map<KeyType, ValueType> asMap() {
        return cache.asMap();
    }
}
