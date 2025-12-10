package edu.yandex.project.integration.controller;

import edu.yandex.project.controller.dto.*;
import edu.yandex.project.controller.dto.enums.CartAction;
import edu.yandex.project.controller.dto.enums.ItemSort;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.ModelAndView;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("ItemWebControllerIT")
public class ItemWebControllerIT extends AbstractControllerIT {
    private final static String ITEMS_ROOT = "/items";

    @Test
    @SuppressWarnings("unchecked")
    void getItemsShowcase_inCaseDefaultRequestParameters_thenGetNextPage_shouldReturn10ItemsIn2Pages() {
        // given
        // ***
        // default request:
        // search filter = "" (empty string -> matches all items)
        // page number = 0 (first page)
        // page size = 5 (should return 6 items, where 6th is stub with id = -1)
        // sort = NO (random order)
        // ***
        var allItemsFromDb = itemRepository.findAllWithCartCount("", ItemSort.NO.name(), Pageable.unpaged())
                .getContent();
        var allItemViewsMap = itemViewMapper.fromItemJoinCartViews(allItemsFromDb).stream()
                .collect(Collectors.toMap(ItemView::id, itemView -> itemView));
        // when
        // for null fields default values must be set
        var response = this.validateAndGetItemsShowcase(new ItemsPageableRequest(null, null, null, null));
        // then
        var firstPageInfo = validateAndGetPageInfo(response, 5, 0, false, true);

        var itemsViewTable = (List<List<ItemView>>) response.getModel().get(ItemListPageView.Fields.items);
        assertThat(itemsViewTable)
                .isNotNull()
                .isNotEmpty()
                .hasSize(2);    // 3 items & 2 items + 1 stub

        var firstRow = itemsViewTable.getFirst();
        // remove stub before validations
        var secondRow = itemsViewTable.getLast().stream()
                .filter(itemView -> itemView.id() > 0)
                .toList();
        assertThat(secondRow).hasSize(2);

        var responseItems = new HashSet<>(firstRow);
        responseItems.addAll(secondRow);
        // ***
        // when
        response = this.validateAndGetItemsShowcase(
                new ItemsPageableRequest(null, firstPageInfo.pageNumber() + 1, firstPageInfo.pageSize(), null)
        );
        // then
        // validate second page
        validateAndGetPageInfo(response, 5, 1, true, true);

        itemsViewTable = (List<List<ItemView>>) response.getModel().get(ItemListPageView.Fields.items);
        assertThat(itemsViewTable)
                .isNotNull()
                .isNotEmpty()
                .hasSize(2);    // 3 items & 2 items + 1 stub

        firstRow = itemsViewTable.getFirst();
        // remove stub before validations
        secondRow = itemsViewTable.getLast().stream()
                .filter(itemView -> itemView.id() > 0)
                .toList();
        assertThat(secondRow).hasSize(2);

        responseItems.addAll(firstRow);
        responseItems.addAll(secondRow);
        // **
        // validate received items
        assertThat(responseItems).hasSize(6 + 6 - 1 - 1);   // 1stPage.items + 2ndPage.items + 2 stubs (alignment x2)
        responseItems.forEach(responseItem -> assertThat(responseItem).isEqualTo(allItemViewsMap.get(responseItem.id())));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getItemsShowcase_inCaseCertainFiltersAndPagingParamsAndSortedByALPHA_shouldReturnSortedItemsWithGivenLimitations() {
        // given
        var searchFilters = ItemsPageableRequest.builder()
                .search("ом")
                .sort(ItemSort.ALPHA)
                .pageSize(10)
                .build();
        var expectedItemsViewsMap = itemRepository.findAllWithCartCount(
                        searchFilters.search(),
                        searchFilters.sort().name(),
                        PageRequest.of(searchFilters.pageNumber(), searchFilters.pageSize())
                )
                .getContent().stream()
                .map(itemViewMapper::fromItemJoinCartView)
                .collect(Collectors.toMap(ItemView::id, itemView -> itemView));
        // when
        var response = this.validateAndGetItemsShowcase(searchFilters);
        // then
        validateAndGetPageInfo(response, searchFilters.pageSize(), searchFilters.pageNumber(), false, false);

        var itemsViewTable = (List<List<ItemView>>) response.getModel().get(ItemListPageView.Fields.items);
        assertThat(itemsViewTable)
                .isNotNull()
                .isNotEmpty()
                .hasSize(expectedItemsViewsMap.size() / 3);

        var allResponseItems = itemsViewTable.stream()
                .flatMap(List::stream)
                .toList();
        // validate if all expected items found
        assertThat(expectedItemsViewsMap.size()).isEqualTo(allResponseItems.size());
        // validate if all items has expected state
        assertThat(allResponseItems).allMatch(itemView ->
                expectedItemsViewsMap.get(itemView.id()).equals(itemView)
        );
        // validate if search filter works fine
        assertThat(allResponseItems).allMatch(itemView -> {
            var title = itemView.title();
            var description = itemView.description();
            return title.contains(searchFilters.search()) || description.contains(searchFilters.search());
        });
        // validate sort (ALPHA)
        assertThat(allResponseItems).isSortedAccordingTo(Comparator.comparing(ItemView::title));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getItemsShowcase_inCaseUnpagedAndSortedByPRICEAndUnpaged_thenUpdateCartFromItemsShowcase_shouldReturnAllItemsInRequiredOrderWithUpdateCount() {
        // given
        var searchFilters = ItemsPageableRequest.builder()
                .sort(ItemSort.PRICE)
                .pageSize(50)
                .build();
        var expectedItemsViewsMap = itemRepository.findAllWithCartCount(
                        searchFilters.search(),
                        searchFilters.sort().name(),
                        PageRequest.of(searchFilters.pageNumber(), searchFilters.pageSize())
                )
                .getContent().stream()
                .map(itemViewMapper::fromItemJoinCartView)
                .collect(Collectors.toMap(ItemView::id, itemView -> itemView));
        assertThat(expectedItemsViewsMap.size()).isEqualTo(11);
        // when
        var response = this.validateAndGetItemsShowcase(searchFilters);
        // then
        validateAndGetPageInfo(response, searchFilters.pageSize(), searchFilters.pageNumber(), false, false);

        var itemsViewTable = (List<List<ItemView>>) response.getModel().get(ItemListPageView.Fields.items);
        assertThat(itemsViewTable)
                .isNotNull()
                .isNotEmpty()
                .hasSize((11 + 1) / 3); // with 1 stub

        var allResponseItems = itemsViewTable.stream()
                .flatMap(List::stream)
                .filter(itemView -> itemView.id() > 0)  // remove stub
                .toList();
        // validate if all expected items found
        assertThat(expectedItemsViewsMap.size()).isEqualTo(allResponseItems.size());
        // validate if all items has expected state
        assertThat(allResponseItems).allMatch(itemView ->
                expectedItemsViewsMap.get(itemView.id()).equals(itemView)
                        && itemView.count() == 0    // there are no item in the cart for now
        );
        // validate sort (ALPHA)
        assertThat(allResponseItems).isSortedAccordingTo(Comparator.comparing(ItemView::price));
        // ***
        // add items with id = 1 to the cart (count++)
        // then check if showcase been updated
        // when
        this.validateAndUpdateItemsShowcase(new CartItemAction(CartAction.PLUS, 1L), searchFilters);
        response = this.validateAndGetItemsShowcase(searchFilters);
        // then
        validateAndGetPageInfo(response, searchFilters.pageSize(), searchFilters.pageNumber(), false, false);

        itemsViewTable = (List<List<ItemView>>) response.getModel().get(ItemListPageView.Fields.items);
        assertThat(itemsViewTable)
                .isNotNull()
                .isNotEmpty()
                .hasSize((11 + 1) / 3); // with 1 stub

        allResponseItems = itemsViewTable.stream()
                .flatMap(List::stream)
                .filter(itemView -> itemView.id() > 0)  // remove stub
                .toList();
        // validate if all expected items found
        assertThat(expectedItemsViewsMap.size()).isEqualTo(allResponseItems.size());
        // validate sort (ALPHA)
        assertThat(allResponseItems).isSortedAccordingTo(Comparator.comparing(ItemView::price));
        // find if item with id = 1 count++
        var updatedItem = allResponseItems.stream()
                .filter(itemView -> itemView.count() == 1)
                .toList();
        assertThat(updatedItem).hasSize(1);
        var updatedItemFromDb = itemViewMapper.fromItemJoinCartView(
                itemRepository.findByIdWithCartCount(1L).orElseThrow()
        );
        assertThat(updatedItemFromDb).isEqualTo(updatedItem.getFirst());
    }

    @Test
    void getItemView_thenUpdateCartFromItemView_thenGetItemViewUpdated_success() {
        // given
        var itemJoinCartPageView = itemRepository.findByIdWithCartCount(7L).orElseThrow();

        var expectedItemView = itemViewMapper.fromItemJoinCartView(itemJoinCartPageView);
        var expectedViewName = "item";
        // when
        var response = this.validateAndGetItemView(itemJoinCartPageView.id());

        var actualItemView = (ItemView) response.getModel().get(expectedViewName);
        assertThat(actualItemView).isNotNull();

        assertThat(actualItemView.id()).isEqualTo(7L);
        assertThat(actualItemView.title()).isEqualTo("Эргономичный диван");
        assertThat(actualItemView.description()).isEqualTo("Просторный диван с ортопедическими подушками, механизмом трансформации и чехлом из экокожи");
        assertThat(actualItemView.imgPath()).isEqualTo("/images/sofa.jpeg");
        assertThat(actualItemView.price()).isEqualTo(125000);
        assertThat(actualItemView.count()).isEqualTo(0L);   // prev

        assertThat(actualItemView).isEqualTo(expectedItemView);
        // ***
        // add the item to the cart to chek if actualItemView.count() value will be incremented
        // when
        this.validateAndUpdateCartFromItemView(itemJoinCartPageView.id());
        // then
        expectedItemView = itemViewMapper.fromItemJoinCartView(
                itemRepository.findByIdWithCartCount(7L).orElseThrow()
        );
        response = this.validateAndGetItemView(itemJoinCartPageView.id());

        actualItemView = (ItemView) response.getModel().get(expectedViewName);
        assertThat(actualItemView).isNotNull();

        assertThat(actualItemView.count()).isEqualTo(1L);   // ++prev
        assertThat(actualItemView).isEqualTo(expectedItemView);
    }

    @SneakyThrows
    private ModelAndView validateAndGetItemView(Long itemId) {
        // given
        var expectedViewName = "item";
        // when
        var response = mockMvc.perform(get(ITEMS_ROOT + "/" + itemId))
                // then
                .andExpect(view().name(expectedViewName))
                .andExpect(model().attributeExists(expectedViewName))
                .andReturn()
                .getModelAndView();

        assertThat(response).isNotNull();
        return response;
    }

    @SneakyThrows
    private void validateAndUpdateCartFromItemView(Long itemId) {
        // given
        var expectedViewName = "redirect:/items/" + itemId;
        // when
        mockMvc.perform(post(ITEMS_ROOT + "/" + itemId)
                        .param("itemId", itemId.toString())
                        .param("action", CartAction.PLUS.toString())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                // then
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(expectedViewName));
    }

    @SneakyThrows
    private void validateAndUpdateItemsShowcase(CartItemAction cartItemAction,
                                                ItemsPageableRequest requestParameters) {
        // given
        var expectedViewName = "redirect:/items";
        // when
        mockMvc.perform(post(ITEMS_ROOT)
                        .param(CartItemAction.Fields.action, cartItemAction.action().name())
                        .param("id", cartItemAction.itemId().toString())
                        .param(ItemsPageableRequest.Fields.search, requestParameters.search())
                        .param(ItemsPageableRequest.Fields.pageNumber, requestParameters.pageNumber().toString())
                        .param(ItemsPageableRequest.Fields.pageSize, requestParameters.pageSize().toString())
                        .param(ItemsPageableRequest.Fields.sort, requestParameters.sort().name())
                )
                // then
                .andExpect(view().name(expectedViewName))
                .andExpect(model().attribute(ItemListPageView.Fields.search, requestParameters.search()))
                .andExpect(model().attribute(ItemListPageView.Fields.sort, requestParameters.sort().name()));
    }

    @SneakyThrows
    private ModelAndView validateAndGetItemsShowcase(ItemsPageableRequest requestParameters) {
        // given
        var expectedViewName = "items";
        // when
        var response = mockMvc.perform(get(ITEMS_ROOT)
                        .param(ItemsPageableRequest.Fields.search, requestParameters.search())
                        .param(ItemsPageableRequest.Fields.pageNumber, requestParameters.pageNumber().toString())
                        .param(ItemsPageableRequest.Fields.pageSize, requestParameters.pageSize().toString())
                        .param(ItemsPageableRequest.Fields.sort, requestParameters.sort().name())
                )
                // then
                .andExpect(view().name(expectedViewName))
                .andExpect(model().attributeExists(
                        ItemListPageView.Fields.items,
                        ItemListPageView.Fields.paging
                ))
                .andExpect(model().attribute(ItemListPageView.Fields.search, requestParameters.search()))
                .andExpect(model().attribute(ItemListPageView.Fields.sort, requestParameters.sort()))
                .andReturn()
                .getModelAndView();

        assertThat(response).isNotNull();
        return response;
    }

    private static PageInfo validateAndGetPageInfo(ModelAndView response,
                                                   int expectedPageSize,
                                                   int expectedPageNumber,
                                                   boolean expectedHasPrevious,
                                                   boolean expectedHasNext) {
        var pageInfo = (PageInfo) response.getModel().get(ItemListPageView.Fields.paging);
        assertThat(pageInfo).isNotNull();
        assertThat(pageInfo.pageSize()).isEqualTo(expectedPageSize);
        assertThat(pageInfo.pageNumber()).isEqualTo(expectedPageNumber);
        assertThat(pageInfo.hasPrevious()).isEqualTo(expectedHasPrevious);
        assertThat(pageInfo.hasNext()).isEqualTo(expectedHasNext);
        return pageInfo;
    }
}
