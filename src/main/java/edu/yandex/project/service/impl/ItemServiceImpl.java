package edu.yandex.project.service.impl;

import edu.yandex.project.controller.dto.ItemListPageView;
import edu.yandex.project.controller.dto.ItemView;
import edu.yandex.project.controller.dto.ItemsPageableRequest;
import edu.yandex.project.domain.CartItem;
import edu.yandex.project.exception.ItemNotFoundException;
import edu.yandex.project.factory.ItemListPageViewFactory;
import edu.yandex.project.mapper.ItemViewMapper;
import edu.yandex.project.repository.CartItemRepository;
import edu.yandex.project.repository.ItemPageableRepository;
import edu.yandex.project.repository.ItemRepository;
import edu.yandex.project.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final CartItemRepository cartItemRepository;
    private final ItemRepository itemRepository;
    private final ItemPageableRepository itemPageableRepository;

    private final ItemListPageViewFactory itemListPageViewFactory;
    private final ItemViewMapper itemViewMapper;

    @Override
    @Transactional(readOnly = true)
    public Mono<ItemListPageView> findAll(@NonNull ItemsPageableRequest pageableRequest) {
        log.debug("ItemServiceImpl::findAll {} in", pageableRequest);
        return itemPageableRepository.findAllWithCartCount(
                        pageableRequest.search(),
                        pageableRequest.sort().name(),
                        PageRequest.of(pageableRequest.pageNumber(), pageableRequest.pageSize())
                )
                .zipWith(Mono.just(pageableRequest))
                .map(itemListPageViewFactory::create)
                .doOnSuccess(itemListPageView ->
                        log.debug("ItemServiceImpl::findAll {} out. Result: {}", pageableRequest, itemListPageView)
                );
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<ItemView> findOne(@NonNull Long itemId) {
        log.debug("ItemServiceImpl::findOne {} in", itemId);
        return itemRepository.findById(itemId)
                .switchIfEmpty(Mono.error(() -> {
                    log.error("ItemServiceImpl::findOne {} not found", itemId);
                    return new ItemNotFoundException(itemId);
                }))
                .zipWith(this.getInCartCount(itemId))
                .map(itemViewMapper::fromTuple)
                .doOnSuccess(itemView ->
                        log.debug("ItemServiceImpl::findOne {} out. Result: {}", itemId, itemView)
                );
    }

    private Mono<Long> getInCartCount(Long itemId) {
        log.debug("ItemServiceImpl::getInCartCount {} in", itemId);
        return cartItemRepository.findCartItemByItemId(itemId)
                .map(CartItem::getItemCount)
                .defaultIfEmpty(0L);
    }
}
