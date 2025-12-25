package edu.yandex.project.integration.controller;

import edu.yandex.project.controller.dto.ItemsPageableRequest;
import edu.yandex.project.controller.dto.enums.CartAction;
import edu.yandex.project.repository.util.view.ItemJoinCartPageView;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;

@Tag("ItemWebControllerIT")
public class ItemWebControllerIT extends AbstractControllerIT {
    private final static String ITEMS_ROOT = "/items";

    @Nested
    class Showcase {
        @Test
        void getItemsShowcase_withDefaultRequestParameters_shouldReturnItemsViewWithRequiredElements() {
            // given
            var defaultQuery = new ItemsPageableRequest(null, null, null, null);
            var expectedItems = getItemJoinCartPageViews(defaultQuery);
            assertThat(expectedItems).hasSize(defaultQuery.pageSize());

            var toBeChecked = DynamicParametersToBeChecked.from(expectedItems);
            // when
            webTestClient.get()
                    .uri(ITEMS_ROOT)
                    .exchange()
                    // then
                    .expectStatus().isOk()
                    .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                    .expectBody(String.class).value(htmlView -> {
                        // top elements
                        assertThat(htmlView).contains(
                                "<span class=\"badge text-bg-success\">Витрина магазина</span>",
                                "<a href=\"/orders\" class=\"btn btn-secondary ms-auto bi bi-file-earmark-text\">Заказы</a>",
                                "<a href=\"/cart/items\" class=\"btn btn-secondary bi bi-cart4\">Корзина</a>"
                        );
                        assertThat(htmlView).contains(
                                "<option value=\"5\" selected=\"selected\">5</option>",  // pageSize = 5
                                "<span>Страница: 1</span>",
                                "form=\"main\">&rarr;"  // there are another pages
                        );
                        validateItemInCartSign(htmlView, false);
                        validateShowcaseDynamicParameters(htmlView, toBeChecked);
                    });
        }

        @Test
        void getItemsShowcase_withSearchFilterByTitle() {
            // given
            var queryWithSearchFilter = new ItemsPageableRequest("Телевизор 4K Smart TV", null, 100, null);
            var requestFormData = createPageableRequestQuery(queryWithSearchFilter);

            var expectedItem = getItemJoinCartPageViews(queryWithSearchFilter);
            assertThat(expectedItem).hasSize(1);

            var toBeChecked = DynamicParametersToBeChecked.from(expectedItem);
            // when
            webTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(ITEMS_ROOT)
                            .queryParams(requestFormData)
                            .build())
                    .exchange()
                    // then
                    .expectStatus().isOk()
                    .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                    .expectBody(String.class).value(htmlView -> {
                        var chosenPageSize = queryWithSearchFilter.pageSize();
                        assertThat(htmlView).contains("<option value=\"" + chosenPageSize + "\" selected=\"selected\">" + chosenPageSize + "</option>");
                        assertThat(htmlView).contains("<span>Страница: 1</span>");
                        assertThat(htmlView).doesNotContain("form=\"main\">&rarr;");  // there is only one page

                        validateItemInCartSign(htmlView, false);
                        validateShowcaseDynamicParameters(htmlView, toBeChecked);
                    });
        }

