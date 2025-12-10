package edu.yandex.project.integration.exception.handler;

import edu.yandex.project.controller.dto.CartItemAction;
import edu.yandex.project.controller.dto.ItemsPageableRequest;
import edu.yandex.project.controller.dto.enums.CartAction;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.servlet.ModelAndView;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@Tag("ItemWebControllerExceptionHandlerIT")
public class ItemWebControllerWebExceptionHandlerIT extends AbstractGlobalWebExceptionHandlerIT {
    private final static String ITEMS_ROOT = "/items";

    @MethodSource("invalidItemsPageableRequestProvider")
    @ParameterizedTest(name = "ItemWebController::getItemsShowcase -> {0}")
    void getItemsShowcase_handleValidationExceptions(String ignored, TestCaseData given) throws Exception {
        // given
        var expectedView = ERR_DIR_NAME + HttpStatus.BAD_REQUEST.value();
        // when
        var response = mockMvc.perform(get(ITEMS_ROOT)
                        .params(new LinkedMultiValueMap<>(given.requestParams())))
                // then
                .andExpect(view().name(expectedView))
                .andExpect(model().attributeExists(ERR_MESSAGE_KEY))
                .andReturn()
                .getModelAndView();

        validateResponse(given, response);
    }

    @MethodSource({"invalidItemsPageableRequestProvider", "invalidCartItemActionProvider"})
    @ParameterizedTest(name = "ItemWebController::updateCartFromItemsShowcase -> {0}")
    void updateCartFromItemsShowcase_handleValidationExceptions(String ignored, TestCaseData given) throws Exception {
        // given
        var expectedView = ERR_DIR_NAME + HttpStatus.BAD_REQUEST.value();
        // when
        var response = mockMvc.perform(post(ITEMS_ROOT)
                        .params(new LinkedMultiValueMap<>(given.requestParams())))
                // then
                .andExpect(view().name(expectedView))
                .andExpect(model().attributeExists(ERR_MESSAGE_KEY))
                .andReturn()
                .getModelAndView();

        validateResponse(given, response);
    }

    @MethodSource("invalidCartItemActionProvider")
    @ParameterizedTest(name = "ItemWebController::updateCartFromItemView -> {0}")
    void updateCartFromItemView_handleValidationExceptions(String ignored, TestCaseData given) throws Exception {
        // given
        var expectedView = ERR_DIR_NAME + HttpStatus.BAD_REQUEST.value();
        // when
        var response = mockMvc.perform(post(ITEMS_ROOT + "/1")
                        .params(new LinkedMultiValueMap<>(given.requestParams())))
                // then
                .andExpect(view().name(expectedView))
                .andExpect(model().attributeExists(ERR_MESSAGE_KEY))
                .andReturn()
                .getModelAndView();

        validateResponse(given, response);
    }

    @Test
    void getItemView_handleItemNotFoundException_inCaseNonExistentItemId() throws Exception {
        // given
        var expectedView = ERR_DIR_NAME + HttpStatus.NOT_FOUND.value();
        var expectedErrorMessage = MessageFormat.format(ITEM_NOT_FOUND_ERROR_MESSAGE_PATTERN, NON_EXISTENT_ID);
        // when
        when(mockedItemRepository.findByIdWithCartCount(NON_EXISTENT_ID)).thenReturn(Optional.empty());

        mockMvc.perform(get(ITEMS_ROOT + "/" + NON_EXISTENT_ID))
                // then
                .andExpect(view().name(expectedView))
                .andExpect(model().attribute(ERR_MESSAGE_KEY, expectedErrorMessage));
    }

    private static void validateResponse(TestCaseData given, ModelAndView response) {
        assertThat(response).isNotNull();
        var errMessage = ((Object[]) response.getModel().get(ERR_MESSAGE_KEY))[0];
        assertThat((String) errMessage).contains(given.expectedMessages());
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
