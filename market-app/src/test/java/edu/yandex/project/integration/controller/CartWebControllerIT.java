package edu.yandex.project.integration.controller;

import edu.yandex.project.controller.dto.CartItemAction;
import edu.yandex.project.controller.dto.enums.CartAction;
import edu.yandex.project.domain.Item;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("ItemWebControllerIT")
public class CartWebControllerIT extends AbstractControllerIT {

    @Test
    void getCartItems_inCaseCartIsEmpty() {
        // given
        assertThat(cartRepository.count().block()).isEqualTo(0);
        // when
        webTestClient.get()
                .uri(CART_ROOT)
                // then
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class).value(CartWebControllerIT::validateEmptyCart);
    }

    @Test
    void getCartItems_inCaseCartContainsTwoIdenticalItems() {
        // given
        var item = itemRepository.findById(2L).block();
        assertThat(item).isNotNull();

        updateCartFromCartView(new CartItemAction(CartAction.PLUS, item.getId()));
        updateCartFromCartView(new CartItemAction(CartAction.PLUS, item.getId()));
        // when
        webTestClient.get()
                .uri(CART_ROOT)
                // then
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class).value(htmlView -> {
                    // top elements
                    assertThat(htmlView).doesNotContain("<h4 class=\"text-muted\">Корзина пуста</h4>");
                    // dynamic elements
                    checkIfItemIsPresent(htmlView, item, 2);
                    // check if place-an-order form is present
                    assertThat(htmlView).contains(
                            "<form action=\"/orders/place-an-order\" method=\"post\">",
                            "<h2>Итого: " + item.getPrice() * 2 + " руб.</h2>"
                    );
                });
        var cartItems = getCartItems();
        assertThat(cartItems)
                .isNotEmpty()
                .hasSize(1);
        var cartItem = cartItems.getFirst();
        assertThat(cartItem.getItemCount()).isEqualTo(2);
        assertThat(cartItem.getTotalCost()).isEqualTo(item.getPrice() * 2);
        assertThat(cartItem.getId().itemId()).isEqualTo(item.getId());
        assertThat(cartItem.getId().cartId()).isNotNull();
    }

    @Test
    void getCartItems_inCaseCartContainsThreeDifferentItems() {
        // given
        var itemIds = Set.of(1L, 2L, 3L);
        var items = itemRepository.findAllById(itemIds)
                .collectList()
                .block();
        assertThat(items)
                .isNotEmpty()
                .hasSize(3);
        // single item.id = 1
        updateCartFromCartView(new CartItemAction(CartAction.PLUS, items.getFirst().getId()));
        // two item.id = 2
        updateCartFromCartView(new CartItemAction(CartAction.PLUS, items.get(1).getId()));
        updateCartFromCartView(new CartItemAction(CartAction.PLUS, items.get(1).getId()));
        // three item.id = 3
        updateCartFromCartView(new CartItemAction(CartAction.PLUS, items.getLast().getId()));
        updateCartFromCartView(new CartItemAction(CartAction.PLUS, items.getLast().getId()));
        updateCartFromCartView(new CartItemAction(CartAction.PLUS, items.getLast().getId()));
        // when
        webTestClient.get()
                .uri(CART_ROOT)
                // then
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class).value(htmlView -> {
                    // top elements
                    assertThat(htmlView).doesNotContain("<h4 class=\"text-muted\">Корзина пуста</h4>");
                    // dynamic elements
                    var totalPrice = items.stream()
                            .map(item -> {
                                checkIfItemIsPresent(htmlView, item, item.getId().intValue());
                                return item.getPrice() * item.getId();
                            })
                            .reduce(0L, Long::sum);
                    // check if place-an-order form is present
                    assertThat(htmlView).contains(
                            "<form action=\"/orders/place-an-order\" method=\"post\">",
                            "<h2>Итого: " + totalPrice + " руб.</h2>"
                    );
                });
        var cartItems = getCartItems();
        assertThat(cartItems)
                .isNotEmpty()
                .hasSize(3);
        cartItems.forEach(cartItem -> {
            var cartItemItemId = cartItem.getId().itemId();
            assertThat(itemIds).contains(cartItemItemId);

            var sourceItem = items.stream()
                    .filter(item -> item.getId().equals(cartItemItemId))
                    .findFirst().orElseThrow();

            assertThat(cartItem.getItemCount()).isEqualTo(cartItemItemId);
            assertThat(cartItem.getTotalCost()).isEqualTo(sourceItem.getPrice() * sourceItem.getId());
        });
    }

    @Test
    void updateCartFromCartView_inCaseCartContainsTwoIdenticalItems_cartActionDELETE() {
        // given
        var item = itemRepository.findById(2L).block();
        assertThat(item).isNotNull();

        updateCartFromCartView(new CartItemAction(CartAction.PLUS, item.getId()));
        updateCartFromCartView(new CartItemAction(CartAction.PLUS, item.getId()));
        // when
        updateCartFromCartView(new CartItemAction(CartAction.DELETE, item.getId()));
        // then
        webTestClient.get()
                .uri(CART_ROOT)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class).value(CartWebControllerIT::validateEmptyCart);

        var cartItems = getCartItems();
        assertThat(cartItems).isEmpty();
        assertThat(cartRepository.count().block()).isEqualTo(1);    // cart must not be deleted
    }

    @Test
    void updateCartFromCartView_inCaseCartContainsTwoDifferentItems_cartActionDELETE() {
        // given
        var item1 = itemRepository.findById(1L).block();
        assertThat(item1).isNotNull();
        var item2 = itemRepository.findById(2L).block();
        assertThat(item2).isNotNull();

        updateCartFromCartView(new CartItemAction(CartAction.PLUS, item1.getId()));
        updateCartFromCartView(new CartItemAction(CartAction.PLUS, item2.getId()));
        // when
        updateCartFromCartView(new CartItemAction(CartAction.DELETE, item1.getId()));
        // then
        webTestClient.get()
                .uri(CART_ROOT)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class).value(htmlView -> {
                    // top elements
                    assertThat(htmlView).doesNotContain("<h4 class=\"text-muted\">Корзина пуста</h4>");
                    // dynamic elements
                    checkIfItemIsPresent(htmlView, item2, 1);
                    // check if place-an-order form is present
                    assertThat(htmlView).contains(
                            "<form action=\"/orders/place-an-order\" method=\"post\">",
                            "<h2>Итого: " + item2.getPrice() + " руб.</h2>"
                    );
                });
        var cartItems = getCartItems();
        assertThat(cartItems)
                .isNotEmpty()
                .hasSize(1);
        var cartItem = cartItems.getFirst();
        assertThat(cartItem.getItemCount()).isEqualTo(1);
        assertThat(cartItem.getTotalCost()).isEqualTo(item2.getPrice());
        assertThat(cartItem.getId().itemId()).isEqualTo(item2.getId());
        assertThat(cartItem.getId().cartId()).isNotNull();
    }

    @Test
    void updateCartFromCartView_inCaseCartContainsTwoDifferentItems_cartActionMINUS() {
        // given
        var item1 = itemRepository.findById(1L).block();
        assertThat(item1).isNotNull();
        var item2 = itemRepository.findById(2L).block();
        assertThat(item2).isNotNull();

        updateCartFromCartView(new CartItemAction(CartAction.PLUS, item1.getId()));
        updateCartFromCartView(new CartItemAction(CartAction.PLUS, item2.getId()));
        // when
        updateCartFromCartView(new CartItemAction(CartAction.MINUS, item1.getId()));
        // then
        webTestClient.get()
                .uri(CART_ROOT)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class).value(htmlView -> {
                    // top elements
                    assertThat(htmlView).doesNotContain("<h4 class=\"text-muted\">Корзина пуста</h4>");
                    // dynamic elements
                    checkIfItemIsPresent(htmlView, item2, 1);
                    // check if place-an-order form is present
                    assertThat(htmlView).contains(
                            "<form action=\"/orders/place-an-order\" method=\"post\">",
                            "<h2>Итого: " + item2.getPrice() + " руб.</h2>"
                    );
                });
        var cartItems = getCartItems();
        assertThat(cartItems)
                .isNotEmpty()
                .hasSize(1);
        var cartItem = cartItems.getFirst();
        assertThat(cartItem.getItemCount()).isEqualTo(1);
        assertThat(cartItem.getTotalCost()).isEqualTo(item2.getPrice());
        assertThat(cartItem.getId().itemId()).isEqualTo(item2.getId());
        assertThat(cartItem.getId().cartId()).isNotNull();
    }

    @Test
    void updateCartFromCartView_inCaseCartContainsTwoIdenticalItems_cartActionMINUS() {
        // given
        var item = itemRepository.findById(2L).block();
        assertThat(item).isNotNull();

        updateCartFromCartView(new CartItemAction(CartAction.PLUS, item.getId()));
        updateCartFromCartView(new CartItemAction(CartAction.PLUS, item.getId()));
        // when
        updateCartFromCartView(new CartItemAction(CartAction.MINUS, item.getId()));
        // then
        webTestClient.get()
                .uri(CART_ROOT)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class).value(htmlView -> {
                    // top elements
                    assertThat(htmlView).doesNotContain("<h4 class=\"text-muted\">Корзина пуста</h4>");
                    // dynamic elements
                    checkIfItemIsPresent(htmlView, item, 1);
                    // check if place-an-order form is present
                    assertThat(htmlView).contains(
                            "<form action=\"/orders/place-an-order\" method=\"post\">",
                            "<h2>Итого: " + item.getPrice() + " руб.</h2>"
                    );
                });
        var cartItems = getCartItems();
        assertThat(cartItems)
                .isNotEmpty()
                .hasSize(1);
        var cartItem = cartItems.getFirst();
        assertThat(cartItem.getItemCount()).isEqualTo(1);
        assertThat(cartItem.getTotalCost()).isEqualTo(item.getPrice());
        assertThat(cartItem.getId().itemId()).isEqualTo(item.getId());
        assertThat(cartItem.getId().cartId()).isNotNull();
    }

    private static void checkIfItemIsPresent(String htmlView, Item item, int itemCount) {
        assertThat(htmlView).contains(
                "<img class=\"p-2\" src=\"" + item.getImgPath() + "\" alt=\"Нет изображения\" width=\"300\" height=\"300\">",
                "<h5 class=\"card-title\">" + item.getTitle() + "</h5>",
                "<span class=\"badge text-bg-success justify-content-end\">" + item.getPrice() + " руб.</span>",
                "<p class=\"card-text\">" + item.getDescription() + "</p>",
                "<input type=\"hidden\" name=\"id\" value=\"" + item.getId() + "\">",
                "name=\"action\" value=\"MINUS\">", // CartAction.MINUS must be available for items, that has count > 0
                "<span>" + itemCount + "</span>",
                "<button type=\"submit\" class=\"btn btn-outline-secondary\" name=\"action\" value=\"PLUS\">",
                "value=\"DELETE\"></button>"    // CartAction.DELETE
        );
    }

    private static void validateEmptyCart(String htmlView) {
        // top elements
        assertThat(htmlView).contains(
                "<span class=\"badge text-bg-success\">Корзина</span>",
                "<a href=\"/orders\" class=\"btn btn-secondary ms-auto bi bi-file-earmark-text\">Заказы</a>",
                "<a href=\"/items\" class=\"btn btn-secondary bi bi-arrow-left-square\">Главная</a>"
        );
        assertThat(htmlView).contains("<h4 class=\"text-muted\">Корзина пуста</h4>");
        // check if there is no possibility to make an order
        assertThat(htmlView).doesNotContain("<form action=\"/orders/place-an-order\" method=\"post\">");
    }
}
