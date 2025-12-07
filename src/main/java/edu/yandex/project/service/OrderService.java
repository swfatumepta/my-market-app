package edu.yandex.project.service;

import edu.yandex.project.entity.util.OrderView;

import java.util.List;

public interface OrderService {

    List<OrderView> findAll();

    Long create();
}
