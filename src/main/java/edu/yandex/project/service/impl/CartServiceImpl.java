package edu.yandex.project.service.impl;

import edu.yandex.project.controller.dto.CartItemActionDto;
import edu.yandex.project.entity.CartEntity;
import edu.yandex.project.exception.GeneralProjectException;
import edu.yandex.project.repository.CartRepository;
import edu.yandex.project.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;

    @Override
    @Transactional
    public void updateCart(@NonNull CartItemActionDto cartItemActionDto) {
        log.debug("CartServiceImpl::updateCart {} in", cartItemActionDto);

        var cartEntity = this.getCart();

        log.debug("CartServiceImpl::updateCart {} out. Result: {}", cartItemActionDto, null);
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
                throw new GeneralProjectException("More than one cart found");
            }
            cartEntity = cartEntities.getFirst();
        }
        return cartEntity;
    }
}