        @Test
        void updateCartFromItemsShowcase_cartActionIsPLUS() {
            // given
            assertThat(cartRepository.count().block()).isEqualTo(0);

            var queryWithSearchFilter = new ItemsPageableRequest("Телевизор 4K Smart TV", null, null, null);

            var testItems = getItemJoinCartPageViews(queryWithSearchFilter).getContent();
            assertThat(testItems).hasSize(1);
            var testItem = testItems.getFirst();
            // when
            var requestFormData = updateCartItemFromShowcase(queryWithSearchFilter, testItem.id(), CartAction.PLUS);
            // validate item state in showcase after cart update
            webTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(ITEMS_ROOT)
                            .queryParams(requestFormData)
                            .build())
                    .exchange()
                    // then
                    .expectStatus().isOk()
                    .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                    .expectBody(String.class)
                    .value(htmlView -> validateItemInCartSign(htmlView, true));
            // check if cart updated
            var cartItems = getCartItems();
            assertThat(cartItems)
                    .isNotEmpty()
                    .hasSize(1);
            var cartItem = cartItems.getFirst();
            assertThat(cartItem.getTotalCost()).isEqualTo(testItem.price());
            assertThat(cartItem.getItemCount()).isEqualTo(1);
            assertThat(cartItem.getId().itemId()).isEqualTo(testItem.id());
            assertThat(cartItem.getId().cartId()).isNotNull();
        }

        @Test
        void updateCartFromItemsShowcase_cartActionIsMINUS() {
            // given
            var queryWithSearchFilter = new ItemsPageableRequest("Телевизор 4K Smart TV", null, null, null);

            var testItems = getItemJoinCartPageViews(queryWithSearchFilter).getContent();
            assertThat(testItems).hasSize(1);
            var testItem = testItems.getFirst();
            // add item to the cart
            updateCartItemFromShowcase(queryWithSearchFilter, testItem.id(), CartAction.PLUS);
            // check if item added
            assertThat(cartRepository.count().block()).isEqualTo(1);
            assertThat(Objects.requireNonNull(cartItemRepository.findCartItemByItemId(testItem.id()).block())
                    .getItemCount()).isEqualTo(1);
            // when
            var requestFormData = updateCartItemFromShowcase(queryWithSearchFilter, testItem.id(), CartAction.MINUS);
            // validate item state in showcase after cart update
            webTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(ITEMS_ROOT)
                            .queryParams(requestFormData)
                            .build())
                    .exchange()
                    // then
                    .expectStatus().isOk()
                    .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                    .expectBody(String.class)
                    .value(htmlView -> validateItemInCartSign(htmlView, false));
            // check if cart updated
            assertThat(cartRepository.count().block()).isEqualTo(1);    // cart must not be deleted
            assertThat(cartItemRepository.findCartItemByItemId(testItem.id()).block()).isNull();
        }
    }

    @Nested
    class ItemView {
        private final long ITEM_ID = ThreadLocalRandom.current().nextLong(1, 12);

        @Test
        void getItemView_inCaseItemNotInCart_shouldReturnItemInfoWithDefaultInCartCounter() {
            // given
            var item = itemRepository.findById(ITEM_ID).block();
            assertThat(item).isNotNull();
            // when
            webTestClient.get()
                    .uri(ITEMS_ROOT + "/" + item.getId())
                    .exchange()
                    // then
                    .expectStatus().isOk()
                    .expectBody(String.class)
                    .value(htmlView -> {
                        // top elements
                        assertThat(htmlView).contains(
                                "<span class=\"badge text-bg-success\">Страница товара</span>",
                                "<a href=\"/cart/items\" class=\"btn btn-secondary bi bi-cart4\">Корзина</a>",
                                "<a href=\"/items\" class=\"btn btn-secondary bi bi-arrow-left-square\">Главная</a>"
                        );
                        // dynamic elements
                        assertThat(htmlView).contains(
                                "<img class=\"p-2\" src=\"" + item.getImgPath() + "\" alt=\"Нет изображения\" width=\"300\" height=\"300\">",
                                "<h5 class=\"card-title\">" + item.getTitle() + "</h5>",
                                "<span class=\"badge text-bg-success justify-content-end\">" + item.getPrice() + " руб.</span>",
                                "<p class=\"card-text\">" + item.getDescription() + "</p>",
                                // add cart icon (must be disabled if at least 1 item with given id added to the cart)
                                "<button type=\"submit\" class=\"btn btn-warning ms-auto bi bi-cart4\" name=\"action\" value=\"PLUS\"></button>"
                        );
                        validateItemInCartSign(htmlView, false);
                    });
        }

        @Test
        void updateCartFromItemView_cartActionIsPLUS() {
            // given
            var item = itemRepository.findById(ITEM_ID).block();
            assertThat(item).isNotNull();
            // when
            // add 3 identical items (for check count parameter)
            updateCartItemFromItemView(item.getId(), CartAction.PLUS);
            updateCartItemFromItemView(item.getId(), CartAction.PLUS);
            var redirectUri = updateCartItemFromItemView(item.getId(), CartAction.PLUS);
            // then
            webTestClient.get()
                    .uri(redirectUri)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(String.class)
                    .value(htmlView -> {
                        assertThat(htmlView).doesNotContain("<button type=\"submit\" class=\"btn btn-warning ms-auto bi bi-cart4\" name=\"action\" value=\"PLUS\"></button>");
                        validateItemInCartSign(htmlView, true, 3);
                    });
        }

        @Test
        void updateCartFromItemView_cartActionIsMINUS() {
            // given
            var item = itemRepository.findById(ThreadLocalRandom.current().nextLong(1, 12)).block();
            assertThat(item).isNotNull();
            // add 2 items to the cart before remove
            updateCartItemFromItemView(item.getId(), CartAction.PLUS);
            updateCartItemFromItemView(item.getId(), CartAction.PLUS);
            // when
            // delete first
            var redirectUri = updateCartItemFromItemView(item.getId(), CartAction.MINUS);
            // then
            webTestClient.get()
                    .uri(redirectUri)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(String.class)
                    .value(htmlView -> {
                        assertThat(htmlView).doesNotContain("<button type=\"submit\" class=\"btn btn-warning ms-auto bi bi-cart4\" name=\"action\" value=\"PLUS\"></button>");
                        validateItemInCartSign(htmlView, true);
                    });
            // delete last
            redirectUri = updateCartItemFromItemView(item.getId(), CartAction.MINUS);
            // then
            webTestClient.get()
                    .uri(redirectUri)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(String.class)
                    .value(htmlView -> {
                        assertThat(htmlView).contains("<button type=\"submit\" class=\"btn btn-warning ms-auto bi bi-cart4\" name=\"action\" value=\"PLUS\"></button>");
                        validateItemInCartSign(htmlView, false);
                    });
        }
    }

    private String updateCartItemFromItemView(Long itemId, CartAction action) {
        var uri = ITEMS_ROOT + "/" + itemId;
        var requestFormData = new LinkedMultiValueMap<>(Map.of(
                "id", List.of(itemId.toString()),
                "action", List.of(action.name())
        ));
        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(uri)
                        .queryParams(requestFormData)
                        .build())
                .exchange()
                // then
                .expectStatus().is3xxRedirection()
                .expectHeader().value("Location", Matchers.equalTo(uri));
        return uri;
    }

    private LinkedMultiValueMap<String, String> updateCartItemFromShowcase(ItemsPageableRequest itemsPageableRequest,
                                                                           Long itemId,
                                                                           CartAction action) {
        var requestFormData = createPageableRequestQuery(itemsPageableRequest);
        requestFormData.put("id", List.of(itemId.toString()));
        requestFormData.put("action", List.of(action.name()));

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(ITEMS_ROOT)
                        .queryParams(requestFormData)
                        .build())
                .exchange()
                // then
                .expectStatus().is3xxRedirection()
                .expectHeader().value("Location", containsString("/items?"));

        requestFormData.remove("id");
        requestFormData.remove("action");
        return requestFormData;
    }

    private Page<ItemJoinCartPageView> getItemJoinCartPageViews(ItemsPageableRequest queryWithSearchFilter) {
        var inCartItems = itemPageableRepository.findAllWithCartCount(
                        queryWithSearchFilter.search(),
                        queryWithSearchFilter.sort().toString(),
                        PageRequest.of(queryWithSearchFilter.pageNumber(), queryWithSearchFilter.pageSize())
                )
                .block();
        assertThat(inCartItems).isNotNull();
        return inCartItems;
    }

    private static void validateItemInCartSign(String htmlView, boolean isAdded) {
        validateItemInCartSign(htmlView, isAdded, 1);
    }

    private static void validateItemInCartSign(String htmlView, boolean isAdded, int numberOfAddedItemsWithSameId) {
        if (isAdded) {
            assertThat(htmlView).contains("<span>" + numberOfAddedItemsWithSameId + "</span>");   // cart is not empty!
            assertThat(htmlView).doesNotContain("name=\"action\" value=\"MINUS\" disabled=\"disabled\">");     // item is in the cart
        } else {
            assertThat(htmlView).doesNotContain("<span>1</span>");   // cart is empty again
            assertThat(htmlView).contains("name=\"action\" value=\"MINUS\" disabled=\"disabled\">");     // item is not in the cart again
        }
    }

    private static void validateShowcaseDynamicParameters(String html, DynamicParametersToBeChecked toBeChecked) {
        toBeChecked.expectedItemIds().forEach(
                id -> assertThat(html).contains("<a href=\"/items/" + id + "\">")
        );
        toBeChecked.expectedItemTitles().forEach(
                title -> assertThat(html).contains("<h5 class=\"card-title\">" + title + "</h5>")
        );
        toBeChecked.expectedItemDescriptions().forEach(
                desc -> assertThat(html).contains("<p class=\"card-text\">" + desc + "</p>")
        );
        toBeChecked.expectedItemPrices().forEach(
                price -> assertThat(html).contains("<span class=\"badge text-bg-success justify-content-end\">" + price + " руб.</span>")
        );
        toBeChecked.expectedItemPicLinks().forEach(
                src -> assertThat(html).contains("<img src=\"" + src + "\" class=\"card-img-top\" alt=\"Нет изображения\">")
        );
    }

    private static LinkedMultiValueMap<String, String> createPageableRequestQuery(ItemsPageableRequest from) {
        return new LinkedMultiValueMap<>(Map.of(
                ItemsPageableRequest.Fields.search, List.of(from.search()),
                ItemsPageableRequest.Fields.pageNumber, List.of(from.pageNumber().toString()),
                ItemsPageableRequest.Fields.pageSize, List.of(from.pageSize().toString()),
                ItemsPageableRequest.Fields.sort, List.of(from.sort().name())
        ));
    }

    private record DynamicParametersToBeChecked(
            ArrayList<Long> expectedItemIds,
            ArrayList<String> expectedItemTitles,
            ArrayList<String> expectedItemDescriptions,
            ArrayList<Long> expectedItemPrices,
            ArrayList<String> expectedItemPicLinks) {

        public static DynamicParametersToBeChecked from(Page<ItemJoinCartPageView> expectedItems) {
            var obj = new DynamicParametersToBeChecked(
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()
            );
            for (var itemView : expectedItems) {
                obj.expectedItemIds.add(itemView.id());
                obj.expectedItemTitles.add(itemView.title());
                obj.expectedItemDescriptions.add(itemView.description());
                obj.expectedItemPrices.add(itemView.price());
                obj.expectedItemPicLinks.add(itemView.imgPath());
            }
            return obj;
        }
    }
}
