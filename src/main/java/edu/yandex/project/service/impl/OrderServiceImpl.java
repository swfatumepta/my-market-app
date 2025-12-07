package edu.yandex.project.service.impl;

import edu.yandex.project.entity.OrderEntity;
import edu.yandex.project.entity.util.OrderView;
import edu.yandex.project.exception.GeneralProjectException;
import edu.yandex.project.repository.CartRepository;
import edu.yandex.project.repository.OrderRepository;
import edu.yandex.project.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional(readOnly = true)
    public List<OrderView> findAll() {
        log.debug("OrderServiceImpl::findAll in");
        log.debug("OrderServiceImpl::findAll out. Result: {}", "");
        return null;
    }

    @Override
    @Transactional
    public Long create() {
        log.debug("OrderServiceImpl::create in");
        var currentCartEntity = cartRepository.findFirstByIdIsNotNull().orElseThrow(() -> {
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
