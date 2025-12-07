package edu.yandex.project.service;

import edu.yandex.project.controller.dto.OrderView;
import org.springframework.lang.NonNull;

import java.util.List;

public interface OrderService {

    List<OrderView> findAll();

    Long create();

    OrderView findOrder(@NonNull Long orderId);
}
