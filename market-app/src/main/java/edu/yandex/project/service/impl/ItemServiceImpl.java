package edu.yandex.project.service.impl;

import edu.yandex.project.cache.ItemCache;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final CartItemRepository cartItemRepository;
    private final ItemRepository itemRepository;
    private final ItemPageableRepository itemPageableRepository;

    private final ItemCache itemCache;

    private final ItemListPageViewFactory itemListPageViewFactory;

    private final ItemViewMapper itemViewMapper;

    @Override
    @Transactional
    public Mono<ItemListPageView> findAllAsView(@NonNull ItemsPageableRequest pageableRequest) {
        log.debug("ItemServiceImpl::findAllAsView {} in", pageableRequest);
        return itemPageableRepository.findAll(
                        pageableRequest.search(),
                        pageableRequest.sort(),
                        PageRequest.of(pageableRequest.pageNumber(), pageableRequest.pageSize())
                )
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
        return itemRepository.findAllById(itemIds);
    }

    private Mono<Item> getCachedOrLoad(long itemId) {
        return itemCache.findById(itemId)
                .switchIfEmpty(this.getFromDb(itemId)
                        .flatMap(itemFromDb -> itemCache.putOne(itemFromDb)
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
}
