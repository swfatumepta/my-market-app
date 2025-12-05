package edu.yandex.project.controller;

import edu.yandex.project.controller.dto.CartItemAction;
import edu.yandex.project.controller.dto.ItemListPageView;
import edu.yandex.project.controller.dto.ItemsPageableRequest;
import edu.yandex.project.service.CartService;
import edu.yandex.project.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/items")

@RequiredArgsConstructor
@Slf4j
public class ItemWebController {

    private final CartService cartService;
    private final ItemService itemService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public String getItems(@ModelAttribute ItemsPageableRequest requestParameters, Model model) {
        log.info("ItemWebController::getItems {} begins", requestParameters);
        var itemListPageView = itemService.findAll(requestParameters);

        this.putSearchAttributes(itemListPageView, model);
        log.info("ItemWebController::getItems {} ends. Result: {}", requestParameters, model);
        return "items";
    }

    @GetMapping("/{itemId}")
    @ResponseStatus(HttpStatus.OK)
    public String getItem(@PathVariable Long itemId, Model model) {
        log.info("ItemWebController::getItem {} begins", itemId);
        var itemView = itemService.findOne(itemId);

        model.addAttribute("item", itemView);
        log.info("ItemWebController::getItem {} ends. Result: {}", itemId, model);
        return "item";
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public String updateCart(@ModelAttribute CartItemAction cartItemAction,
                             @ModelAttribute ItemsPageableRequest requestParameters,
                             Model model) {
        log.info("ItemWebController::updateCart {} begins", cartItemAction);
        cartService.updateCart(cartItemAction);

        var itemListPageView = itemService.findAll(requestParameters);
        this.putSearchAttributes(itemListPageView, model);
        log.info("ItemWebController::updateCart {} ends. Result: {}", cartItemAction, model);
        return "items";
    }

    private void putSearchAttributes(ItemListPageView itemListPageView, Model model) {
        model.addAttribute("items", itemListPageView.items());
        model.addAttribute("paging", itemListPageView.pageInfo());
        model.addAttribute("search", itemListPageView.search());
        model.addAttribute("sort", itemListPageView.sort());
    }
}
