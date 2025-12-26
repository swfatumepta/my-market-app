package edu.yandex.project.integration.exception.handler;

import edu.yandex.project.controller.dto.CartItemAction;
import edu.yandex.project.controller.dto.ItemsPageableRequest;
import edu.yandex.project.controller.dto.enums.CartAction;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import reactor.core.publisher.Mono;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@Tag("ItemWebControllerExceptionHandlerIT")
public class ItemWebControllerWebExceptionHandlerIT extends AbstractGlobalWebExceptionHandlerIT {
    private final static String ITEMS_ROOT = "/items";

    @MethodSource("invalidItemsPageableRequestProvider")
    @ParameterizedTest(name = "ItemWebController::getItemsShowcase -> {0}")
    void getItemsShowcase_handleValidationExceptions(String ignored, TestCaseData given) {
        // given
        var viewIdentifyingText = "400 - BAD_REQUEST";
        // when
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(ITEMS_ROOT)
                        .queryParams(new LinkedMultiValueMap<>(given.requestParams()))
                        .build())
                .exchange()
                // then
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(validate(given, viewIdentifyingText));
    }

    @MethodSource({"invalidItemsPageableRequestProvider", "invalidCartItemActionProvider"})
    @ParameterizedTest(name = "ItemWebController::updateCartFromItemsShowcase -> {0}")
    void updateCartFromItemsShowcase_handleValidationExceptions(String ignored, TestCaseData given) {
        // given
        var viewIdentifyingText = "400 - BAD_REQUEST";
        // when
        webTestClient.post()
                .uri(ITEMS_ROOT)
                .bodyValue(new LinkedMultiValueMap<>(given.requestParams()))
                .exchange()
                // then
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(validate(given, viewIdentifyingText));
    }

    @MethodSource("invalidCartItemActionProvider")
    @ParameterizedTest(name = "ItemWebController::updateCartFromItemView -> {0}")
    void updateCartFromItemView_handleValidationExceptions(String ignored, TestCaseData given) {
        // given
        var viewIdentifyingText = "400 - BAD_REQUEST";
        // when
        webTestClient.post()
                .uri(ITEMS_ROOT + "/1")
                .bodyValue(new LinkedMultiValueMap<>(given.requestParams()))
                .exchange()
                // then
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(validate(given, viewIdentifyingText));
    }

    @Test
    void getItemView_handleItemNotFoundException_inCaseNonExistentItemId() {
        // given
        var viewIdentifyingText = "404 - NOT_FOUND";
        var expectedMessages = MessageFormat.format(ITEM_NOT_FOUND_ERROR_MESSAGE_PATTERN, NON_EXISTENT_ID);
        // when
        when(mockedItemRepository.findById(NON_EXISTENT_ID)).thenReturn(Mono.empty());
        when(mockedCartItemRepository.findCartItemByItemId(NON_EXISTENT_ID)).thenReturn(Mono.empty());

        webTestClient.get()
                .uri(ITEMS_ROOT + "/" + NON_EXISTENT_ID)
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

    private static @NotNull Consumer<String> validate(TestCaseData given, String viewIdentifyingText) {
        return html -> {
            assertThat(html).contains(viewIdentifyingText);
            assertThat(html).contains(given.expectedMessages());
        };
    }

    private static Stream<Arguments> invalidCartItemActionProvider() {
        return Stream.of(
                Arguments.arguments(
                        "action NOT IN " + Arrays.toString(CartAction.values()),
                        new TestCaseData(
                                Map.of(CartItemAction.Fields.action, List.of("NON_EXISTENT_ACTION_TYPE")),
                                Set.of("action: value rejected = NON_EXISTENT_ACTION_TYPE")
                        )
                )
        );
    }

    private static Stream<Arguments> invalidItemsPageableRequestProvider() {
        return Stream.of(
                Arguments.arguments(
                        "pageNumber == -1",
                        new TestCaseData(
                                Map.of(ItemsPageableRequest.Fields.pageNumber, List.of("-1")),
                                Set.of("pageNumber: must be >= 0")
                        )
                ),
                Arguments.arguments(
                        "pageSize == -1",
                        new TestCaseData(
                                Map.of(ItemsPageableRequest.Fields.pageSize, List.of("-1")),
                                Set.of("pageSize: must be > 0")
                        )
                ),
                Arguments.arguments(
                        "pageSize == 0",
                        new TestCaseData(
                                Map.of(ItemsPageableRequest.Fields.pageSize, List.of("0")),
                                Set.of("pageSize: must be > 0")
                        )
                ),
                Arguments.arguments(
                        "pageSize == 101",
                        new TestCaseData(
                                Map.of(ItemsPageableRequest.Fields.pageSize, List.of("101")),
                                Set.of("pageSize: must be <= 100")
                        )
                ),
                Arguments.arguments(
                        "pageSize == 101 && pageNumber == -1",
                        new TestCaseData(
                                Map.of(
                                        ItemsPageableRequest.Fields.pageSize, List.of("101"),
                                        ItemsPageableRequest.Fields.pageNumber, List.of("-1")
                                ),
                                Set.of("pageSize: must be <= 100", "pageNumber: must be >= 0")
                        )
                ),
                Arguments.arguments(
                        "sort NOT IN " + Arrays.toString(CartAction.values()),
                        new TestCaseData(
                                Map.of(ItemsPageableRequest.Fields.sort, List.of("NON_EXISTENT_SORT_TYPE")),
                                Set.of("sort: value rejected = NON_EXISTENT_SORT_TYPE")
                        )
                )
        );
    }

    private record TestCaseData(Map<String, List<String>> requestParams, Set<String> expectedMessages) {
    }
}
