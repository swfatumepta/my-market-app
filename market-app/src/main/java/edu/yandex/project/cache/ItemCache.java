package edu.yandex.project.cache;

import edu.yandex.project.domain.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class ItemCache {
    private final static String CACHE_KEY_PREFIX = "item:";

    @Value("${spring.cache.item.ttl:PT2M}")
    private Duration cacheTTL;

    private final ReactiveRedisTemplate<String, Item> itemReactiveRedisTemplate;

    public Mono<Item> findById(@NonNull Long itemId) {
        log.debug("ItemCache::findById {} in", itemId);
        return itemReactiveRedisTemplate.opsForValue()
                .get(toCacheId(itemId))
                .doOnSuccess(fromCache -> log.debug("ItemCache::findById {} out. Result: {}", itemId, fromCache));
    }

    public Mono<Void> putOne(@NonNull Item toBeCached) {
        log.debug("ItemCache::putOne {} in", toBeCached.getId());
        return itemReactiveRedisTemplate.opsForValue()
                .set(toCacheId(toBeCached.getId()), toBeCached, cacheTTL)
                .doOnNext(success -> log.debug("ItemCache::putOne {}. Cached = {}", toBeCached.getId(), success))
                .then()
                .doOnSuccess(ignored -> log.debug("ItemCache::putOne {} out. Success", toBeCached.getId()));
    }

    private static String toCacheId(Long itemId) {
        return CACHE_KEY_PREFIX + itemId;
    }
}
