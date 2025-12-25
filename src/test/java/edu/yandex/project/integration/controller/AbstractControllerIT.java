package edu.yandex.project.integration.controller;

import edu.yandex.project.controller.dto.CartItemAction;
import edu.yandex.project.domain.Cart;
import edu.yandex.project.domain.CartItem;
import edu.yandex.project.integration.AbstractDbIT;
import edu.yandex.project.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@AutoConfigureWebTestClient
@ActiveProfiles({"test", "debug"})
@SpringBootTest
public class AbstractControllerIT extends AbstractDbIT {
    protected final static String CART_ROOT = "/cart/items";

    @Autowired
    protected WebTestClient webTestClient;
    @Autowired
    protected DatabaseClient databaseClient;

    @Autowired
    protected CartRepository cartRepository;
    @Autowired
    protected CartItemRepository cartItemRepository;
    @Autowired
    protected ItemRepository itemRepository;
    @Autowired
    protected ItemPageableRepository itemPageableRepository;
    @Autowired
    protected OrderRepository orderRepository;

    @BeforeEach
    protected void dropData() {
        log.info("AbstractControllerIT::dropData in");
        databaseClient.sql("""
                        DELETE FROM cart_item WHERE TRUE;
                        DELETE FROM carts WHERE TRUE;
                        DELETE FROM order_item WHERE TRUE;
                        DELETE FROM orders WHERE TRUE;
                        ALTER SEQUENCE carts_id_seq RESTART WITH 1;
                        ALTER SEQUENCE order_item_id_seq RESTART WITH 1;
                        ALTER SEQUENCE orders_id_seq RESTART WITH 1;
                        """)
                .then()
                .block();
        log.info("AbstractControllerIT::dropData out");
    }

    protected List<CartItem> getCartItems() {
        var cartId = this.validateAndGetCart().getId();
        return cartItemRepository.findAllByCartId(cartId)
                .collectList()
                .switchIfEmpty(Mono.just(List.of()))
                .block();
    }

    protected @NotNull Cart validateAndGetCart() {
        var carts = cartRepository.findAll()
                .collectList()
                .block();
        assertThat(carts)
                .overridingErrorMessage("at first, you should call an action, that creates cart")
                .isNotEmpty()
                .hasSize(1);
        return carts.getFirst();
    }

    protected void updateCartFromCartView(CartItemAction cartItemAction) {
        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(CART_ROOT)
                        .queryParam("id", cartItemAction.itemId())
                        .queryParam("action", cartItemAction.action())
                        .build())
                .exchange()
                // then
                .expectStatus().is3xxRedirection()
                .expectHeader().value("Location", Matchers.equalTo(CART_ROOT));
    }
}
