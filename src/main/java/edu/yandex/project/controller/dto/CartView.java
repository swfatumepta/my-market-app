package edu.yandex.project.controller.dto;

import lombok.experimental.FieldNameConstants;

import java.util.List;

@FieldNameConstants
public record CartView(List<ItemView> items, long total) {

    public static CartView createStub() {
        return new CartView(List.of(), 0L);
    }
}
