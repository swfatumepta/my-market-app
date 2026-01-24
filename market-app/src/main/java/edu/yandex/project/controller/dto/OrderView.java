package edu.yandex.project.controller.dto;

import java.util.List;

public record OrderView(long id, long totalSum, List<OrderItemView> items) {
}
