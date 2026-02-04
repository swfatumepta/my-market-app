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
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ItemCache {

    @Value("${spring.cache.item.name}")
    private String cacheKeyPrefix;
    @Value("${spring.cache.item.ttl}")
    private Duration cacheTTL;

    private final ReactiveRedisTemplate<String, Item> itemReactiveRedisTemplate;

    public Mono<Item> findById(@NonNull Long itemId) {
        log.debug("ItemCache::findById {} in", itemId);
        return itemReactiveRedisTemplate.opsForValue()
                .get(toItemCacheId(itemId))
                .doOnSuccess(fromCache -> log.debug("ItemCache::findById {} out. Result: {}", itemId, fromCache));
    }

    public Mono<List<Item>> findAllById(@NonNull Collection<Long> itemIds) {
        if (itemIds.isEmpty()) {
            log.debug("ItemCacheService::findAllById {} out. Result: []", itemIds);
            return Mono.just(List.of());
        }
        return itemReactiveRedisTemplate.opsForValue()
                .multiGet(this.toItemCacheIds(itemIds))
                .map(items -> items.stream()
                        .filter(Objects::nonNull)
                        .toList())
                .doOnSuccess(result ->
                        log.debug("ItemCacheService::findAllById {} out. Result (count): {}", itemIds, result.size())
                );
    }

    public Mono<Void> put(@NonNull Item toBeCached) {
        log.debug("ItemCache::put {} in", toBeCached.getId());
        return itemReactiveRedisTemplate.opsForValue()
                .set(toItemCacheId(toBeCached.getId()), toBeCached, cacheTTL)
                .doOnNext(success -> log.debug("ItemCache::put {}. Cached = {}", toBeCached.getId(), success))
                .then()
                .doOnSuccess(ignored -> log.debug("ItemCache::put {} out. Success", toBeCached.getId()));
    }

    private Set<String> toItemCacheIds(Collection<Long> itemIds) {
        return itemIds.stream()
                .map(this::toItemCacheId)
                .collect(Collectors.toSet());
    }

    private String toItemCacheId(Long itemId) {
        return cacheKeyPrefix + itemId;
    }
}
