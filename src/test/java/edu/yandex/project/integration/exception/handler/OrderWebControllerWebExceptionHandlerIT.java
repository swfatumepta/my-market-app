package edu.yandex.project.integration.exception.handler;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.text.MessageFormat;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@Tag("OrderWebControllerExceptionHandlerIT")
public class OrderWebControllerWebExceptionHandlerIT extends AbstractGlobalWebExceptionHandlerIT {
    private final static String ORDERS_ROOT = "/orders";

    @Test
    void getOrder_handleOrderNotFoundException_inCaseNonExistentOrderId() throws Exception {
        // given
        var expectedView = ERR_DIR_NAME + HttpStatus.NOT_FOUND.value();
        var expectedErrorMessage = MessageFormat.format(ORDER_NOT_FOUND_ERROR_MESSAGE_PATTERN, NON_EXISTENT_ID);
        // when
        when(mockedOrderRepository.findWithItemsById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

        mockMvc.perform(get(ORDERS_ROOT + "/" + NON_EXISTENT_ID))
                // then
                .andExpect(view().name(expectedView))
                .andExpect(model().attribute(ERR_MESSAGE_KEY, expectedErrorMessage));
    }

    @Test
    void placeAnOrder_handleGeneralProjectException_inCaseTryToPlaceAnOrderWithNoCartPresent() throws Exception {
        // given
        var expectedView = ERR_DIR_NAME + HttpStatus.INTERNAL_SERVER_ERROR.value();
        var expectedErrorMessage = "There are no cart found in database!";
        // when
        when(mockedCartRepository.findFirstByIdIsNotNull()).thenReturn(Optional.empty());

        mockMvc.perform(post(ORDERS_ROOT + "/place-an-order"))
                // then
                .andExpect(view().name(expectedView))
                .andExpect(model().attribute(ERR_MESSAGE_KEY, expectedErrorMessage));

        verifyNoInteractions(mockedOrderRepository);
        verify(mockedCartRepository, never()).save(any());
    }
}
