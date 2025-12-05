package edu.yandex.project.controller.dto;

import edu.yandex.project.controller.dto.enums.CartAction;

import java.beans.ConstructorProperties;

public record CartItemActionDto(CartAction action,
                                Long itemId) {

    @ConstructorProperties({"action", "id"})
    public CartItemActionDto {
    }
}
