package edu.yandex.project.service;

import edu.yandex.project.controller.dto.OrderView;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Mono;

import java.util.List;

public interface OrderService {

    Mono<OrderView> findOne(@NonNull Long orderId);

    Mono<List<OrderView>> findAll();

    Mono<Long> create();
}
