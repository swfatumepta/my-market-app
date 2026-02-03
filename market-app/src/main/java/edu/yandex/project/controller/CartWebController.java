package edu.yandex.project.controller;

import edu.yandex.project.controller.dto.CartItemAction;
import edu.yandex.project.controller.dto.CartView;
import edu.yandex.project.controller.util.Views;
import edu.yandex.project.service.CartService;
import edu.yandex.project.integration.WalletService;
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
    private final static String HAS_ENOUGH_MONEY_PARAMETER = "HAS_ENOUGH_MONEY";

    private final CartService cartService;
    private final WalletService walletService;

    @GetMapping
    public Mono<Rendering> getCartItems() {
        log.info("CartWebController::getCartItems begins");
        return cartService.getCartContent()
                .zipWith(walletService.getBalance())
                .map(tuple -> Rendering
                        .view(Views.CART.getName())
                        .modelAttribute(CartView.Fields.items, tuple.getT1().items())
                        .modelAttribute(CartView.Fields.total, tuple.getT1().total())
                        // время сильно поджимает, потому так
                        // (по хорошему, надо делать это в CartService и класть в дто, но так  норм в контексте обучения)
                        .modelAttribute(HAS_ENOUGH_MONEY_PARAMETER, tuple.getT1().total() <= tuple.getT2())
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
