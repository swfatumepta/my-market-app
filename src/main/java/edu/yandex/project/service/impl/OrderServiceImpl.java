package edu.yandex.project.service.impl;

import edu.yandex.project.entity.OrderEntity;
import edu.yandex.project.controller.dto.OrderView;
import edu.yandex.project.exception.GeneralProjectException;
import edu.yandex.project.exception.OrderNotFoundException;
import edu.yandex.project.mapper.OrderItemViewMapper;
import edu.yandex.project.repository.CartRepository;
import edu.yandex.project.repository.OrderRepository;
import edu.yandex.project.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;

    private final OrderItemViewMapper orderItemViewMapper;

    @Override
    @Transactional(readOnly = true)
    public List<OrderView> findAll() {
        log.debug("OrderServiceImpl::findAll in");
        var orderViews = orderRepository.findAllWithItems().stream()
                .map(orderEntity -> {
                    var orderItemViews = orderItemViewMapper.from(orderEntity.getItems());
                    return new OrderView(orderEntity.getId(), orderEntity.getTotalCost(), orderItemViews);
                })
                .toList();
        log.debug("OrderServiceImpl::findAll out. Result: {}", orderViews);
        return orderViews;
    }

    @Override
    @Transactional(readOnly = true)
    public OrderView findOne(@NonNull Long orderId) {
        log.debug("OrderServiceImpl::findOne {} in", orderId);
        var orderEntity = orderRepository.findWithItemsById(orderId)
                .orElseThrow(() -> {
                    log.error("OrderServiceImpl::findOne ItemEntity.id = {} not found", orderId);
                    return new OrderNotFoundException(orderId);
                });
        var orderItemViews = orderItemViewMapper.from(orderEntity.getItems());
        var orderView = new OrderView(orderEntity.getId(), orderEntity.getTotalCost(), orderItemViews);
        log.debug("OrderServiceImpl::findOne {} out. Result: {}", orderId, "");
        return orderView;
    }

    @Override
    @Transactional
    public Long create() {
        log.debug("OrderServiceImpl::create in");
        var currentCartEntity = cartRepository.findFirstByIdIsNotNull()
                .orElseThrow(() -> {
                    log.error("OrderServiceImpl::create there are no cart found in database");
                    return new GeneralProjectException("There are no cart found in database!");
                });
        var orderEntity = OrderEntity.createNew(currentCartEntity);
        orderRepository.save(orderEntity);

        cartRepository.delete(currentCartEntity);
        log.debug("OrderServiceImpl::create out. Result: {}", "");
        return orderEntity.getId();
    }
}
