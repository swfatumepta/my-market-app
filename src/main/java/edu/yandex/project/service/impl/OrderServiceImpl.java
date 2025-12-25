package edu.yandex.project.service.impl;

import edu.yandex.project.controller.dto.OrderView;
import edu.yandex.project.domain.Order;
import edu.yandex.project.domain.OrderItem;
import edu.yandex.project.exception.GeneralProjectException;
import edu.yandex.project.exception.OrderNotFoundException;
import edu.yandex.project.mapper.OrderItemViewMapper;
import edu.yandex.project.repository.OrderItemRepository;
import edu.yandex.project.repository.OrderRepository;
import edu.yandex.project.service.CartService;
import edu.yandex.project.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    private final CartService cartService;

    private final OrderItemViewMapper orderItemViewMapper;

    @Override
    @Transactional(readOnly = true)
    public Mono<OrderView> findOne(@NonNull Long orderId) {
        log.debug("OrderServiceImpl::findOne {} in", orderId);
        return orderRepository.findById(orderId)
                .switchIfEmpty(Mono.error(() -> {
                    log.error("OrderServiceImpl::findOne {} not found", orderId);
                    return new OrderNotFoundException(orderId);
                }))
                .flatMap(this::joinOrderItemAndMap)
                .doOnSuccess(orderView -> log.debug("OrderServiceImpl::findOne {} out. Result: {}", orderId, orderView));
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<List<OrderView>> findAll() {
        log.debug("OrderServiceImpl::findAll in");
        return orderItemRepository.findAll()
                .collectList()
                .flatMap(this::joinOrderAndMap)
                .doOnSuccess(orderViews -> log.debug("OrderServiceImpl::findAll out. Result: {}", orderViews));
    }

    @Override
    @Transactional
    public Mono<Long> create() {
        log.debug("OrderServiceImpl::create in");
        // get current cart state
        return cartService.getCartContent()
                .filter(cartView -> !cartView.items().isEmpty())
                .switchIfEmpty(Mono.error(() -> {
                    log.error("OrderServiceImpl::create Cart is empty");
                    return new GeneralProjectException("Cart is empty");
                }))
                .flatMap(cartView -> {
                    // build order items (needed to compute order)
                    var orderItems = cartView.items().stream()
                            .map(OrderItem::fromItemViewWithEmptyOrder)
                            .toList();
                    // create and save new order
                    return orderRepository.save(Order.createNew(orderItems))
                            .flatMap(order -> {
                                // enrich order_items with required order.id
                                orderItems.forEach(orderItem -> orderItem.setOrderId(order.getId()));
                                // save order_items
                                return orderItemRepository.saveAll(orderItems)
                                        .then(cartService.deleteCart())
                                        .thenReturn(order.getId());
                            });
                })
                .doOnSuccess(createdOrderId -> log.debug("OrderServiceImpl::create out. Result: {}", createdOrderId));
    }

    private Mono<List<OrderView>> joinOrderAndMap(List<OrderItem> orderItems) {
        log.debug("OrderServiceImpl::joinOrderAndMap {}", orderItems);
        var orderIdToOrderItemView = orderItems.stream()
                .collect(Collectors.groupingBy(
                        OrderItem::getOrderId, Collectors.mapping(orderItemViewMapper::from, Collectors.toList()))
                );
        return orderRepository.findAllById(orderIdToOrderItemView.keySet())
                .map(order -> new OrderView(order.getId(), order.getTotalCost(), orderIdToOrderItemView.get(order.getId())))
                .collectList();
    }

    private Mono<OrderView> joinOrderItemAndMap(Order order) {
        log.debug("OrderServiceImpl::joinOrderItemAndMap {}", order);
        return orderItemRepository.findAllByOrderId(order.getId())
                .collectList()
                .map(orderItemViewMapper::from)
                .map(itemViews -> new OrderView(order.getId(), order.getTotalCost(), itemViews));
    }
}
