package edu.yandex.project.controller;

import edu.yandex.project.controller.dto.CartActionRequest;
import edu.yandex.project.controller.dto.CartItemAction;
import edu.yandex.project.controller.dto.ItemListPageView;
import edu.yandex.project.controller.dto.ItemsPageableRequest;
import edu.yandex.project.controller.util.Views;
import edu.yandex.project.service.CartService;
import edu.yandex.project.service.ItemService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemWebController {

    private final CartService cartService;
    private final ItemService itemService;

    // showcase
    @GetMapping
    public Mono<Rendering> getItemsShowcase(@Valid @NotNull @ModelAttribute ItemsPageableRequest requestParameters) {
        log.info("ItemWebController::getItemsShowcase {} begins", requestParameters);
        return itemService.findAll(requestParameters)
                .map(itemListPageView -> Rendering
                        .view(Views.ITEMS.getName())
                        .modelAttribute(ItemListPageView.Fields.items, itemListPageView.items())
                        .modelAttribute(ItemListPageView.Fields.paging, itemListPageView.paging())
                        .modelAttribute(ItemListPageView.Fields.search, itemListPageView.search())
                        .modelAttribute(ItemListPageView.Fields.sort, itemListPageView.sort())
                        .status(HttpStatus.OK)
                        .build())
                .doOnSuccess(rendering ->
                        log.info("ItemWebController::getItemsShowcase {} ends. Result: {}", requestParameters, rendering)
                );
    }

    @PostMapping
    public Mono<Rendering> updateCartFromItemsShowcase(@Valid @NotNull @ModelAttribute CartItemAction cartItemAction,
                                                       @Valid @NotNull @ModelAttribute ItemsPageableRequest requestParameters) {
        log.info("ItemWebController::updateCartFromItemsShowcase {} begins", cartItemAction);
        return cartService.updateCart(cartItemAction)
                .thenReturn(Rendering
                        .redirectTo("/items?pageNumber={pageNumber}&pageSize={pageSize}&search={search}&sort={sort}")
                        .modelAttribute(ItemsPageableRequest.Fields.pageNumber, requestParameters.pageNumber())
                        .modelAttribute(ItemsPageableRequest.Fields.pageSize, requestParameters.pageSize())
                        .modelAttribute(ItemsPageableRequest.Fields.search, requestParameters.search())
                        .modelAttribute(ItemsPageableRequest.Fields.sort, requestParameters.sort())
                        .build())
                .doOnSuccess(rendering ->
                        log.info("ItemWebController::updateCartFromItemsShowcase {} ends. Redirecting -> /items",
                                cartItemAction)
                );
    }

    // item view
    @GetMapping("/{itemId}")
    public Mono<Rendering> getItemView(@PathVariable Long itemId) {
        log.info("ItemWebController::getItemView {} begins", itemId);
        return itemService.findOne(itemId)
                .map(itemView -> Rendering
                        .view(Views.ITEM.getName())
                        .modelAttribute("item", itemView)
                        .status(HttpStatus.OK)
                        .build())
                .doOnSuccess(rendering ->
                        log.info("ItemWebController::getItemView {} ends. Result: {}", itemId, rendering)
                );
    }

    @PostMapping("/{itemId}")
    public Mono<Rendering> updateCartFromItemView(@PathVariable Long itemId,
                                                  // Webflux не умеет напрямую распознвать enum в параметре запроса
                                                  @Valid @NotNull @ModelAttribute CartActionRequest cartActionRequest) {
        log.info("ItemWebController::updateCartFromItemView {}, {} begins", itemId, cartActionRequest);
        return cartService.updateCart(new CartItemAction(cartActionRequest.action(), itemId))
                .thenReturn(Rendering.redirectTo("/items/" + itemId).build())
                .doOnSuccess(rendering ->
                        log.info("ItemWebController::updateCartFromItemView {}, {} ends. Redirecting -> /items/{}",
                                itemId, cartActionRequest, itemId)
                );
    }
}
