package edu.yandex.project.integration.controller;

import edu.yandex.project.domain.CartItem;
import edu.yandex.project.domain.Item;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.text.MessageFormat.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;

@Slf4j
@Tag("OrderWebControllerIT")
public class OrderWebControllerIT extends AbstractControllerIT {
    private final static String ORDERS_ROOT = "/orders";
    private final static String PLACE_AN_ORDER_URI = ORDERS_ROOT + "/place-an-order";

    @Test
    void getOrders_inCaseNoOrders_success() {
        // given
        assertThat(orderRepository.findAll().collectList().block()).isEmpty();
        assertThat(orderItemRepository.findAll().collectList().block()).isEmpty();
        // when
        webTestClient.get()
                .uri(ORDERS_ROOT)
                // then
                .exchange()
                // then
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class).value(htmlView -> {
                    // top elements
                    validateOrderListStaticElements(htmlView);
                    // no order are present
                    assertThat(htmlView).doesNotContain("<div class=\"card\">");
                });
    }

    @Test
    void getOrders_inCaseOrdersHistoryIsNotEmpty() {
        // given
        this.fillFirstCart();
        var cartItems = getCartItems();
        assertThat(cartItems).isNotEmpty();

        var cartTotalCost = cartItems.stream()
                .map(CartItem::getTotalCost)
                .reduce(0L, Long::sum);

        var itemsMap = getItemsMap(cartItems);
        // when
        placeAnOrder();
        // then
        webTestClient.get()
                .uri(ORDERS_ROOT)
                .exchange()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class).value(htmlView -> {
                    // top elements
                    validateOrderListStaticElements(htmlView);
                    // dynamic elements
                    cartItems.forEach(cartItem -> {
                        var item = itemsMap.get(cartItem.getId().itemId());
                        assertThat(item).isNotNull();
                        var line = format(
                                "<li class=\"list-group-item\">{0} ({1} шт.) {2} руб.</li>",
                                item.getTitle(), cartItem.getItemCount(), cartItem.getTotalCost().toString()
                        );
                        assertThat(htmlView).contains(line);
                    });
                    assertThat(htmlView).contains("<b>Сумма: " + cartTotalCost + " руб.</b>");
                });
        this.validateOrderDbState(cartTotalCost, cartItems);

        assertThat(cartRepository.count().block()).isEqualTo(0);    // cart must be deleted after order creation
    }

    @Test
    void getOrder_withNewOrderIsTrue() {
        // given
        this.fillFirstCart();
        var cartItems = getCartItems();
        assertThat(cartItems).isNotEmpty();

        var cartItemTotal = cartItems.stream()
                .map(CartItem::getTotalCost)
                .reduce(0L, Long::sum);

        var itemsMap = getItemsMap(cartItems);
        // when
        placeAnOrder();
        // then
        webTestClient.get()
                .uri(ORDERS_ROOT + "/1?newOrder=true")
                .exchange()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class).value(htmlView -> {
                    assertThat(htmlView).contains("Поздравляем! Успешная покупка! &#128578;");  // new order created event check
                    // top elements
                    assertThat(htmlView).contains(
                            "<a href=\"/orders\" class=\"btn btn-secondary ms-auto bi bi-file-earmark-text\">Заказы</a>",
                            "<a href=\"/cart/items\" class=\"btn btn-secondary bi bi-cart4\">Корзина</a>",
                            "<a href=\"/items\" class=\"btn btn-secondary bi bi-arrow-left-square\">Главная</a>"
                    );
                    // dynamic elements
                    cartItems.forEach(cartItem -> {
                        var item = itemsMap.get(cartItem.getId().itemId());
                        assertThat(item).isNotNull();
                        assertThat(htmlView).contains(
                                "<b>" + item.getTitle() + "</b>",
                                "<li class=\"list-group-item\">" + cartItem.getItemCount() + " шт.</li>",
                                "<li class=\"list-group-item\">" + item.getPrice() + " руб.</li>",
                                "<b>Сумма: " + cartItem.getTotalCost() + " руб.</b>"
                        );
                    });
                    assertThat(htmlView).contains("<h3>Сумма: " + cartItemTotal + " руб.</h3>");
                });
        assertThat(cartRepository.count().block()).isEqualTo(0);    // cart must be deleted after order creation
    }

    private void placeAnOrder() {
        // when
        webTestClient.post()
                .uri(PLACE_AN_ORDER_URI)
                // then
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().value("Location", containsString(ORDERS_ROOT + "/1?newOrder=true"));
    }

    private void validateOrderDbState(Long cartTotalCost, List<CartItem> cartItems) {
        var orders = orderRepository.findAll()
                .collectList()
                .block();
        assertThat(orders)
                .isNotEmpty()
                .hasSize(1);
        var order = orders.getFirst();
        assertThat(order.getTotalCost()).isEqualTo(cartTotalCost);
        var orderItems = orderItemRepository.findAllByOrderId(order.getId())
                .collectList()
                .block();
        assertThat(orderItems)
                .isNotEmpty()
                .hasSize(cartItems.size());
    }

    private @NotNull Map<Long, Item> getItemsMap(Collection<CartItem> cartItems) {
        var itemIds = cartItems.stream()
                .map(CartItem::getId)
                .map(CartItem.CartItemCompositeId::itemId)
                .collect(Collectors.toSet());
        var items = itemRepository.findAllById(itemIds)
                .collectList()
                .block();
        assertThat(items).isNotEmpty();
        return items.stream()
                .collect(Collectors.toMap(Item::getId, item -> item));
    }

    private void fillFirstCart() {
        log.debug("AbstractControllerIT::fillFirstCart in");
        databaseClient.sql("""
                        -- create cart
                        INSERT INTO carts (id)
                        VALUES (555_555);
                        -- insert items in the cart
                        INSERT INTO cart_item (cart_id, item_id, items_count, total_cost)
                        VALUES (555_555, 1, 1, (SELECT price * 1 FROM items WHERE id = 1)),
                               (555_555, 2, 2, (SELECT price * 2 FROM items WHERE id = 2)),
                               (555_555, 3, 3, (SELECT price * 3 FROM items WHERE id = 3)),
                               (555_555, 4, 4, (SELECT price * 4 FROM items WHERE id = 4)),
                               (555_555, 5, 5, (SELECT price * 5 FROM items WHERE id = 5));
                        """)
                .then()
                .block();
        log.debug("AbstractControllerIT::fillFirstCart out");
    }

    private static void validateOrderListStaticElements(String htmlView) {
        assertThat(htmlView).contains(
                "<span class=\"badge text-bg-success\">История заказов</span>",
                "<a href=\"/cart/items\" class=\"btn btn-secondary bi bi-cart4 ms-auto\">Корзина</a>",
                "<a href=\"/items\" class=\"btn btn-secondary bi bi-arrow-left-square\">Главная</a>"
        );
    }
}
