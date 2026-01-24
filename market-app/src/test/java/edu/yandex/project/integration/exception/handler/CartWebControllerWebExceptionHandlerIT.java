package edu.yandex.project.integration.exception.handler;

import edu.yandex.project.controller.dto.CartItemAction;
import edu.yandex.project.controller.dto.enums.CartAction;
import edu.yandex.project.domain.Cart;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.MessageFormat;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@Tag("CartWebControllerExceptionHandlerIT")
public class CartWebControllerWebExceptionHandlerIT extends AbstractGlobalWebExceptionHandlerIT {
    private final static String CART_ROOT = "/cart/items";

    @Test
    void getCartItems_handleGeneralProjectException_inCaseMoreThenOneCartFound() {
        // given
        var viewIdentifyingText = "500 - INTERNAL_SERVER_ERROR";
        var expectedMessages = "More than one cart found";
        // when
        when(mockedCartRepository.findAll()).thenReturn(Flux.fromIterable(List.of(new Cart(), new Cart())));

        webTestClient.get()
                .uri(CART_ROOT)
                .exchange()
                // then
                .expectStatus().is5xxServerError()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(html -> {
                            assertThat(html).contains(viewIdentifyingText);
                            assertThat(html).contains(expectedMessages);
                        }
                );
    }

    @Test
    void updateCartItems_handleItemNotFoundException_inCaseGivenItemIdNotFoundInDb() {
        // given
        var viewIdentifyingText = "404 - NOT_FOUND";
        var expectedMessages = MessageFormat.format(ITEM_NOT_FOUND_ERROR_MESSAGE_PATTERN, NON_EXISTENT_ID);
        // when
        when(mockedCartRepository.findAll()).thenReturn(Flux.just(new Cart()));
        when(mockedItemRepository.findById(NON_EXISTENT_ID)).thenReturn(Mono.empty());

        webTestClient.post()
                .uri(CART_ROOT)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(CartItemAction.Fields.action, CartAction.PLUS.toString())
                        .with("id", NON_EXISTENT_ID.toString())
                )
                .exchange()
                // then
                .expectStatus().isNotFound()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(html -> {
                            assertThat(html).contains(viewIdentifyingText);
                            assertThat(html).contains(expectedMessages);
                        }
                );
    }

    @Test
    void updateCartItems_handleMethodArgumentNotValidException_inCaseGivenItemActionIsNotExist() {
        // given
        var viewIdentifyingText = "400 - BAD_REQUEST";
        // when
        webTestClient.post()
                .uri(CART_ROOT)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(CartItemAction.Fields.action, "HOW_ARE_YOU")
                        .with("id", NON_EXISTENT_ID.toString()))
                .exchange()
                // then
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(html -> {
                            assertThat(html).contains(viewIdentifyingText);
                            assertThat(html).contains("HOW_ARE_YOU");
                        }
                );
    }
}
