package edu.yandex.project.service.impl;

import edu.yandex.project.controller.dto.CartItemActionDto;
import edu.yandex.project.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    @Override
    @Transactional
    public void updateCart(@NonNull CartItemActionDto cartItemActionDto) {
        log.debug("CartServiceImpl::updateCart {} in", cartItemActionDto);

        log.debug("CartServiceImpl::updateCart {} out. Result: {}", cartItemActionDto, null);
    }
}
