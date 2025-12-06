package edu.yandex.project.controller.dto;

import java.util.List;

public record CartView(List<ItemView> items, int totalPrice) {

    public static CartView createStub() {
        return new CartView(List.of(), 0);
    }
}
