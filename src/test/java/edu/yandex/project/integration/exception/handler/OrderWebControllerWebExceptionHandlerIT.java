package edu.yandex.project.integration.exception.handler;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@Tag("OrderWebControllerExceptionHandlerIT")
public class OrderWebControllerWebExceptionHandlerIT extends AbstractGlobalWebExceptionHandlerIT {
    private final static String ORDERS_ROOT = "/orders";

    @Test
    void getOrders_inCaseNoOrders_success() throws Exception {
        // given
        // when
        mockMvc.perform(get(ORDERS_ROOT))
                // then
                .andExpect(view().name("orders"))
                .andExpect(model().attributeExists("orders"))
                .andExpect(model().size(1));
    }
}
