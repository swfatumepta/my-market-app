package edu.yandex.project.controller;

import edu.yandex.project.controller.dto.CartItemAction;
import edu.yandex.project.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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

        model.addAttribute("items", cartView.items());
        model.addAttribute("total", cartView.totalPrice());
        log.info("CartWebController::getCartItems ends. Result: {}", model);
        return "cart";
    }

    @PostMapping
    public String updateCartFromCartView(@ModelAttribute CartItemAction cartItemAction, Model model) {
        log.info("ItemWebController::updateCartFromCartView {} begins", cartItemAction);
        cartService.updateCart(cartItemAction);
        log.info("ItemWebController::updateCartFromCartView {} ends. Going to call ItemWebController::getItemView..",
                cartItemAction);
        return this.getCartItems(model);
    }
}
