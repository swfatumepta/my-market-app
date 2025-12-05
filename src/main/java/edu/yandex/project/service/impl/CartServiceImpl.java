package edu.yandex.project.service.impl;

import edu.yandex.project.controller.dto.CartItemAction;
import edu.yandex.project.entity.CartEntity;
import edu.yandex.project.entity.CartItemEntity;
import edu.yandex.project.entity.ItemEntity;
import edu.yandex.project.exception.CartItemNotFoundException;
import edu.yandex.project.exception.GeneralProjectException;
import edu.yandex.project.exception.ItemNotFoundException;
import edu.yandex.project.repository.CartItemRepository;
import edu.yandex.project.repository.CartRepository;
import edu.yandex.project.repository.ItemRepository;
import edu.yandex.project.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public void updateCart(@NonNull CartItemAction cartItemAction) {
        log.debug("CartServiceImpl::updateCart {} in", cartItemAction);
        var cartEntity = this.getCart();
        var itemEntity = itemRepository.findById(cartItemAction.itemId())
                .orElseThrow(() -> {
                    log.error("CartServiceImpl::updateCart ItemEntity.id = {} not found", cartItemAction.itemId());
                    return new ItemNotFoundException(cartItemAction.itemId());
                });
        var cartItemId = new CartItemEntity.CartItemCompositeId(cartEntity.getId(), cartItemAction.itemId());
        var optionalCartItemEntity = cartItemRepository.findById(cartItemId);
        switch (cartItemAction.action()) {
            case PLUS -> this.addItem(optionalCartItemEntity, cartEntity, itemEntity);
            case MINUS -> this.removeItem(optionalCartItemEntity, cartEntity, itemEntity);
        }
        log.debug("CartServiceImpl::updateCart {} out", cartItemAction);
    }

    private void addItem(@NonNull Optional<CartItemEntity> optionalCartItemEntity,
                         @NonNull CartEntity cartEntity,
                         @NonNull ItemEntity itemEntity) {
        log.debug("CartServiceImpl::addItem {} in", optionalCartItemEntity.orElse(null));
        CartItemEntity toBeUpdated;
        if (optionalCartItemEntity.isPresent()) {
            toBeUpdated = optionalCartItemEntity.get();
            toBeUpdated.incrementCount();
        } else {
            toBeUpdated = CartItemEntity.createNew(cartEntity, itemEntity, 1L);
        }
        cartItemRepository.save(toBeUpdated);
        log.debug("CartServiceImpl::addItem {} out. Added: {}", optionalCartItemEntity.orElse(null), toBeUpdated);
    }

    private void removeItem(@NonNull Optional<CartItemEntity> optionalCartItemEntity,
                            @NonNull CartEntity cartEntity,
                            @NonNull ItemEntity itemEntity) {
        log.debug("CartServiceImpl::removeItem {} in", optionalCartItemEntity.orElse(null));
        optionalCartItemEntity.ifPresentOrElse(
                cartItemEntity -> {
                    cartItemEntity.decrementCount();
                    if (cartItemEntity.getItemCount() < 1) {
                        cartItemRepository.delete(cartItemEntity);
                        log.debug("CartServiceImpl::removeItem {} out. Removed: {}", cartItemEntity, cartItemEntity);
                    } else {
                        cartItemRepository.save(cartItemEntity);
                        log.debug("CartServiceImpl::removeItem {} out. (updated) CartItemEntity.count = {}",
                                cartItemEntity, cartItemEntity.getItemCount());
                    }
                },
                () -> {
                    log.error("CartServiceImpl::updateCart ItemEntity.id = {} not found in CartEntity.id = {}",
                            cartEntity.getId(), itemEntity.getId());
                    throw new CartItemNotFoundException(cartEntity.getId(), itemEntity.getId());
                }
        );
    }

    /**
     * В текущей версии приложения предполагается, что есть только одна глобальная корзина для покупок
     * @return {@link CartEntity}
     */
    private CartEntity getCart() {
        CartEntity cartEntity;
        var cartEntities = cartRepository.findAll();
        if (cartEntities.isEmpty()) {
            cartEntity = cartRepository.save(new CartEntity());
        } else {
            if (cartEntities.size() > 1) {
                log.error("CartServiceImpl::getCart More than one cart found");
                throw new GeneralProjectException("More than one cart found");
            }
            cartEntity = cartEntities.getFirst();
        }
        return cartEntity;
    }
}
