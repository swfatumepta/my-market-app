package edu.yandex.project.cache;

import edu.yandex.project.cache.util.CachedItemPage;
import edu.yandex.project.cache.util.ItemPage;
import edu.yandex.project.controller.dto.ItemsPageableRequest;
import edu.yandex.project.domain.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.MessageFormat;
import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class ItemPageableRequestCache {

    @Value("${spring.cache.item-page.key-pattern}")
    private String cacheKeyPattern;
    @Value("${spring.cache.item-page.name}")
    private String cacheKeyPrefix;
    @Value("${spring.cache.item-page.ttl}")
    private Duration cacheTTL;

    private final ReactiveRedisTemplate<String, CachedItemPage> itemIdListReactiveRedisTemplate;

    private final ItemCache itemCache;

    public Mono<ItemPage> findByRequest(@NonNull ItemsPageableRequest request) {
        log.debug("ItemPageableRequestCache::findByRequest {} in", request);
        return itemIdListReactiveRedisTemplate.opsForValue()
                .get(this.toItemCacheId(request))
                .flatMap(this::getItemsAndMap)
                .switchIfEmpty(Mono.empty())
                .doOnSuccess(itemPage ->
                        log.debug("ItemPageableRequestCache::findByRequest {} out. Result: {}", request, itemPage)
                );
    }

    public Mono<Void> put(@NonNull ItemsPageableRequest request, @NonNull Page<Item> itemPage) {
        log.debug("ItemPageableRequestCache::put {} in", request);
        return Flux.fromIterable(itemPage.getContent())
                .flatMap(item -> itemCache.put(item).thenReturn(item.getId()))
                .collectList()
                .map(itemIdList -> new CachedItemPage(itemIdList, itemPage.getTotalElements()))
                .flatMap(cachedItemIds -> this.mapAndCache(request, cachedItemIds))
                .then()
                .doOnSuccess(ignored -> log.debug("ItemPageableRequestCache::put {} out", request));
    }

    private Mono<Boolean> mapAndCache(ItemsPageableRequest request, CachedItemPage cachedItemIds) {
        return itemIdListReactiveRedisTemplate.opsForValue()
                .set(this.toItemCacheId(request), cachedItemIds, cacheTTL);
    }

    private Mono<ItemPage> getItemsAndMap(CachedItemPage cachedItemPage) {
        return itemCache.findAllById(cachedItemPage.itemIds())
                .map(itemsFromCache -> new ItemPage(itemsFromCache, cachedItemPage.total()))
                .doOnSuccess(itemPage -> log.debug("ItemPageableRequestCache::getItemsAndMap result: {}", itemPage));
    }

    private String toItemCacheId(ItemsPageableRequest request) {
        log.debug("ItemPageableRequestCache::toItemCacheId {} in", request);
        var key = MessageFormat.format(
                cacheKeyPrefix + cacheKeyPattern,
                request.search(), request.pageNumber(), request.pageSize(), request.sort()
        );
        log.debug("ItemPageableRequestCache::toItemCacheId {} out. Result: {}", request, key);
        return key;
    }
}
