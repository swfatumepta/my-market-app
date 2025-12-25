package edu.yandex.project.integration.exception.handler;

import edu.yandex.project.domain.Cart;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.MessageFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Tag("OrderWebControllerExceptionHandlerIT")
public class OrderWebControllerWebExceptionHandlerIT extends AbstractGlobalWebExceptionHandlerIT {
    private final static String ORDERS_ROOT = "/orders";

    @Test
    void getOrder_handleOrderNotFoundException_inCaseNonExistentOrderId() {
        // given
        var viewIdentifyingText = "404 - NOT_FOUND";
        var expectedMessages = MessageFormat.format(ORDER_NOT_FOUND_ERROR_MESSAGE_PATTERN, NON_EXISTENT_ID);
        // when
        when(mockedOrderRepository.findById(NON_EXISTENT_ID)).thenReturn(Mono.empty());

        webTestClient.get()
                .uri(ORDERS_ROOT + "/" + NON_EXISTENT_ID)
                .exchange()
                // then
                .expectStatus().isNotFound()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(html -> {
                    assertThat(html).contains(viewIdentifyingText);
                    assertThat(html).contains(expectedMessages);
                });
    }

    @Test
    void placeAnOrder_handleGeneralProjectException_inCaseTryToPlaceAnOrderWithNoCartPresent() {
        // given
        var viewIdentifyingText = "500 - INTERNAL_SERVER_ERROR";
        var expectedMessages = "Cart is empty";
        // when
        when(mockedCartRepository.findAll()).thenReturn(Flux.just(new Cart()));
        when(mockedCartItemRepository.findAllByCartId(any())).thenReturn(Flux.empty());

        webTestClient.post()
                .uri(ORDERS_ROOT + "/place-an-order")
                .exchange()
                // then
                .expectStatus().is5xxServerError()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(html -> {
                    assertThat(html).contains(viewIdentifyingText);
                    assertThat(html).contains(expectedMessages);
                });

        verifyNoInteractions(mockedOrderRepository);
        verify(mockedCartRepository, never()).save(any());
    }
}
