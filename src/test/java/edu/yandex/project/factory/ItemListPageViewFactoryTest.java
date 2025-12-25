package edu.yandex.project.factory;

import edu.yandex.project.controller.dto.ItemView;
import edu.yandex.project.controller.dto.ItemsPageableRequest;
import edu.yandex.project.controller.dto.PageInfo;
import edu.yandex.project.controller.dto.enums.ItemSort;
import edu.yandex.project.mapper.ItemViewMapper;
import edu.yandex.project.repository.util.view.ItemJoinCartPageView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemListPageViewFactoryTest {
    private final static int ITEMS_IN_ONE_ROW = 3;
    private final static long STUB_ITEM_ID = ItemView.createStub().id();
    private final static ItemsPageableRequest PAGEABLE_REQUEST = new ItemsPageableRequest("", 0, 50, ItemSort.NO);

    @Mock
    private ItemViewMapper itemViewMapper;
    @InjectMocks
    private ItemListPageViewFactory factory;

    @BeforeEach
    void setItemViewTableSize() {
        ReflectionTestUtils.setField(factory, "itemViewTableSize", ITEMS_IN_ONE_ROW);
    }

    @Test
    void create_inCasePageContentIsEmpty_shouldReturnItemListPageViewWithoutItems() {
        // given
        var itemEntitiesPage = new PageImpl<ItemJoinCartPageView>(List.of(), PageRequest.of(0, 5), 0);
        var pageableRequest = new ItemsPageableRequest("", 0, 5, ItemSort.NO);
        // when
        var actualResult = factory.create(itemEntitiesPage, pageableRequest);
        // then
        assertThat(actualResult).isNotNull();
        assertThat(actualResult.search()).isEqualTo(pageableRequest.search());
        assertThat(actualResult.sort()).isEqualTo(pageableRequest.sort());
        assertThat(actualResult.items())
                .isNotNull()
                .isEmpty();
        validatePageInfo(actualResult.paging(), itemEntitiesPage);

        verifyNoInteractions(itemViewMapper);
    }

    @Test
    void create_inCasePageContentHas1Element_shouldReturnFulfilledItemListPageView_and_itemsMustBeAligned() {
        // given
        var itemJoinCartPageView = ItemJoinCartPageView.builder().id(1L).build();
        var itemEntitiesPage = new PageImpl<>(List.of(itemJoinCartPageView), PageRequest.of(0, 5), 2);

        var mapperCallResult = List.of(ItemView.builder().id(1L).build());
        // when
        when(itemViewMapper.fromItemJoinCartViews(itemEntitiesPage.getContent())).thenReturn(mapperCallResult);

        var actualResult = factory.create(itemEntitiesPage, PAGEABLE_REQUEST);
        // then
        assertThat(actualResult).isNotNull();
        assertThat(actualResult.search()).isEqualTo(PAGEABLE_REQUEST.search());
        assertThat(actualResult.sort()).isEqualTo(PAGEABLE_REQUEST.sort());

        assertThat(actualResult.items())
                .isNotEmpty()
                .hasSize(1);
        validateAlignment(actualResult.items().getFirst(), 2);

        validatePageInfo(actualResult.paging(), itemEntitiesPage);
    }

    @Test
    void create_inCasePageContentHas5Elements_shouldReturnFulfilledItemListPageView_and_itemsMustBeSplitApartAndAligned() {
        // given
        var itemJoinCartPageViews = IntStream.range(1, 6)
                .mapToObj(this::createSimpleItemJoinCartPageView)
                .toList();
        var itemEntitiesPage = new PageImpl<>(itemJoinCartPageViews, PageRequest.of(0, 50), 5);

        var mapperCallResult = itemEntitiesPage.getContent().stream()
                .map(this::createSimpleItemView)
                .toList();
        // when
        when(itemViewMapper.fromItemJoinCartViews(itemEntitiesPage.getContent())).thenReturn(mapperCallResult);

        var actualResult = factory.create(itemEntitiesPage, PAGEABLE_REQUEST);
        // then
        assertThat(actualResult).isNotNull();
        assertThat(actualResult.search()).isEqualTo(PAGEABLE_REQUEST.search());
        assertThat(actualResult.sort()).isEqualTo(PAGEABLE_REQUEST.sort());
        // 6 elements must be split into 3 and (2 + 1 stub) lists
        assertThat(actualResult.items())
                .isNotEmpty()
                .hasSize(2);
        // check if first list has 3 elements with no stubs
        validateAlignment(actualResult.items().getFirst(), 0);
        // check if second list has 1 stub (elem with is = -1L)
        validateAlignment(actualResult.items().getLast(), 1);

        validatePageInfo(actualResult.paging(), itemEntitiesPage);
    }

    @Test
    void create_inCasePageContentHas9Elements_shouldReturnFulfilledItemListPageView_and_itemsMustBeSplitApartAndAligned() {
        // given
        var itemJoinCartPageViews = IntStream.range(1, 10)
                .mapToObj(this::createSimpleItemJoinCartPageView)
                .toList();
        var itemEntitiesPage = new PageImpl<>(itemJoinCartPageViews, PageRequest.of(0, 50), 10);

        var mapperCallResult = itemEntitiesPage.getContent().stream()
                .map(this::createSimpleItemView)
                .toList();
        // when
        when(itemViewMapper.fromItemJoinCartViews(itemEntitiesPage.getContent())).thenReturn(mapperCallResult);

        var actualResult = factory.create(itemEntitiesPage, PAGEABLE_REQUEST);
        // then
        assertThat(actualResult).isNotNull();
        // 9 elements must be split into 3 x 3 with no stubs added!
        assertThat(actualResult.items())
                .isNotEmpty()
                .hasSize(3);
        for (int i = 0; i < 3; i++) {
            // check each if no stubs added
            validateAlignment(actualResult.items().get(i), 0);
        }
        validatePageInfo(actualResult.paging(), itemEntitiesPage);
    }

    private static void validateAlignment(List<ItemView> itemsRow, long expectedStubsCount) {
        assertThat(itemsRow)
                .isNotEmpty()
                .hasSize(ITEMS_IN_ONE_ROW);
        long actualStubsCount = itemsRow.stream()
                .map(ItemView::id)
                .filter(id -> id == STUB_ITEM_ID)
                .count();
        assertThat(expectedStubsCount).isEqualTo(actualStubsCount);
    }

    private static void validatePageInfo(PageInfo toBeValidated, PageImpl<ItemJoinCartPageView> itemEntitiesPage) {
        assertThat(toBeValidated).isNotNull();
        assertThat(toBeValidated.pageSize()).isEqualTo(itemEntitiesPage.getSize());
        assertThat(toBeValidated.pageNumber()).isEqualTo(itemEntitiesPage.getNumber());
        assertThat(toBeValidated.hasPrevious()).isEqualTo(itemEntitiesPage.hasPrevious());
        assertThat(toBeValidated.hasNext()).isEqualTo(itemEntitiesPage.hasNext());
    }

    private ItemView createSimpleItemView(ItemJoinCartPageView itemJoinCartPageView) {
        return ItemView.builder().id(itemJoinCartPageView.id()).build();
    }

    private ItemJoinCartPageView createSimpleItemJoinCartPageView(long id) {
        return ItemJoinCartPageView.builder().id(id).build();
    }
}
