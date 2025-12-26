package edu.yandex.project.controller.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Views {
    CART("cart"),

    ITEM("item"),
    ITEMS("items"),

    ORDER("order"),
    ORDERS("orders");

    private final String name;
}
