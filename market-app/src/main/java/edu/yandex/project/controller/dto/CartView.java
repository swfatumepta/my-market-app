package edu.yandex.project.controller.dto;

import lombok.experimental.FieldNameConstants;

import java.util.List;

@FieldNameConstants
public record CartView(List<ItemView> items, long total, boolean hasMoney) {

    public static CartView withHasMoney(CartView source, boolean hasMoney) {
        return new CartView(source.items(), source.total(), hasMoney);
    }

    public static CartView fromItemViews(List<ItemView> itemViews) {
        long totalPrice = itemViews.stream()
                .map(itemView -> itemView.price() * itemView.count())
                .reduce(0L, Long::sum);
        return new CartView(itemViews, totalPrice, false);
    }

    public static CartView createStub() {
        return new CartView(List.of(), 0L, false);
    }
}
