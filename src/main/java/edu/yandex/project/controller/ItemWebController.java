package edu.yandex.project.controller;

import edu.yandex.project.controller.dto.CartItemAction;
import edu.yandex.project.controller.dto.ItemListPageView;
import edu.yandex.project.controller.dto.ItemsPageableRequest;
import edu.yandex.project.controller.dto.enums.CartAction;
import edu.yandex.project.service.CartService;
import edu.yandex.project.service.ItemService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemWebController {

    private final CartService cartService;
    private final ItemService itemService;

    // showcase
    @GetMapping
    public String getItemsShowcase(@Valid @NotNull @ModelAttribute ItemsPageableRequest requestParameters, Model model) {
        log.info("ItemWebController::getItemsShowcase {} begins", requestParameters);
        var itemListPageView = itemService.findAll(requestParameters);

        model.addAttribute(ItemListPageView.Fields.items, itemListPageView.items());
        model.addAttribute(ItemListPageView.Fields.paging, itemListPageView.paging());
        model.addAttribute(ItemListPageView.Fields.search, itemListPageView.search());
        model.addAttribute(ItemListPageView.Fields.sort, itemListPageView.sort());
        log.info("ItemWebController::getItemsShowcase {} ends. Result: {}", requestParameters, itemListPageView);
        return "items";
    }

    @PostMapping
    public String updateCartFromItemsShowcase(@Valid @NotNull @ModelAttribute CartItemAction cartItemAction,
                                              @Valid @NotNull @ModelAttribute ItemsPageableRequest requestParameters,
                                              RedirectAttributes redirectAttributes) {
        log.info("ItemWebController::updateCartFromItemsShowcase {} begins", cartItemAction);
        cartService.updateCart(cartItemAction);

        redirectAttributes.addAttribute(ItemsPageableRequest.Fields.pageNumber, requestParameters.pageNumber());
        redirectAttributes.addAttribute(ItemsPageableRequest.Fields.pageSize, requestParameters.pageSize());
        redirectAttributes.addAttribute(ItemsPageableRequest.Fields.search, requestParameters.search());
        redirectAttributes.addAttribute(ItemsPageableRequest.Fields.sort, requestParameters.sort());
        log.info("ItemWebController::updateCartFromItemsShowcase {} ends. Redirecting -> /items ...", cartItemAction);
        return "redirect:/items";
    }

    // item view
    @GetMapping("/{itemId}")
    public String getItemView(@PathVariable Long itemId, Model model) {
        log.info("ItemWebController::getItemView {} begins", itemId);
        var itemView = itemService.findOne(itemId);

        model.addAttribute("item", itemView);
        log.info("ItemWebController::getItemView {} ends. Result: {}", itemId, itemView);
        return "item";
    }

    @PostMapping("/{itemId}")
    public String updateCartFromItemView(@PathVariable Long itemId,
                                         @Valid @NotNull @RequestParam("action") CartAction cartItemAction) {
        log.info("ItemWebController::updateCartFromItemView {} begins", cartItemAction);
        cartService.updateCart(new CartItemAction(cartItemAction, itemId));
        log.info("ItemWebController::updateCartFromItemView {} ends. Redirecting -> /item/{} ...", itemId, cartItemAction);
        return "redirect:/items/" + itemId;
    }
}
