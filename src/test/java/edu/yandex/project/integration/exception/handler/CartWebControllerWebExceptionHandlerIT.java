package edu.yandex.project.integration.exception.handler;

import edu.yandex.project.controller.dto.CartItemAction;
import edu.yandex.project.controller.dto.enums.CartAction;
import edu.yandex.project.entity.CartEntity;
import edu.yandex.project.entity.CartItemEntity;
import edu.yandex.project.entity.ItemEntity;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@Tag("CartWebControllerExceptionHandlerIT")
public class CartWebControllerWebExceptionHandlerIT extends AbstractGlobalWebExceptionHandlerIT {
    private final static String CART_ROOT = "/cart/items";

    @Test
    void getCartItems_handleGeneralProjectException_inCaseMoreThenOneCartFound() throws Exception {
        // given
        var expectedView = ERR_DIR_NAME + HttpStatus.INTERNAL_SERVER_ERROR.value();
        var expectedErrorMessage = "More than one cart found";
        // when
        when(mockedCartRepository.findAll()).thenReturn(List.of(new CartEntity(), new CartEntity()));

        mockMvc.perform(get(CART_ROOT))
                // then
                .andExpect(view().name(expectedView))
                .andExpect(model().attribute(ERR_MESSAGE_KEY, expectedErrorMessage));
    }

    @Test
    void updateCartItems_handleItemNotFoundException_inCaseGivenItemIdNotFoundInDb() throws Exception {
        // given
        var expectedView = ERR_DIR_NAME + HttpStatus.NOT_FOUND.value();
        var expectedErrorMessage = MessageFormat.format(ITEM_NOT_FOUND_ERROR_MESSAGE_PATTERN, NON_EXISTENT_ID);
        // when
        when(mockedItemRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

        mockMvc.perform(post(CART_ROOT)
                        .param(CartItemAction.Fields.action, CartAction.PLUS.toString())
                        .param("id", NON_EXISTENT_ID.toString())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                // then
                .andExpect(view().name(expectedView))
                .andExpect(model().attribute(ERR_MESSAGE_KEY, expectedErrorMessage));
    }

    @Test
    void updateCartItems_handleMethodArgumentNotValidException_inCaseGivenItemActionIsUnprocessable() throws Exception {
        // given
        var expectedView = ERR_DIR_NAME + HttpStatus.BAD_REQUEST.value();
        // when
        mockMvc.perform(post(CART_ROOT)
                        .param(CartItemAction.Fields.action, "HOW_ARE_YOU")
                        .param("id", NON_EXISTENT_ID.toString())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                // then
                .andExpect(view().name(expectedView))
                .andExpect(model().attributeExists(ERR_MESSAGE_KEY));
    }

    @EnumSource(value = CartAction.class, names = {"MINUS", "DELETE"})
    @ParameterizedTest
    void updateCartItems_handleGeneralProjectException_inCaseAttemptToRemoveOrDeleteItemWhenCartIsNotCreated(CartAction action) throws Exception {
        // given
        var expectedView = ERR_DIR_NAME + HttpStatus.INTERNAL_SERVER_ERROR.value();
        var itemEntity = ItemEntity.builder().id(1L).build();
        var cartEntity = CartEntity.builder().id(1L).build();
        // when
        when(mockedCartRepository.findAll()).thenReturn(List.of());
        when(mockedCartRepository.save(any())).thenReturn(cartEntity);

        when(mockedItemRepository.findById(itemEntity.getId())).thenReturn(Optional.of(itemEntity));
        when(mockedCartItemRepository.findById(new CartItemEntity.CartItemCompositeId(1L, itemEntity.getId())))
                .thenReturn(Optional.empty());

        mockMvc.perform(post(CART_ROOT)
                        .param(CartItemAction.Fields.action, action.toString())
                        .param("id", itemEntity.getId().toString())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                // then
                .andExpect(view().name(expectedView))
                .andExpect(model().attribute(ERR_MESSAGE_KEY, "Impossible event! Check it ASAP!"));
    }
}
