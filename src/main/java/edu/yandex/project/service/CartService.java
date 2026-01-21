package edu.yandex.project.service;

import edu.yandex.project.controller.dto.CartItemAction;
import edu.yandex.project.controller.dto.CartView;
import edu.yandex.project.controller.dto.ItemsPageableRequest;
import edu.yandex.project.domain.Cart;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Mono;

public interface CartService {

    Mono<CartView> getCartContent();

    Mono<Void> updateCart(@NonNull CartItemAction cartItemAction);

    Mono<Void> updateCart(@NonNull CartItemAction cartItemAction, @NonNull ItemsPageableRequest requestParameters);

    Mono<Cart> getCart();

    Mono<Void> deleteCart();
}
