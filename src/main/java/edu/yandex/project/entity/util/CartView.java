package edu.yandex.project.entity.util;

import edu.yandex.project.controller.dto.ItemView;

import java.util.List;

public record CartView(List<ItemView> items, long totalPrice) {

    public static CartView createStub() {
        return new CartView(List.of(), 0L);
    }
}
