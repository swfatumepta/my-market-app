package edu.yandex.project.controller.dto;

import edu.yandex.project.controller.dto.enums.CartAction;

import java.beans.ConstructorProperties;

public record CartItemActionDto(CartAction action, Long itemId) {

    // workaround для правильного парсинга полей, если они не соовтетствуют названиям формы - @RequestParam не работает в этом случае
    @ConstructorProperties({"action", "id"})
    public CartItemActionDto {
    }
}
