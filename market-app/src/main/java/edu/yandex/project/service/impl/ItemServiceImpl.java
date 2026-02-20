package edu.yandex.project.service.impl;

import edu.yandex.project.cache.ItemCache;
import edu.yandex.project.cache.ItemPageableRequestCache;
import edu.yandex.project.cache.util.ItemPage;
import edu.yandex.project.controller.dto.ItemListPageView;
import edu.yandex.project.controller.dto.ItemView;
import edu.yandex.project.controller.dto.ItemsPageableRequest;
import edu.yandex.project.domain.CartItem;
import edu.yandex.project.domain.Item;
import edu.yandex.project.exception.ItemNotFoundException;
import edu.yandex.project.factory.ItemListPageViewFactory;
import edu.yandex.project.mapper.ItemViewMapper;
import edu.yandex.project.repository.CartItemRepository;
import edu.yandex.project.repository.ItemPageableRepository;
import edu.yandex.project.repository.ItemRepository;
import edu.yandex.project.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final CartItemRepository cartItemRepository;
    private final ItemRepository itemRepository;
    private final ItemPageableRepository itemPageableRepository;

    private final ItemCache itemCache;
    private final ItemPageableRequestCache itemPageableRequestCache;

    private final ItemListPageViewFactory itemListPageViewFactory;

    private final ItemViewMapper itemViewMapper;

    @Override
    @Transactional
    public Mono<ItemListPageView> findAllAsView(@NonNull ItemsPageableRequest pageableRequest) {
        log.debug("ItemServiceImpl::findAllAsView {} in", pageableRequest);
        return itemPageableRequestCache.findByRequest(pageableRequest)
                .map(itemPage -> map(pageableRequest, itemPage))
                .switchIfEmpty(Mono.defer(() -> this.cacheAndGet(pageableRequest)))
                .flatMap(this::enrichWithCartCountAndMap)
                .map(page -> itemListPageViewFactory.create(page, pageableRequest))
                .doOnSuccess(itemListPageView ->
                        log.debug("ItemServiceImpl::findAllAsView {} out. Result: {}", pageableRequest, itemListPageView)
                );
    }

    @Override
    @Transactional
    public Mono<ItemView> findOneAsView(@NonNull Long itemId) {
        log.debug("ItemServiceImpl::findOneAsView {} in", itemId);
        return this.getCachedOrLoad(itemId)
                .zipWith(this.getInCartCount(itemId))
                .map(itemWithCount -> itemViewMapper.fromItemWithCount(itemWithCount.getT1(), itemWithCount.getT2()))
                .doOnSuccess(itemView ->
                        log.debug("ItemServiceImpl::findOneAsView {} out. Result: {}", itemId, itemView)
                );
    }

    @Override
    @Transactional
    public Mono<Item> findOne(@NonNull Long itemId) {
        log.debug("ItemServiceImpl::findOne {} in", itemId);
        return this.getCachedOrLoad(itemId)
                .doOnSuccess(found -> log.debug("ItemServiceImpl::findOne {} out. Result: {}", itemId, found));
    }

    @Override
    @Transactional
    public Flux<Item> findAll(@NonNull Collection<Long> itemIds) {
        log.debug("ItemServiceImpl::findAll {} in", itemIds);
        return this.getCachedOrLoad(itemIds)
                .doOnComplete(() -> log.debug("ItemServiceImpl::findAll {} out", itemIds));
    }

    private Flux<Item> getCachedOrLoad(Collection<Long> itemIds) {
        log.debug("ItemServiceImpl::getCachedOrLoad {} in", itemIds);
        if (itemIds.isEmpty()) {
            log.debug("ItemServiceImpl::getCachedOrLoad {}. Result: []", itemIds);
            return Flux.empty();
        }
        return itemCache.findAllById(itemIds)
                .flatMapMany(cachedItems -> {
                    var cachedFlux = Flux.fromIterable(cachedItems);

                    var notCachedItemIds = getDiff(itemIds, cachedItems);
                    if (!notCachedItemIds.isEmpty()) {
                        log.debug("ItemServiceImpl::getCachedOrLoad {}. Items will be loaded from db: {}",
                                itemIds, notCachedItemIds);
                        var newCachedFlux = itemRepository.findAllById(notCachedItemIds)
                                .flatMap(item -> itemCache.put(item).thenReturn(item));
                        return Flux.merge(cachedFlux, newCachedFlux);
                    }
                    return cachedFlux;
                });
    }

    private Mono<Item> getCachedOrLoad(long itemId) {
        return itemCache.findById(itemId)
                .switchIfEmpty(this.getFromDb(itemId)
                        .flatMap(itemFromDb -> itemCache.put(itemFromDb)
                                .thenReturn(itemFromDb))
                );
    }

    private Mono<Item> getFromDb(long itemId) {
        return itemRepository.findById(itemId)
                .switchIfEmpty(Mono.error(() -> {
                    log.error("ItemServiceImpl::getFromDb {} not found", itemId);
                    return new ItemNotFoundException(itemId);
                }));
    }

    private Mono<Long> getInCartCount(Long itemId) {
        log.debug("ItemServiceImpl::getInCartCount {} in", itemId);
        return cartItemRepository.findCartItemByItemId(itemId)
                .map(CartItem::getItemCount)
                .defaultIfEmpty(0L);
    }

    private Mono<Page<ItemView>> enrichWithCartCountAndMap(Page<Item> itemsPage) {
        return cartItemRepository.findAllByItems(itemsPage.getContent())
                .collectMap(cartItem -> cartItem.getId().itemId(), CartItem::getItemCount)
                .map(inCartCountMap -> itemsPage.getContent().stream()
                        .map(item -> itemViewMapper.fromItemWithCount(
                                item, inCartCountMap.getOrDefault(item.getId(), 0L)
                        ))
                        .toList()
                )
                .map(itemViews -> new PageImpl<>(itemViews, itemsPage.getPageable(), itemsPage.getTotalElements()));
    }

    private Mono<Page<Item>> cacheAndGet(ItemsPageableRequest pageableRequest) {
        return this.findAllAsPage(pageableRequest)
                .flatMap(toBeCached -> itemPageableRequestCache.put(pageableRequest, toBeCached)
                        .thenReturn(toBeCached));
    }

    private Mono<Page<Item>> findAllAsPage(ItemsPageableRequest pageableRequest) {
        return itemPageableRepository.findAll(
                pageableRequest.search(), pageableRequest.sort(), getPageable(pageableRequest)
        );
    }

    private static Set<Long> getDiff(Collection<Long> itemIds, Collection<Item> cachedItems) {
        var cachedItemIds = cachedItems.stream()
                .map(Item::getId)
                .collect(Collectors.toSet());
        return itemIds.stream()
                .filter(id -> !cachedItemIds.contains(id))
                .collect(Collectors.toSet());
    }

    private static Page<Item> map(ItemsPageableRequest pageableRequest, ItemPage itemPage) {
        return new PageImpl<>(itemPage.items(), getPageable(pageableRequest), itemPage.total());
    }

    private static PageRequest getPageable(ItemsPageableRequest pageableRequest) {
        return PageRequest.of(pageableRequest.pageNumber(), pageableRequest.pageSize());
    }
}
