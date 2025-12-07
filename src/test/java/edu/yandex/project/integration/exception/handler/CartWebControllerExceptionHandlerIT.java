package edu.yandex.project.integration.exception.handler;

import edu.yandex.project.controller.dto.enums.CartAction;
import edu.yandex.project.entity.CartEntity;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@Tag("CartWebControllerExceptionHandlerIT")
public class CartWebControllerExceptionHandlerIT extends AbstractGlobalExceptionHandlerIT {
    private final static String CART_ROOT = "/cart/items";

    @Test
    void getCartItems_handleGeneralProjectException() throws Exception {
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
    void updateCartItems_handleItemNotFoundException() throws Exception {
        // given
        var expectedView = ERR_DIR_NAME + HttpStatus.NOT_FOUND.value();
        var expectedErrorMessage = MessageFormat.format(ITEM_NOT_FOUND_ERROR_MESSAGE_PATTERN, NON_EXISTENT_ID);
        // when
        when(mockedItemRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

        mockMvc.perform(post(CART_ROOT)
                        .param("action", CartAction.PLUS.toString())
                        .param("id", NON_EXISTENT_ID.toString())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                // then
                .andExpect(view().name(expectedView))
                .andExpect(model().attribute(ERR_MESSAGE_KEY, expectedErrorMessage));
    }
}
