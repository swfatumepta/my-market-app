package edu.yandex.project.service;

import edu.yandex.project.controller.dto.CartItemActionDto;
import org.springframework.lang.NonNull;

public interface CartService {

    void updateCart(@NonNull CartItemActionDto cartItemActionDto);
}
