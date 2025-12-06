package edu.yandex.project.controller.dto;

import java.util.List;

public record CartView(List<ItemView> items, long totalPrice) {

    public static CartView createStub() {
        return new CartView(List.of(), 0L);
    }
}
