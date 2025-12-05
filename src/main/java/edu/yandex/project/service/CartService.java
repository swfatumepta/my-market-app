package edu.yandex.project.service;

import edu.yandex.project.controller.dto.CartItemAction;
import org.springframework.lang.NonNull;

public interface CartService {

    void updateCart(@NonNull CartItemAction cartItemAction);
}
