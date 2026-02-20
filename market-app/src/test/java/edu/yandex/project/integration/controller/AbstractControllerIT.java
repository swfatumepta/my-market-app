package edu.yandex.project.integration.controller;

import edu.yandex.project.cache.ItemCache;
import edu.yandex.project.cache.ItemPageableRequestCache;
import edu.yandex.project.client.WalletApi;
import edu.yandex.project.controller.dto.CartItemAction;
import edu.yandex.project.domain.Cart;
import edu.yandex.project.domain.CartItem;
import edu.yandex.project.domain.Item;
import edu.yandex.project.integration.AbstractDbIT;
import edu.yandex.project.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@AutoConfigureWebTestClient(timeout = "PT5M")
@ActiveProfiles({"debug", "test"})
@SpringBootTest
public class AbstractControllerIT extends AbstractDbIT {
    protected final static String CART_ROOT = "/cart/items";

    @Value("${spring.cache.item.name}")
    protected String itemCacheKeyPrefix;
    @Value("${spring.cache.item-page.name}")
    protected String itemPageCacheKeyPrefix;
    @Value("${spring.cache.item-page.key-pattern}")
    protected String itemPageCacheKeyPattern;

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
    @Autowired
    protected OrderItemRepository orderItemRepository;

    @Autowired
    protected ReactiveRedisTemplate<String, Item> itemReactiveRedisTemplate;

    @MockitoSpyBean
    protected ItemCache itemCacheSpy;
    @MockitoSpyBean
    protected ItemPageableRequestCache itemPageableRequestCacheSpy;

    @MockitoBean
    protected WalletApi mockedWalletClient;

    @BeforeEach
    protected void dropData() {
        log.info("AbstractControllerIT::dropData in");
        StepVerifier.create(Mono.when(this.clearDb(), this.clearCache()))
                .verifyComplete();
        log.info("AbstractControllerIT::dropData out");
    }

    protected Mono<Void> clearCache() {
        return itemReactiveRedisTemplate.getConnectionFactory()
                .getReactiveConnection()
                .serverCommands()
                .flushAll()
                .then();
    }

    protected Mono<Void> clearDb() {
        return databaseClient.sql("DELETE FROM cart_item").then()
                .then(databaseClient.sql("DELETE FROM carts").then())
                .then(databaseClient.sql("DELETE FROM order_item").then())
                .then(databaseClient.sql("DELETE FROM orders").then())
                .then(databaseClient.sql("ALTER SEQUENCE carts_id_seq RESTART WITH 1").then())
                .then(databaseClient.sql("ALTER SEQUENCE order_item_id_seq RESTART WITH 1").then())
                .then(databaseClient.sql("ALTER SEQUENCE orders_id_seq RESTART WITH 1").then());
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
