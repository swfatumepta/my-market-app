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

@Tag("ItemWebControllerExceptionHandlerIT")
public class ItemWebControllerExceptionHandlerIT extends AbstractGlobalExceptionHandlerIT {
    private final static String ITEMS_ROOT = "/items";

    @Test
    void getItemView_itemViewNotFound_fail() throws Exception {
        // given
        var expectedView = ERR_DIR_NAME + HttpStatus.NOT_FOUND.value();
        var expectedErrorMessage = MessageFormat.format(ITEM_NOT_FOUND_ERROR_MESSAGE_PATTERN, NON_EXISTENT_ID);
        // when
        when(mockedItemRepository.findByIdWithCartCount(NON_EXISTENT_ID)).thenReturn(Optional.empty());

        mockMvc.perform(get(ITEMS_ROOT + "/" + NON_EXISTENT_ID))
                // then
                .andExpect(view().name(expectedView))
                .andExpect(model().attribute(ERR_MESSAGE_KEY, expectedErrorMessage));
    }
}
