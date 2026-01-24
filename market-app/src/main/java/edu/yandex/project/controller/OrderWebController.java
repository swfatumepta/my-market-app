package edu.yandex.project.controller;

import edu.yandex.project.controller.util.Views;
import edu.yandex.project.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderWebController {

    private final OrderService orderService;

    @GetMapping
    public Mono<Rendering> getOrders() {
        log.info("OrderWebController::getOrders begins");
        return orderService.findAll()
                .map(orderViews -> Rendering
                        .view(Views.ORDERS.getName())
                        .modelAttribute("orders", orderViews)
                        .status(HttpStatus.OK)
                        .build())
                .doOnSuccess(rendering -> log.info("OrderWebController::getOrders ends. Result: {}", rendering));
    }

    @GetMapping("/{id}")
    public Mono<Rendering> getOrder(@PathVariable Long id, @RequestParam(required = false) boolean newOrder) {
        log.info("OrderWebController::getOrder {}, isNew = {} begins", id, newOrder);
        return orderService.findOne(id)
                .map(orderView -> Rendering
                        .view(Views.ORDER.getName())
                        .modelAttribute("order", orderView)
                        .modelAttribute("newOrder", newOrder)
                        .build())
                .doOnSuccess(rendering ->
                        log.info("OrderWebController::getOrder {}, isNew = {} ends. Result: {}", id, newOrder, rendering)
                );
    }

    @PostMapping("/place-an-order")
    public Mono<Rendering> placeAnOrder() {
        log.info("OrderWebController::placeAnOrder begins");
        return orderService.create()
                .map(createdOrderId -> Rendering
                        .redirectTo("/orders/{id}?newOrder=true")
                        .modelAttribute("id", createdOrderId)
                        .build())
                .doOnSuccess(rendering ->
                        log.info("OrderWebController::placeAnOrder ends. Redirecting -> {}", rendering)
                );
    }
}
