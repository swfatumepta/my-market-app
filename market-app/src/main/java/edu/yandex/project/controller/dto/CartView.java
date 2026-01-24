package edu.yandex.project.controller.dto;

import lombok.experimental.FieldNameConstants;

import java.util.List;

@FieldNameConstants
public record CartView(List<ItemView> items, long total) {

    public static CartView fromItemViews(List<ItemView> itemViews) {
        long totalPrice = itemViews.stream()
                .map(itemView -> itemView.price() * itemView.count())
                .reduce(0L, Long::sum);
        return new CartView(itemViews, totalPrice);
    }

    public static CartView createStub() {
        return new CartView(List.of(), 0L);
    }
}
