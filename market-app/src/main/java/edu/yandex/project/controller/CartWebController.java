package edu.yandex.project.controller;

import edu.yandex.project.controller.dto.CartItemAction;
import edu.yandex.project.controller.dto.CartView;
import edu.yandex.project.controller.util.Views;
import edu.yandex.project.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/cart/items")
@RequiredArgsConstructor
@Slf4j
public class CartWebController {

    private final CartService cartService;

    @GetMapping
    public Mono<Rendering> getCartItems() {
        log.info("CartWebController::getCartItems begins");
        return cartService.getCartContent()
                .map(cartView -> Rendering
                        .view(Views.CART.getName())
                        .modelAttribute(CartView.Fields.items, cartView.items())
                        .modelAttribute(CartView.Fields.total, cartView.total())
                        .status(HttpStatus.OK)
                        .build()
                )
                .doOnSuccess(rendering ->
                        log.info("CartWebController::getCartItems ends. Result: {}", rendering)
                );
    }

    @PostMapping
    public Mono<Rendering> updateCartFromCartView(@ModelAttribute CartItemAction cartItemAction) {
        log.info("CartWebController::updateCartFromCartView {} begins", cartItemAction);
        return cartService.updateCart(cartItemAction)
                .thenReturn(Rendering.redirectTo("/cart/items").build())
                .doOnSuccess(rendering ->
                        log.info("CartWebController::updateCartFromCartView {} ends. Redirecting -> /cart/items",
                                cartItemAction)
                );
    }
}
