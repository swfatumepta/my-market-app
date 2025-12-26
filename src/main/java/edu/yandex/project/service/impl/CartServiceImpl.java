package edu.yandex.project.service.impl;

import edu.yandex.project.controller.dto.CartItemAction;
import edu.yandex.project.controller.dto.CartView;
import edu.yandex.project.domain.Cart;
import edu.yandex.project.domain.CartItem;
import edu.yandex.project.domain.Item;
import edu.yandex.project.exception.GeneralProjectException;
import edu.yandex.project.exception.ItemNotFoundException;
import edu.yandex.project.mapper.ItemViewMapper;
import edu.yandex.project.repository.CartItemRepository;
import edu.yandex.project.repository.CartRepository;
import edu.yandex.project.repository.ItemRepository;
import edu.yandex.project.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ItemRepository itemRepository;

    private final ItemViewMapper itemViewMapper;

    @Override
    @Transactional
    public Mono<CartView> getCartContent() {
        log.debug("CartServiceImpl::getCartContent in");
        return this.getCart().flatMap(cart ->
                cartItemRepository.findAllByCartId(cart.getId())
                        .collectList()
                        .flatMap(this::joinItemAndMap)
                        .doOnSuccess(cartView -> log.debug("CartServiceImpl::getCartContent out. Result: {}", cartView))
        );
    }

    @Override
    @Transactional
    public Mono<Void> updateCart(@NonNull CartItemAction cartItemAction) {
        log.debug("CartServiceImpl::updateCart {} in", cartItemAction);
        return this.getCart().zipWith(this.getItem(cartItemAction))
                .flatMap(tuple -> cartItemRepository.findById(buildId(tuple))
                        .singleOptional()
                        .flatMap(cartItem ->
                                switch (cartItemAction.action()) {
                                    case PLUS -> this.addItem(cartItem, tuple.getT1(), tuple.getT2());
                                    case MINUS -> this.removeItem(cartItem, tuple.getT1(), tuple.getT2());
                                    case DELETE -> this.removeItems(cartItem, tuple.getT1(), tuple.getT2());
                                }))
                .then()
                .doOnSuccess(ignored -> log.debug("CartServiceImpl::updateCart {} out", cartItemAction));
    }

    private Mono<Void> removeItems(@NonNull Optional<CartItem> optionalCartItem, @NonNull Cart cart, @NonNull Item item) {
        log.debug("CartServiceImpl::removeItems {} in", optionalCartItem);
        return optionalCartItem.map(cartItem -> cartItemRepository.delete(cartItem)
                        .doOnSuccess(ignored -> log.debug("CartServiceImpl::removeItems {} out. Removed: {}", cartItem, cartItem))
                        .then())
                .orElse(Mono.error(() -> {
                    log.error("CartServiceImpl::removeItems Item.id = {} not found in Cart.id = {}", cart.getId(), item.getId());
                    return new GeneralProjectException("Impossible event! Check it ASAP!");
                }));
    }

    private Mono<Void> removeItem(@NonNull Optional<CartItem> optionalCartItem, @NonNull Cart cart, @NonNull Item item) {
        log.debug("CartServiceImpl::removeItem {} in", optionalCartItem);
        return optionalCartItem.map(cartItem -> {
                    cartItem.decrementCount();
                    if (cartItem.getItemCount() < 1) {
                        return cartItemRepository.delete(cartItem)
                                .doOnSuccess(ignored -> log.debug("CartServiceImpl::removeItem {} out. (removed): {}", cartItem, cartItem))
                                .then();
                    } else {
                        return cartItemRepository.upsert(cartItem)
                                .doOnSuccess(ignored ->
                                        log.debug("CartServiceImpl::removeItem {} out. (updated) CartItem.count = {}",
                                                cartItem, cartItem.getItemCount()))
                                .then();
                    }
                })
                .orElse(Mono.error(() -> {
                    log.error("CartServiceImpl::removeItem Item.id = {} not found in Cart.id = {}", cart.getId(), item.getId());
                    return new GeneralProjectException("Impossible event! Check it ASAP!");
                }));
    }

    private Mono<Void> addItem(@NonNull Optional<CartItem> optionalCartItem, @NonNull Cart cart, @NonNull Item item) {
        log.debug("CartServiceImpl::addItem {} in", optionalCartItem);
        CartItem toBeUpdated;
        if (optionalCartItem.isPresent()) {
            toBeUpdated = optionalCartItem.get();
            toBeUpdated.incrementCount();
        } else {
            toBeUpdated = CartItem.createNew(cart, item, 1L);
        }
        return cartItemRepository.upsert(toBeUpdated)
                .doOnSuccess(ignored -> log.debug("CartServiceImpl::addItem {} out", optionalCartItem))
                .then();
    }

    private Mono<Item> getItem(CartItemAction cartItemAction) {
        return itemRepository.findById(cartItemAction.itemId())
                .switchIfEmpty(Mono.error(() -> {
                    log.error("CartServiceImpl::updateCart {} not found", cartItemAction.itemId());
                    return new ItemNotFoundException(cartItemAction.itemId());
                }));
    }

    private Mono<CartView> joinItemAndMap(List<CartItem> cartItems) {
        log.debug("CartServiceImpl::joinItemAndMap {}", cartItems);
        if (cartItems.isEmpty()) {
            return Mono.just(CartView.createStub());
        } else {
            var itemIdToCountMap = cartItems.stream()
                    .collect(Collectors.toMap(cartItem -> cartItem.getId().itemId(), CartItem::getItemCount));
            return itemRepository.findAllById(itemIdToCountMap.keySet())
                    .map(item -> itemViewMapper.fromItemWithCount(item, itemIdToCountMap.get(item.getId())))
                    .collectList()
                    .map(CartView::fromItemViews);
        }
    }

    /**
     * В текущей версии приложения предполагается, что есть только одна глобальная корзина для покупок
     * @return {@link Cart}
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public Mono<Cart> getCart() {
        log.debug("CartServiceImpl::getCart");
        return cartRepository.findAll()
                .singleOrEmpty()
                .onErrorMap(IndexOutOfBoundsException.class, exc -> {
                    log.error("CartServiceImpl::getCart More than one cart found");
                    return new GeneralProjectException("More than one cart found");
                })
                .doOnNext(cart -> log.debug("CartServiceImpl::getCart found Cart.id = {}", cart.getId()))
                .switchIfEmpty(Mono.defer(() -> cartRepository.save(new Cart())
                        .doOnSuccess(saved -> log.debug("CartServiceImpl::getCart created Cart = {}", saved))));
    }

    @Override
    @Transactional
    public Mono<Void> deleteCart() {
        log.info("CartServiceImpl::deleteCart begins");
        return cartRepository.deleteAll()
                .doOnSuccess(ignored -> log.info("CartServiceImpl::deleteCart ends successful"));
    }

    private static CartItem.CartItemCompositeId buildId(@NonNull Tuple2<Cart, Item> tuple) {
        return CartItem.CartItemCompositeId.builder()
                .cartId(tuple.getT1().getId())
                .itemId(tuple.getT2().getId())
                .build();
    }
}
