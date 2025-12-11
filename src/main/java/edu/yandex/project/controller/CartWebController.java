package edu.yandex.project.controller;

import edu.yandex.project.controller.dto.CartItemAction;
import edu.yandex.project.controller.dto.CartView;
import edu.yandex.project.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/cart/items")
@RequiredArgsConstructor
@Slf4j
public class CartWebController {

    private final CartService cartService;

    @GetMapping
    public String getCartItems(Model model) {
        log.info("CartWebController::getCartItems begins");
        var cartView = cartService.getCartContent();

        model.addAttribute(CartView.Fields.items, cartView.items());
        model.addAttribute(CartView.Fields.total, cartView.total());
        log.info("CartWebController::getCartItems ends. Result: {}", cartView);
        return "cart";
    }

    @PostMapping
    public String updateCartFromCartView(@ModelAttribute CartItemAction cartItemAction) {
        log.info("CartWebController::updateCartFromCartView {} begins", cartItemAction);
        cartService.updateCart(cartItemAction);
        log.info("CartWebController::updateCartFromCartView {} ends. Redirecting -> /cart/items", cartItemAction);
        return "redirect:/cart/items";
    }
}
