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

    // showcase
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public String getItemsShowcase(@ModelAttribute ItemsPageableRequest requestParameters, Model model) {
        log.info("ItemWebController::getItemsShowcase {} begins", requestParameters);
        var itemListPageView = itemService.findAll(requestParameters);

        this.fillModel(itemListPageView, model);
        log.info("ItemWebController::getItemsShowcase {} ends. Result: {}", requestParameters, model);
        return "items";
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public String updateCartFromItemsShowcase(@ModelAttribute CartItemAction cartItemAction,
                                              @ModelAttribute ItemsPageableRequest requestParameters,
                                              Model model) {
        log.info("ItemWebController::updateCartFromItemsShowcase {} begins", cartItemAction);
        cartService.updateCart(cartItemAction);
        log.info("ItemWebController::updateCartFromItemsShowcase {} ends. Going to call ItemWebController::getItemsShowcase..",
                cartItemAction);
        return this.getItemsShowcase(requestParameters, model);
    }

    // item view
    @GetMapping("/{itemId}")
    @ResponseStatus(HttpStatus.OK)
    public String getItemView(@PathVariable Long itemId, Model model) {
        log.info("ItemWebController::getItemView {} begins", itemId);
        var itemView = itemService.findOne(itemId);

        model.addAttribute("item", itemView);
        log.info("ItemWebController::getItemView {} ends. Result: {}", itemId, model);
        return "item";
    }

    private void fillModel(ItemListPageView itemListPageView, Model model) {
        model.addAttribute("items", itemListPageView.items());
        model.addAttribute("paging", itemListPageView.pageInfo());
        model.addAttribute("search", itemListPageView.search());
        model.addAttribute("sort", itemListPageView.sort());
    }
}
